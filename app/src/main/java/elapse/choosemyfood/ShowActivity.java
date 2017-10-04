package elapse.choosemyfood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * Main activity to launch,get location, and init searches
 */

public class ShowActivity extends AppCompatActivity{
    private static final int  MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";

    private static final String TAG = "ShowActivity";

    private Restaurant currentRest;
    private Bitmap imgSrc;

    private ArrayList<String> placeIds;

    private RestaurantRequest restReq;
    private Random rn;


    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(myToolbar != null) {
            setSupportActionBar(myToolbar);
            getSupportActionBar().openOptionsMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rn = new Random();
        String latLong = (String) getIntent().getStringExtra("lat_long");
         restReq = new RestaurantRequest(getApplicationContext(), new VolleyIdCallback() {
            @Override
            public void onSuccess(ArrayList<String> ids) {
                placeIds = ids;
                 getRestaurantDetails();
              }
             @Override
             public void onFailure() {
                 Toast.makeText(ShowActivity.this,"Error retrieving information from google.",Toast.LENGTH_LONG).show();
                 View progressBar = findViewById(R.id.progress_bar);
                 if (progressBar.getVisibility() == View.VISIBLE){
                     progressBar.setVisibility(View.INVISIBLE);
                 }

             }
           });
            restReq.executeFullSearch(latLong);

    }
    //Options Menu initialization and setting
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_settings:
                Intent i = new Intent(ShowActivity.this, PreferencesActivity.class);
                i.putExtra("from_show",true);
                startActivity(i);
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }
    //Choose random restaurant from the placeIds retrieved and request details.
    private void getRestaurantDetails(){
        View progressBar = findViewById(R.id.progress_bar);
        if (progressBar.getVisibility() == View.INVISIBLE){
            progressBar.setVisibility(View.VISIBLE);
        }
        int i = rn.nextInt(placeIds.size());
        if (placeIds.size() ==0 ){
            Toast.makeText(getApplicationContext(),"No Restaurant fitting criteria found. Consider changing settings.",Toast.LENGTH_SHORT).show();
        }
        else{
            if(placeIds.size()<4){
                String text = "Only "+placeIds.size()+" restaurants fitting your criteria";
                Toast toast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT);
                toast.show();
            }
            restReq = new RestaurantRequest(getApplicationContext(), new VolleyDetailCallback() {
                @Override
                public void onSuccess(Restaurant restaurant) {
                    if (restaurant !=  null){
                        currentRest = restaurant;
                        getPhoto(currentRest.photo);
                    }else{
                        Toast.makeText(getApplicationContext(),"Error retrieving information. Try again.",Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure() {
                    Toast.makeText(ShowActivity.this,"Error retrieving restaurant. Try again.",Toast.LENGTH_LONG);
                }
            });
            restReq.executeDetailSearch(placeIds.get(i));
        }
    }
    //Set values of Views.
    private void setupView(){
        setUpButtons();

        TextView name = (TextView) findViewById(R.id.name);
        TextView address = (TextView) findViewById(R.id.address);

        RatingBar priceBar = (RatingBar) findViewById(R.id.priceRatingBar);
        RatingBar reviewRatingBar = (RatingBar) findViewById(R.id.reviewRatingBar);

        name.setText(currentRest.name);
        address.setText(currentRest.address);

        if(!currentRest.rating.equals("")){
            Float starRating = Float.parseFloat(currentRest.rating);
            reviewRatingBar.setRating(starRating);
            reviewRatingBar.setVisibility(View.VISIBLE);
        }
        if(!currentRest.price.equals("")){
            Float starPrice = Float.parseFloat(currentRest.price);
            priceBar.setRating(starPrice);
            priceBar.setVisibility(View.VISIBLE);
        }
        View progressBar = findViewById(R.id.progress_bar);
        if (progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }    }
    //Initialize and set OnclickListeners
    private void setUpButtons(){
        ImageButton callButton = (ImageButton) findViewById(R.id.call);
        ImageButton openWebsiteButton = (ImageButton) findViewById((R.id.website));
        ImageButton directionsButton = (ImageButton) findViewById(R.id.directions);
        Button searchButton = (Button) findViewById(R.id.search_again);


        callButton.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                checkAndRequestCallPermissions();
            }
        });
        final String tmpName = currentRest.name;
        directionsButton.setOnClickListener(new ImageButton.OnClickListener(){

            public void onClick(View v){
                String url = "https://www.google.com/maps/dir/?api=1&destination=";
                url = url+tmpName.replaceAll(" ","+");
                Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                startActivity(i);
            }
        });
        openWebsiteButton.setOnClickListener(new ImageButton.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(currentRest.website));
                startActivity(i);
            }
        });
        searchButton.setOnClickListener(new Button.OnClickListener() {
          public void onClick(View v) {
                getRestaurantDetails();
            }
         });
    }

    private void callNumber(){
        String number = "tel:"+this.currentRest;
        startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse(number)));
    }


    //Retrieve photo given reference string from google.
    //Then set as image source and call setupView()
    private void getPhoto(String ref){
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    String url = "https://maps.googleapis.com/maps/api/place/photo?";
                    url = url+"maxwidth=200&maxheight=200&photoreference="+params[0]+"&key="+googleApiKey;
                    InputStream in = new URL(url).openStream();
                    imgSrc = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.d(TAG,e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                ImageView photo = (ImageView) findViewById(R.id.photo);
                if (imgSrc != null){
                    photo.setImageBitmap(imgSrc);
                }else{
                    photo.setImageResource(R.drawable.no_image_available);
                }
                setupView();

            }

        }.execute(ref);
    }
    //Check permissions. If needed ask, else call
    private void checkAndRequestCallPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        if (permissionCheck != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }else{
            callNumber();
        }
    }

    //Ask for permission and call if necessary
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    callNumber();


                } else {
                    Toast.makeText(this, "Cannot make call without your permission", Toast.LENGTH_SHORT).show();
                    //TODO: Notify permissions needed
                }
            }
        }
    }
}





