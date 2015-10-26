package com.kaiyuan_wang.mobileconnexus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubscribedStreams extends ActionBarActivity {

    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private String TAG  = "SubscribedStreams";
    Context context = this;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_streams);
        userName = getIntent().getStringExtra("userName");
        Button sign_out_btn = (Button) findViewById(R.id.sign_out_button);
        if (userName.equals("no_user")) {
            sign_out_btn.setText("Go Login");
        }
        sign_out_btn.setVisibility(View.VISIBLE);
        sign_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscribedStreams.this, LoginPage.class);
                intent.putExtra("userName", userName);
                if (!userName.equals("no_user"))
                    Toast.makeText(getApplicationContext(), userName + " signed out", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        String REQUEST_ViewAllStreams = "http://connexus-kaiyuanw.appspot.com/mobile_subscribed_streams/"+userName;
        System.out.println("-----------" + REQUEST_ViewAllStreams);
        httpClient.get(REQUEST_ViewAllStreams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                //responseText.setText(new String(response));
                //System.out.println(new String(response));

                final ArrayList<String> image_urls = new ArrayList<String>();
                final ArrayList<String> photo_upload_dates = new ArrayList<String>();
                final ArrayList<String> photo_captions = new ArrayList<String>();
                final ArrayList<String> photo_stream_names = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    String user = jObject.getString("user");
                    JSONArray jPhotos = jObject.getJSONArray("photos");
                    for (int i = 0; i < jPhotos.length(); i++) {
                        String photo = jPhotos.getString(i);
                        JSONObject jPhoto = new JSONObject(photo);
                        image_urls.add(jPhoto.getString("image_url"));
                        photo_upload_dates.add(jPhoto.getString("upload_date"));
                        photo_captions.add(jPhoto.getString("caption"));
                        photo_stream_names.add(jPhoto.getString("stream_name"));
                    }

                    GridView gridview = (GridView) findViewById(R.id.gridview);
//                    gridview.setAdapter(new ImageAdapter(context, image_urls));
                    gridview.setAdapter(new SquareImageAdapter(context, image_urls, photo_captions));
                    gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                                       int position, long id) {
                            Toast.makeText(context, photo_captions.get(position) + "\n" + photo_upload_dates.get(position), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            Intent intent = new Intent(SubscribedStreams.this, ViewSingleStream.class);
                            intent.putExtra("position", position);
                            intent.putExtra("streamName", photo_stream_names.get(position));
//                            intent.putExtra("streamID",streamIDs.get(position));
                            intent.putExtra("userName", userName);
                            startActivity(intent);

                        }
                    });

                } catch (JSONException j) {
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });

    }

}
