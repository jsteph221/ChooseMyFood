package elapse.choosemyfood;


import android.Manifest;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity  implements OnConnectionFailedListener {
    private static final int  MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private boolean LOCATION_GRANTED = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(myToolbar != null) {
            setSupportActionBar(myToolbar);
            getSupportActionBar().openOptionsMenu();
        }
        ImageButton searchButton = (ImageButton) findViewById(R.id.init_search);
        searchButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Location loc = PreferencesSingleton.getInstance().getLocation();
                if(!LOCATION_GRANTED){
                    Toast.makeText(MainActivity.this,"This app requires Location permissions to function.",Toast.LENGTH_SHORT);
                }else{
                    if (loc == null){
                        Toast.makeText(MainActivity.this,"Error getting Location.Verify location is on.",Toast.LENGTH_SHORT);
                    }else{
                        initSearch();
                    }
                }
            }
        });
        setupLocationServices();
        
    }
    private void initSearch(){
        checkLocationSettings();
    }
    private void search(){
        Location loc = PreferencesSingleton.getInstance().getLocation();
        String latlong = loc.getLatitude()+","+loc.getLongitude();
        Intent intent = new Intent(MainActivity.this,ShowActivity.class);
        intent.putExtra("lat_long",latlong);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //Intent i = new Intent(MainActivity.this, PreferencesActivity.class);
                //startActivity(i);
                //return true;
                Log.d(MainActivity.this.getLocalClassName(),"Switch case ");
                Intent i = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                Log.d(MainActivity.this.getLocalClassName(),"Switch default ");

                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        Toast.makeText(MainActivity.this,"Error connecting to Google play services",Toast.LENGTH_LONG).show();    }

    private void setupLocationServices(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
        LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                for (Location loc : locationResult.getLocations()){
                    PreferencesSingleton.getInstance().setLocation(loc);
                }
            }
        };
        checkAndRequestPermissions();
        try{
            mFusedLocationClient.requestLocationUpdates(buildLocationRequest(),mLocationCallback,null);
        }catch (SecurityException e){
            checkAndRequestPermissions();
            setupLocationServices();
        }
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
                search();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

    }
    private LocationRequest buildLocationRequest(){
        LocationRequest request = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(5000)
                .setInterval(10000);
        return request;
    }
    private void checkAndRequestPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            LOCATION_GRANTED = true;
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
                    LOCATION_GRANTED = true;

                } else {
                    Toast.makeText(MainActivity.this,"This app requires Location permissions to function.",Toast.LENGTH_SHORT);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}


