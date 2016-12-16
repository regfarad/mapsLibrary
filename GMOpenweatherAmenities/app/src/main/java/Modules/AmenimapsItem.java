package Modules;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Cédric on 16-12-16.
 */

public class AmenimapsItem {
    public double lat;
    public double lng;
    public String itemName;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }




}
