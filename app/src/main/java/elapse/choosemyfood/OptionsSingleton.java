package elapse.choosemyfood;

import android.location.Location;

/**
 * Created by Joshua on 9/10/2017.
 */

public class OptionsSingleton  {
    private String radius = "25000"; //Max 50000
    private String minPrice = "0";
    private String maxPrice = "4";
    private String[] keywords ={};
    private Location location;


    private static OptionsSingleton INSTANCE = new OptionsSingleton();

    private OptionsSingleton() {};

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
}