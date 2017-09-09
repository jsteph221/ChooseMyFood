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

public class Restaurant implements Parcelable {
    public String name;
    public String address;
    public String phone;
    public String website ;
    public String location;
    public String price;
    public String rating;
    public String photo;

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


    }
    public String getAttribute(JSONObject param, String attr){
        try{
            String res = param.getString(attr);
            return res;

        }catch (JSONException e){
            return "";
        }

    }
    public Restaurant(Parcel source) {
        name = source.readString();
        address = source.readString();
        phone = source.readString();
        website = source.readString();
        location = source.readString();
        price = source.readString();
        rating = source.readString();
        photo = source.readString();

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(website);
        dest.writeString(location);
        dest.writeString(price);
        dest.writeString(rating);
        dest.writeString(photo);
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }

        @Override
        public Restaurant createFromParcel(Parcel source) {
            return new Restaurant(source);
        }
    };

}
