package elapse.choosemyfood;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Class to async get restaurant IDs or restaurant from specific id
 *
 */

public class RestaurantRequest  {
    private static final String googleApiKey = "AIzaSyCcndRN1-FqszyIMNj8m4Goa2C3U5rofqM";
    private static final String TAG = "RestaurantRequest";
    private VolleyIdCallback vIdCallback;
    private VolleyDetailCallback vDetCallback;

    private RequestQueue queue;
    private ArrayList<String> ids;

    protected RestaurantRequest(Context context,VolleyIdCallback vCallback){
        this.vIdCallback = vCallback;
        this.queue = Volley.newRequestQueue(context);
        this.ids = new ArrayList<>();
    }
    protected RestaurantRequest(Context context, VolleyDetailCallback vDetCallback){
        this.vDetCallback = vDetCallback;
        this.queue = Volley.newRequestQueue(context);
        this.ids = new ArrayList<>();
    }
    protected void executeDetailSearch(String placeId){
        getRestaurantDetails(placeId);
    }
    protected void executeFullSearch(String latLng){
        getPlaceIds(buildRadarUrl());
    }


    private String buildSearchUrl(){
        PreferencesSingleton options = PreferencesSingleton.getInstance();
        ///Necessary Settings
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+options.getLocation().getLatitude()+","+options.getLocation().getLongitude()+
                "&radius="+options.getRadius()+"&type=restaurant"+"&opennow=true";

        if(options.getKeywords().size()>0){
            url+="&keyword=";
            for(String word : options.getKeywords()){
                url+= word+"+";
            }
        }
        if (!options.getMinPrice().equals("0")){
            url+= "&minPrice="+options.getMinPrice();
        }
        if(!options.getMaxPrice().equals("4")){
            url+="&maxPrice="+options.getMaxPrice();
        }
        return url+"&key="+googleApiKey;
    }

    private String buildRadarUrl(){
        PreferencesSingleton options = PreferencesSingleton.getInstance();
        String base = "https://maps.googleapis.com/maps/api/place/radarsearch/json?";
        base +="location="+options.getLocation().getLatitude()+","+options.getLocation().getLongitude()+
        "&radius="+options.getRadius()+"&type=restaurant"+"&opennow=true";
        if(options.getKeywords().size()>0){
            base+="&keyword=";
            for(String word : options.getKeywords()){
                base+= word+"+";
            }
            base = base.substring(0, base.length()-1);
        }
        if (!options.getMinPrice().equals("0")){
            base+= "&minPrice="+options.getMinPrice();
        }
        if(!options.getMaxPrice().equals("4")){
            base+="&maxPrice="+options.getMaxPrice();
        }
        return base+"&key="+googleApiKey;

    }
    private String buildDetailsUrl(String id){
        return "https://maps.googleapis.com/maps/api/place/details/json?"+"placeid="+
                id+"&key="+googleApiKey;

    }
    //Method: Get a nearby restaurant using location data
    //Parameters: code-> tell whether search coming from MainActivity or ShowActivity,url-> string to send to google places web api

    private void getPlaceIds(final String url){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String status = response.getString("status");
                            if (status.equals("INVALID_REQUEST")){
                                vIdCallback.onFailure();
                                /*
                                Log.d(TAG,"Invalid Request in GetPlaceIds: Retrying");
                                queue.getCache().remove(url);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        getPlaceIds(url);
                                    }
                                }, 1000);
                                */
                            }else if(status.equals("ZERO_RESULTS")){
                                Log.d(TAG,"No Results");
                                ids = new ArrayList<>();
                                vIdCallback.onSuccess(ids);

                            }
                            else{
                                JSONArray results = response.getJSONArray("results");
                                String tmpId;
                                for(int i = 0; i < results.length();i++ ){
                                    tmpId = results.getJSONObject(i).getString("place_id");
                                    ids.add(tmpId);
                                }
                                //If no pageToken, exception thrown;
                                String pageToken =response.getString("next_page_token");
                                String newUrl ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken="+pageToken+"&key="+googleApiKey;
                                getPlaceIds(newUrl);
                            }


                        }catch(JSONException e){
                            Log.d(TAG,"Completed PlaceIdSearch on URL: "+url+"\n found "+ids.size()+"restaurants");
                            vIdCallback.onSuccess(ids);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        vIdCallback.onFailure();
                    }
                });
        queue.add(jsObjRequest);
    }

    //Method: Get details of a restaurant given the place id and starts ShowActivity.
    private  void getRestaurantDetails(String id){
        String url = buildDetailsUrl(id);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getString("status").equals("OK")){
                                Restaurant restaurant = new Restaurant(response.getJSONObject("result"));
                                Log.d(TAG,"Completed Restaurant Detail Search");
                                vDetCallback.onSuccess(restaurant);
                            }

                        }catch(JSONException e){
                            Log.d(TAG,e.getMessage());

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        vDetCallback.onFailure();
                    }
                });
        queue.add(jsObjRequest);
    }

}
