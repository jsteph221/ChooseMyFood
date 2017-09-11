package elapse.choosemyfood;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Joshua on 9/7/2017.
 */

public class Restaurant {
    public String name;
    public String address;
    public String phone;
    public String website ;
    public String location;
    public String price;
    public String rating;
    public String photo;
    public String closesAt;

    public Restaurant(JSONObject param){
            this.name = getAttribute(param,"name");
            this.address = getAttribute(param,"formatted_address");
            this.phone = getAttribute(param,"formatted_phone_number");

            this.website = getAttribute(param,"website");
            this.price = getAttribute(param,"price_level");
            this.rating = getAttribute(param,"rating");
            try{
                this.photo = param.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            }catch (JSONException e){
                this.photo = null;
            }
            try{
                this.location = param.getJSONObject("location").getString("Geometry");
            }catch(JSONException e){
                this.location = "";
            }
            this.closesAt = getAttribute(param,"close");


    }
    public String getAttribute(JSONObject param, String attr){
        try{
            String res = param.getString(attr);
            return res;

        }catch (JSONException e){
            return "";
        }

    }
}
