package com.itshareplus.googlemapProjet;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Cédric on 16-12-16.
 */

public class AmenimapsHttpClient {
    private static String BASE_URL = "http://amenimaps.com/amenimapi.php?amenity=";
    private static String username = "ruky91";
    private static String amenimaps_KEY = "cfee105e7cddd0a5563057b9a547dcc5";

    public String getAmenimapsData(String itemValue, double lat, double lng) {
        HttpURLConnection con = null ;
        InputStream is = null;

        try {
            con = (HttpURLConnection) ( new URL(BASE_URL + itemValue +"&mylat=" + lat +"&mylon=" + lng +"&mode=json&name=" + username +"&key=" + amenimaps_KEY)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while (  (line = br.readLine()) != null )
                buffer.append(line + "\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try { is.close(); } catch(Throwable t) {}
            try { con.disconnect(); } catch(Throwable t) {}
        }

        return null;

    }
}
