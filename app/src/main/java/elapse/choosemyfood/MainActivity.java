package elapse.choosemyfood;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {
    private static final String TAG = "Main_Activity";
    private static final String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";
    //Search Options
    private String[] keywords ={};
    private Location mCurrentLocation;
    private String radius = "25000"; //Max 50000
    private String minPrice = "0";
    private String maxPrice = "4";
    private static final String openNow = "true";
    private static final String type = "restaurant";


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;



    private static final int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton searchButton = (ImageButton) findViewById(R.id.init_search);
        searchButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String url = buildURL(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
                Intent intent = new Intent(getApplicationContext(),Restaurant.class);
                intent.putExtra("reqUrl",url);
                startActivity(intent);
            }
        });
        setupLocationServices();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        Toast.makeText(MainActivity.this,"Error connecting to Google play services",Toast.LENGTH_LONG).show();    }

    private void setupLocationServices(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                for (Location loc : locationResult.getLocations()){
                    mCurrentLocation =loc;
                }
            }
        };
        checkAndRequestPermissions();
        checkLocationSettings();
        try{
            mFusedLocationClient.requestLocationUpdates(buildLocationRequest(),mLocationCallback,null);
        }catch (SecurityException e){
            checkAndRequestPermissions();
            setupLocationServices();
        }
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws java.io.IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();

        return new JSONObject(jsonString);
    }


    private String buildURL(double lat, double lon){
        ///Necessary Settings
        String url = baseUrl+"location="+lat+","+lon+
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
        url += "&key="+googleApiKey;
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
        LocationRequest mLocationRequest = buildLocationRequest();
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
    private LocationRequest buildLocationRequest(){
        LocationRequest request = createLocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(2000)
                .setInterval(4000);
        return request;
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


