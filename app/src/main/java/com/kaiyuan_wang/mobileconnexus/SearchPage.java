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
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchPage extends ActionBarActivity {
    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private String TAG  = "SearchPage";
    final ArrayList<String> image_urls = new ArrayList<String>();
    final ArrayList<String> stream_names = new ArrayList<String>();
    int display_index = 0;
//    JSONArray streamsFoundIDs;
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);
        display_index = 8;
        final String searchContent = getIntent().getStringExtra("search_content");
        String searchContentUrl = searchContent.replaceAll(" ", "+");
        String requestUrl = "http://miniproject-1107.appspot.com/mobile_show_result?search_keyword="+searchContentUrl;
        final TextView responseText = (TextView) this.findViewById(R.id.search_result_message);
        //responseText.setText(new String(streamName));

        httpClient.get(requestUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                //responseText.setText(new String(response));
                //System.out.println(new String(response));
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray jStreams = jObject.getJSONArray("streams");

                    for (int i = 0; i < jStreams.length(); i++) {
                        String stream = jStreams.getString(i);
                        JSONObject jStream = new JSONObject(stream);
                        image_urls.add(jStream.getString("cover_url"));
                        stream_names.add(jStream.getString("stream_name"));
                    }
                    if (display_index < stream_names.size()) {
                        Button more_btn = (Button) findViewById(R.id.more_search_results);
                        more_btn.setVisibility(View.VISIBLE);
                    }
                    final ArrayList<String> display_image_urls = getMore(image_urls);
                    final ArrayList<String> display_stream_names = getMore(stream_names);
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context, display_image_urls));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Intent intent= new Intent(SearchPage.this, ViewSingleStream.class);
                            intent.putExtra("position",position);
                            intent.putExtra("numOfMatches",stream_names.size());
                            intent.putExtra("streamName",display_stream_names.get(position));
//                            intent.putExtra("streamID",streamIDs.get(position));
                            startActivity(intent);

                        }
                    });
                    String searchResultsMessage = stream_names.size()+" results for "+ searchContent+"\n"+"click on an image to view stream";
                    responseText.setText(new String(searchResultsMessage));
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

    private ArrayList<String> getMore(ArrayList<String> arr) {
        ArrayList<String> results = new ArrayList<String>();
        for (int i = Math.max(0, display_index - 8); i < Math.min(arr.size(), display_index); i++) {
            results.add(arr.get(i));
        }
        return results;
    }

    public void searchHandler(View view){
        Intent intent = new Intent(this,SearchPage.class);
        EditText text = (EditText)findViewById(R.id.search_content);
        String returnText = text.getText().toString();
        intent.putExtra("search_content", returnText);
        startActivity(intent);
    }

    public void moreSearchResult(View view) {
//        Intent intent = new Intent(this, NearbyPhotos.class);
//        intent.putExtra("indexes",indexes);
//        startActivity(intent);
        display_index += 8;
        final ArrayList<String> display_image_urls = getMore(image_urls);
        final ArrayList<String> display_stream_names = getMore(stream_names);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(context, display_image_urls));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent intent= new Intent(SearchPage.this, ViewSingleStream.class);
                intent.putExtra("position",position);
                intent.putExtra("numOfMatches",stream_names.size());
                intent.putExtra("streamName",display_stream_names.get(position));
//                            intent.putExtra("streamID",streamIDs.get(position));
                startActivity(intent);

            }
        });
        if (display_index >= stream_names.size()) {
            Button more_btn = (Button) findViewById(R.id.more_search_results);
            more_btn.setVisibility(View.INVISIBLE);
        }
    }
}
