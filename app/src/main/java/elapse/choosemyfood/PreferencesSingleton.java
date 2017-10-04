package elapse.choosemyfood;

import android.location.Location;

import java.util.ArrayList;

/**
 * Singleton to hold global preferences and location.
 */

public class PreferencesSingleton {
    private String radius = "25000";
    private String minPrice = "0";
    private String maxPrice = "4";
    private ArrayList<String>keywords = new ArrayList<String>();
    private Location location;


    private static PreferencesSingleton INSTANCE = new PreferencesSingleton();

    private PreferencesSingleton() {}

    public static PreferencesSingleton getInstance() {
        return(INSTANCE);
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }
    public String getMinPrice(){
        return this.minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList keywords) {
        this.keywords = keywords;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }



}