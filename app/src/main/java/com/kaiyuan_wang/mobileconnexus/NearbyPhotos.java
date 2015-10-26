package com.kaiyuan_wang.mobileconnexus;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearbyPhotos extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private AsyncHttpClient httpClient = new AsyncHttpClient();
    private String TAG  = "NearbyPhotos";

    private String userName;
//    public static String indexes;
    Context context = this;
    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient mLocationClient;
    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            System.out.println("CONNECTION FAILED");
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                // errorFragment.show(getSupportFragmentManager(),"Location Updates");
            }
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_photos);
        if (servicesConnected()){
            System.out.println("servicesConnected");
            mLocationClient = new LocationClient(this, this, this);
            display_index = capacity;
            userName = getIntent().getStringExtra("userName");
            Button sign_out_btn = (Button) findViewById(R.id.sign_out_button);
            if (userName.equals("no_user")) {
                sign_out_btn.setText("Go Login");
            }
            sign_out_btn.setVisibility(View.VISIBLE);
            sign_out_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NearbyPhotos.this, LoginPage.class);
                    intent.putExtra("userName", userName);
                    if (!userName.equals("no_user"))
                        Toast.makeText(getApplicationContext(), userName + " signed out", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            });
        }
    }

    int display_index = 0;
    int capacity = 8;
    final ArrayList<String> image_urls = new ArrayList<String>();
    final ArrayList<String> photo_stream_names = new ArrayList<String>();
    final ArrayList<String> photo_distances = new ArrayList<String>();
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
//        indexes = getIntent().getStringExtra("indexes");
        String location=mLocationClient.getLastLocation().getLatitude()+"_"+mLocationClient.getLastLocation().getLongitude();
        final String request_url = "http://connexus-kaiyuanw.appspot.com/mobile_nearby_photos/"+location;

        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    JSONObject jObject = new JSONObject(new String(response));
//                    String user = jObject.getString("user");
                    JSONArray jPhotos = jObject.getJSONArray("photos");

                    for(int i=0;i<jPhotos.length();i++) {
                        String photo = jPhotos.getString(i);
                        JSONObject jPhoto = new JSONObject(photo);
                        image_urls.add(jPhoto.getString("image_url"));
                        photo_stream_names.add(jPhoto.getString("stream_name"));
                        photo_distances.add(jPhoto.getString("str_distance"));
                    }
                    if (display_index < image_urls.size()) {
                        Button more_nearby_photos = (Button)findViewById(R.id.more_nearby_photos);
                        more_nearby_photos.setVisibility(View.VISIBLE);
                    }

                    final ArrayList<String> display_image_urls = getMore(image_urls);
                    final ArrayList<String> display_photo_stream_names = getMore(photo_stream_names);
                    final ArrayList<String> display_photo_distances = getMore(photo_distances);

                    GridView gridview = (GridView) findViewById(R.id.gridview);
//                    gridview.setAdapter(new ImageAdapter(context,display_image_urls));
                    gridview.setAdapter(new SquareImageAdapter(context, display_image_urls, display_photo_distances));
                    gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View v,
                                                       int position, long id) {
                            Toast.makeText(context, display_photo_distances.get(position), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            Intent intent= new Intent(NearbyPhotos.this, ViewSingleStream.class);
                            intent.putExtra("position",position);
                            intent.putExtra("streamName",display_photo_stream_names.get(position));
//                            intent.putExtra("streamID",streamIDs.get(position));
                            intent.putExtra("userName", userName);
                            startActivity(intent);

                        }
                    });

                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG,"There was a problem in retrieving the url : " + e.toString());
            }
        });



    }

    private ArrayList<String> getMore(ArrayList<String> arr) {
        ArrayList<String> results = new ArrayList<String>();
        for (int i = Math.max(0, display_index - capacity); i < Math.min(arr.size(), display_index); i++) {
            results.add(arr.get(i));
        }
        return results;
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            System.out.println(connectionResult.getErrorCode());
            //showErrorDialog(connectionResult.getErrorCode());
        }
    }



    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();

    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
    // Global variable to hold the current location
    //Location mCurrentLocation;
    //mCurrentLocation = mLocationClient.getLastLocation();
    public void viewAllStreams(View view){
        Intent intent = new Intent(this, ViewAllStreams.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    public void moreNearbyPhotos(View view) {
//        Intent intent = new Intent(this, NearbyPhotos.class);
//        intent.putExtra("indexes",indexes);
//        startActivity(intent);
        display_index += capacity;
        final ArrayList<String> display_image_urls = getMore(image_urls);
        final ArrayList<String> display_photo_stream_names = getMore(photo_stream_names);
        final ArrayList<String> display_photo_distances = getMore(photo_distances);

        GridView gridview = (GridView) findViewById(R.id.gridview);
//        gridview.setAdapter(new ImageAdapter(context,display_image_urls));
        gridview.setAdapter(new SquareImageAdapter(context, display_image_urls, display_photo_distances));
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {
                Toast.makeText(context, display_photo_distances.get(position), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(NearbyPhotos.this, ViewSingleStream.class);
                intent.putExtra("position", position);
                intent.putExtra("streamName", display_photo_stream_names.get(position));
//                            intent.putExtra("streamID",streamIDs.get(position));
                intent.putExtra("userName", userName);
                startActivity(intent);

            }
        });
        if (display_index >= image_urls.size()) {
            Button more_nearby_photos = (Button)findViewById(R.id.more_nearby_photos);
            more_nearby_photos.setVisibility(View.INVISIBLE);
        }
    }
}
