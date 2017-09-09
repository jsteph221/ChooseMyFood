package elapse.choosemyfood;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.InputStream;
import java.net.URL;

import elapse.choosemyfood.Restaurant;

import static android.R.attr.dial;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * Created by Joshua on 9/6/2017.
 */

public class ShowActivity extends AppCompatActivity{
    private static final int  MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";


    private Restaurant restaurant;
    private static final String TAG = "ShowActivity";

    private String name;
    private String address;
    private String phone;
    private Bitmap imgSrc;


    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        restaurant = (Restaurant) getIntent().getParcelableExtra("restaurantAttrs");
        setContentView(R.layout.activity_restaurant);
        setupRestaurant(restaurant);
        setupView(restaurant);

    }

    private void setupRestaurant(Restaurant r){
        this.name = r.name;
        this.address = r.address;
        this.phone = r.phone;
        getPhoto(r.photo);
    }

    private void setupView(Restaurant restaurant){
        TextView name = (TextView) findViewById(R.id.name);
        TextView address = (TextView) findViewById(R.id.address);
        TextView phone = (TextView) findViewById(R.id.phone);
        ImageView photo = (ImageView) findViewById(R.id.photo);
        ImageButton call = (ImageButton) findViewById(R.id.call);

        name.setText(this.name);
        address.setText(this.address);
        phone.setText(this.phone);
        call.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                checkAndRequestCallPermissions();
            }
        });
    }

    private void callNumber(){
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(this.phone)));
    }

    private void searchForRestaurant(){

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
                if (imgSrc != null){
                    ImageView photo = (ImageView) findViewById(R.id.photo);
                    photo.setImageBitmap(imgSrc);
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



