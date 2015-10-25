package com.kaiyuan_wang.mobileconnexus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewSingleStream extends ActionBarActivity {
    private String TAG  = "ViewSingleStream";
    Context context = this;
    public static String REQUEST_ViewSingleStream = "http://miniproject-1107.appspot.com/mobile_stream_name";
    private AsyncHttpClient httpClient = new AsyncHttpClient();
    final ArrayList<String> image_urls = new ArrayList<String>();
    final ArrayList<String> photo_captions = new ArrayList<String>();
    private int display_index = 0;
    private int capacity = 16;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_stream);
        userName = getIntent().getStringExtra("userName");
        Button sign_out_btn = (Button) findViewById(R.id.sign_out_button);
        if (userName.equals("no_user")) {
            sign_out_btn.setText("Go Login");
        }
        sign_out_btn.setVisibility(View.VISIBLE);
        sign_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewSingleStream.this, LoginPage.class);
                intent.putExtra("userName", userName);
                if (!userName.equals("no_user"))
                    Toast.makeText(getApplicationContext(), userName + " signed out", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
        display_index = capacity;
        String streamName = getIntent().getStringExtra("streamName");
//        String streamID = getIntent().getStringExtra("streamID");
        TextView responseText = (TextView) this.findViewById(R.id.stream_name);
        responseText.setText(new String(streamName));

        final String request_url = REQUEST_ViewSingleStream + "=" + streamName + "?" + userName;

        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    String user = jObject.getString("user");
                    JSONArray jPhotos = jObject.getJSONArray("photos");
                    String user_email = jObject.getString("owner");
                    if (userName.equals(user_email)) {
                        Button upload_btn = (Button) findViewById(R.id.upload_btn);
                        upload_btn.setVisibility(View.VISIBLE);
                    }

                    for(int i=0;i<jPhotos.length();i++) {
                        String photo = jPhotos.getString(i);
                        JSONObject jPhoto = new JSONObject(photo);
                        image_urls.add(jPhoto.getString("image_url"));
                        String cap = jPhoto.getString("caption");
                        if (cap.equals("null") || cap.equals(""))
                            cap = "no caption";
                        photo_captions.add(cap);
                    }
                    if (display_index < image_urls.size()) {
                        Button more_btn = (Button) findViewById(R.id.more_photos);
                        more_btn.setVisibility(View.VISIBLE);
                    }
                    final ArrayList<String> display_image_urls = getMore(image_urls);
                    final ArrayList<String> display_photo_captions = getMore(photo_captions);

                    GridView gridview = (GridView) findViewById(R.id.gridview);
//                    gridview.setAdapter(new ImageAdapter(context,display_image_urls));
                    gridview.setAdapter(new SquareImageAdapter(context, display_image_urls, display_photo_captions));
                    gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                                       int position, long id) {
                            Toast.makeText(context, display_photo_captions.get(position), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.thumb_nail);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_view);

                            Picasso.with(context).load(display_image_urls.get(position)).into(image);

                            imageDialog.show();
                        }
                    });
                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    public void viewAllStreams(View view){
        Intent intent = new Intent(this, ViewAllStreams.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    public void uploadImage(View view){
        Intent intent = new Intent(this,Upload.class);
        intent.putExtra("userName", userName);
        String streamName = getIntent().getStringExtra("streamName");
        String streamID = getIntent().getStringExtra("streamID");
        intent.putExtra("streamName",streamName);
        intent.putExtra("streamID", streamID);
        startActivity(intent);
    }

    private ArrayList<String> getMore(ArrayList<String> arr) {
        ArrayList<String> results = new ArrayList<String>();
        for (int i = Math.max(0, display_index - capacity); i < Math.min(arr.size(), display_index); i++) {
            results.add(arr.get(i));
        }
        return results;
    }

    public void morePhotos(View view) {
//        Intent intent = new Intent(this, NearbyPhotos.class);
//        intent.putExtra("indexes",indexes);
//        startActivity(intent);
        display_index += capacity;
        final ArrayList<String> display_image_urls = getMore(image_urls);
        final ArrayList<String> display_photo_captions = getMore(photo_captions);
        GridView gridview = (GridView) findViewById(R.id.gridview);
//        gridview.setAdapter(new ImageAdapter(context, display_image_urls));
        gridview.setAdapter(new SquareImageAdapter(context, display_image_urls, display_photo_captions));
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {
                Toast.makeText(context, display_photo_captions.get(position), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Dialog imageDialog = new Dialog(context);
                imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                imageDialog.setContentView(R.layout.thumb_nail);
                ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_view);

                Picasso.with(context).load(display_image_urls.get(position)).into(image);

                imageDialog.show();
            }
        });
        if (display_index >= image_urls.size()) {
            Button more_btn = (Button) findViewById(R.id.more_photos);
            more_btn.setVisibility(View.INVISIBLE);
        }
    }

}
