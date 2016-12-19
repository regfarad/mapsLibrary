package Modules;

import java.util.ArrayList;

/**
 * Created by regfa on 19-12-16.
 */

public class Amenities {
    private double lat;
    private double lng;
    private String infoFauteuilRoulant;

    public Amenities(double lat, double lng, String infoFauteuilRoulant) {
        this.lat = lat;
        this.lng = lng;
        this.infoFauteuilRoulant = infoFauteuilRoulant;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getInfoFauteuilRoulant() {
        return infoFauteuilRoulant;
    }

}
