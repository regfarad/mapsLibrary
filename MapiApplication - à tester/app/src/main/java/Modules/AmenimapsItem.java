package Modules;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Cédric on 16-12-16.
 */

public class AmenimapsItem {
    public double lat;
    public double lng;

    public AmenimapsItem(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
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
