package com.itshareplus.googlemapProjet;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

import Modules.AmenimapsItem;

import static com.itshareplus.googlemapProjet.JSONWeatherParser.getObject;
import static java.lang.reflect.Array.getFloat;

/**
 * Created by Cédric on 16-12-16.
 */

public class JSONAmenimapsParser {
    public static AmenimapsItem getItems(String data) throws JSONException {
        AmenimapsItem items = new AmenimapsItem();
        JSONObject jObj = new JSONObject(data);
        items.setLng(getFloat("longitude", jObj));
        items.setLat(getFloat("latitude", jObj));

        return items;
    }

    public static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float  getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int  getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }
}
