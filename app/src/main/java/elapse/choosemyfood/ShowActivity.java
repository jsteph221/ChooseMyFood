package elapse.choosemyfood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * Created by Joshua on 9/6/2017.
 */

public class ShowActivity extends AppCompatActivity{
    private static final int  MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";


    private static final String TAG = "ShowActivity";

    private String name;
    private String address;
    private String phone;
    private String websiteUrl;
    private String photoRef;
    private Bitmap imgSrc;

    private String latLong;
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rn = new Random();
        latLong = (String) getIntent().getStringExtra("lat_long");
        boolean fullSearch = getIntent().getBooleanExtra("full_search",true);
        if (fullSearch){
            restReq = new RestaurantRequest(getApplicationContext(), new VolleyIdCallback() {
                @Override
                public void onSuccess(ArrayList<String> ids) {
                    placeIds = ids;
                    getRestaurantDetails(placeIds.get(rn.nextInt(placeIds.size())));
                }
            });
            restReq.executeFullSearch(latLong);
        }else{
            int i = rn.nextInt(placeIds.size());
            getRestaurantDetails(placeIds.get(i));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_settings);
        menuItem.setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }
    private void getRestaurantDetails(String placeId){
        restReq = new RestaurantRequest(getApplicationContext(), new VolleyDetailCallback() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                if (placeIds.size() < 5){
                    String text = "Only "+placeIds.size()+" choices. Consider changing settings for more options";
                    Toast toast = Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT);
                    toast.show();
                }
                setupRestaurant(restaurant);
                setupView();
            }
        });
        restReq.executeDetailSearch(placeId);
    }

    private void setupRestaurant(Restaurant r){
        this.name = r.name;
        this.address = r.address;
        this.phone = r.phone;
        this.photoRef = r.photo;
        this.websiteUrl = r.website;
    }

    private void setupView(){
        getPhoto(this.photoRef);
        //Button searchButton = (Button) findViewById(R.id.search_again);
        //searchButton.setOnClickListener(new Button.OnClickListener() {
          //  public void onClick(View v) {
        //        int i= rn.nextInt(placeIds.size());
        //        getRestaurantDetails(placeIds.get(i));
        //    }
       // });
        TextView name = (TextView) findViewById(R.id.name);
        TextView address = (TextView) findViewById(R.id.address);
        ImageButton callButton = (ImageButton) findViewById(R.id.call);
        ImageButton openWebsiteButton = (ImageButton) findViewById((R.id.website));
        ImageButton directionsButton = (ImageButton) findViewById(R.id.website);

        name.setText(this.name);
        address.setText(this.address);
        callButton.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                checkAndRequestCallPermissions();
            }
        });
        final String tmpName = this.name;
        openWebsiteButton.setOnClickListener(new ImageButton.OnClickListener(){

            public void onClick(View v){
                String url = "https://www.google.com/maps/dir/?api=1&destination=";
                url = url+tmpName.replaceAll(" ","+");
                Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                startActivity(i);
            }
        });
        directionsButton.setOnClickListener(new ImageButton.OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(websiteUrl));
                startActivity(i);
            }
        });
        findViewById(R.id.progress_bar).setVisibility(View.GONE);


    }

    private void callNumber(){
        String number = "tel:"+this.phone;
        startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse(number)));
    }



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
                    // log error
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

            }

        }.execute(ref);
    }
    private void checkAndRequestCallPermissions(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        if (permissionCheck != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }else{
            callNumber();
        }
    }
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





