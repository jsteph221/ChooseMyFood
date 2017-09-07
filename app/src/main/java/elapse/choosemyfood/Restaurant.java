package elapse.choosemyfood;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import static elapse.choosemyfood.MainActivity.getJSONObjectFromURL;

/**
 * Created by Joshua on 9/6/2017.
 */

public class Restaurant extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Restaurant";
    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("reqUrl");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
        new AsyncRestaurantReq().execute(url,null,null);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(Restaurant.this,"Error connecting to Google play services",Toast.LENGTH_LONG).show();

    }
    class AsyncRestaurantReq extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            //display progress dialog.
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                JSONObject resp = getJSONObjectFromURL(params[0]);
                JSONArray results = resp.getJSONArray("results");
                Random rn = new Random();
                int i  =  rn.nextInt(results.length());
                String placeId = results.getJSONObject(i).getString("place_id");
                return placeId;


            }catch(IOException e){
                Toast.makeText(Restaurant.this, "Error retrieving restaurant. Try Again.", Toast.LENGTH_LONG).show();
                return null;

            }catch (JSONException e){
                Toast.makeText(Restaurant.this, "Error Parsing Json. Try Again.", Toast.LENGTH_LONG).show();
                return null;
            }

        }
        @Override
        protected void onPostExecute(String place_id) {
            if(place_id != null){
                Places.GeoDataApi.getPlaceById(mGoogleApiClient,place_id)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceBuffer places) {
                                if(places.getStatus().isSuccess()){
                                    final CharSequence thirdPartyAttributions =
                                            places.getAttributions();
                                    Log.d(TAG,"Found Location attr");
                                    Toast.makeText(Restaurant.this, "FOUND A RESTAURANT.", Toast.LENGTH_LONG).show();
                                    //TODO: Show restaurant
                                    //

                                }else{
                                    Log.d(TAG,"Error finding place by ID");
                                }
                                places.release();
                            }
                        });
            }else{
                Toast.makeText(Restaurant.this,"Places error",Toast.LENGTH_LONG).show();
            }


        }

    }

}



