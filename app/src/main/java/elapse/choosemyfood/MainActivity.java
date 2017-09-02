package elapse.choosemyfood;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener  {
    private static final String TAG = "Main_Activity";
    private static final String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";
    //Search Options
    private String[] keywords;
    private Location mCurrentLocation;
    private String radius = "25000"; //Max 50000
    private String minPrice = "0";
    private String maxPrice = "4";
    private static final String openNow = "true";
    private static final String type = "restaurant";


    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;


    private static final int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
        checkAndRequestPermissions();
        checkLocationSettings();


    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // TODO: On connection to google play failed
    }

    private void getRestaurant(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String req = buildURL();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, req, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray results = response.getJSONArray("results");
                            Random rn = new Random();
                            int i = rn.nextInt(response.length());
                            String placeId = results.getJSONObject(i).getString("place_id");
                            Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId)
                                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                        @Override
                                        public void onResult(@NonNull PlaceBuffer places) {
                                            if(places.getStatus().isSuccess()){
                                                Place rest = places.get(0);
                                                //TODO: OPen new page with info
                                            }else{
                                                Log.d(TAG,"Error finding place by ID");
                                            }
                                        }
                                    });


                        }catch (JSONException e){
                            Log.d(TAG,"Error parsing JSON Response");
                            Toast.makeText(MainActivity.this, "Error Parsing Json. Try Again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(MainActivity.this, "Error retrieving restaurant. Try Again.", Toast.LENGTH_LONG).show();
                    }
                });
        queue.add(jsObjRequest);

    }

    private void getLocation() {
        try{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mCurrentLocation = location;

                            }
                        }
                    });
        }catch(SecurityException e){
            checkAndRequestPermissions();
            getLocation();
        }
    }

    private String buildURL(){
        ///Necessary Settings
        String url = baseUrl+"location="+mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude()+
                        "&radius="+radius+"&type="+type+"&opennow=true";
        //Optional
        if(keywords.length>0){
            url+="&keyword=";
            for(String word : keywords){
                url+= word+" ";
            }
        }
        if (!minPrice.equals("0")){
            url+= "&minPrice="+minPrice;
        }
        if(!maxPrice.equals("4")){
            url+="&maxPrice="+maxPrice;
        }
        url += "&keyword="+googleApiKey;
        return url;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void checkLocationSettings(){
        LocationRequest mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>(){
            @Override
            public void onSuccess (LocationSettingsResponse resp){
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });

    }

    private void checkAndRequestPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //TODO: Notify permissions needed
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
