package com.kaiyuan_wang.mobileconnexus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewAllStreams extends ActionBarActivity {
    public static String REQUEST_ViewAllStreams = "http://connexus-kaiyuanw.appspot.com/mobile_view_all_stream";
    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private String TAG = "ViewAllStreams";
    Context context = this;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_streams);
        userName = getIntent().getStringExtra("userName");

        Button sign_out_btn = (Button) findViewById(R.id.sign_out_button);
        if (userName.equals("no_user")) {
            sign_out_btn.setText("Go Login");
        }
        sign_out_btn.setVisibility(View.VISIBLE);
        sign_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewAllStreams.this, LoginPage.class);
                intent.putExtra("userName", userName);
                if (!userName.equals("no_user"))
                    Toast.makeText(getApplicationContext(), userName + " signed out", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        httpClient.get(REQUEST_ViewAllStreams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> image_urls = new ArrayList<String>();
//                final ArrayList<String> streamIDs = new ArrayList<String>();
                final ArrayList<String> stream_names = new ArrayList<String>();
                final ArrayList<String> stream_create_times = new ArrayList<String>();

                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    String user = jObject.getString("user");
                    JSONArray jStreams = jObject.getJSONArray("streams");

                    for (int i = 0; i < jStreams.length(); i++) {
                        String stream = jStreams.getString(i);
                        JSONObject jStream = new JSONObject(stream);
                        image_urls.add(jStream.getString("cover_url"));
//                        String streamIDNum = jObject2.getString("streamID");
//                        streamIDs.add(streamIDNum);
                        stream_names.add(jStream.getString("stream_name"));
                        stream_create_times.add(jStream.getString("create_time"));
                    }
                    GridView gridview = (GridView) findViewById(R.id.gridview);
//                    gridview.setAdapter(new ImageAdapter(context, image_urls));
                    gridview.setAdapter(new SquareImageAdapter(context, image_urls, stream_names));
                    gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                                       int position, long id) {
                            Toast.makeText(context, stream_names.get(position) + "\n" + stream_create_times.get(position), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            Intent intent = new Intent(ViewAllStreams.this, ViewSingleStream.class);
                            intent.putExtra("position", position);
                            intent.putExtra("userName", userName);
                            intent.putExtra("streamName", stream_names.get(position));
//                            intent.putExtra("streamID", streamIDs.get(position));
                            startActivity(intent);

                        }
                    });

//                    String userName = getIntent().getStringExtra("userName");
                    if (!userName.equals("no_user")) {
                        Button subscribed_streams_btn = (Button) findViewById(R.id.subscribed_streams);
                        subscribed_streams_btn.setVisibility(View.VISIBLE);
                    }

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

    public void searchHandler(View view) {
        Intent intent = new Intent(this, SearchPage.class);
        EditText text = (EditText) findViewById(R.id.search_content);
        String returnText = text.getText().toString();
        intent.putExtra("search_content", returnText);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    public void viewNearbyPhotos(View view) {
        Intent intent = new Intent(this, NearbyPhotos.class);
        intent.putExtra("indexes", "0_15");
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    public void viewSubscribedStreams(View view) {
        Intent intent = new Intent(this, SubscribedStreams.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }
}