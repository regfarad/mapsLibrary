package com.itshareplus.googlemapProjet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.itshareplus.googlemapdemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Modules.AmenimapsItem;
import Modules.AmenitiesAtm;
import Modules.AmenitiesBank;
import Modules.AmenitiesParking;
import Modules.AmenitiesToilet;
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

import static android.support.v7.appcompat.R.id.info;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private Button btnMeteo;
    public String SpinnerValue;
    //public static ArrayList<AmenimapsItem> amenimapsData = new ArrayList<>();
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private double lat;
    private double lng;
    public static String curLocality;
    public static AmenimapsItem[] dataAmenities = new AmenimapsItem[10];
    private Geocoder geocoder;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this);
        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnMeteo = (Button) findViewById(R.id.btnMeteo);
        final Spinner amenimapSpinner = (Spinner) findViewById(R.id.spinnerAmenimaps);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        btnMeteo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MeteoActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.choicelist, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amenimapSpinner.setAdapter(adapter);
        amenimapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //JSONAmenimapsTask task = new JSONAmenimapsTask();
                String url_adress;

                if (pos > 0) {
                    SpinnerValue = amenimapSpinner.getSelectedItem().toString();
                    //url_adress = "http://amenimaps.com/amenimapi.php?amenity=" + SpinnerValue + "&mylat=50.8504500&mylon=4.3487800&mode=json&name=ruky91&key=cfee105e7cddd0a5563057b9a547dcc5";
                    //task.execute(new String[]{url_adress});
                    switch (SpinnerValue) {
                        case "toilette" :
                            try {
                                addMarkerToilet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "atm"  :
                            try {
                                addMarkerAtm();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "banque"   :
                            try {
                                addMarkerBanque();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "parking"  :
                            try {
                                addMarkerParking();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Veuillez choisir un objet valide...";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private String getAdresseLocation (double latG, double lngG) throws IOException {
        StringBuffer info = null;
        List<Address> addresses = null;
        Address address;
        String adressText = "";
        addresses = geocoder.getFromLocation(latG,lngG, 1);
        address = addresses.get(0);
        adressText = String.format("%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getLocality());
        info = new StringBuffer(adressText);
        return info.toString();
    }

    private void addMarkerToilet() throws IOException {
        AmenitiesToilet amenitiesT = new AmenitiesToilet();
        for (int i = 0; i<amenitiesT.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .title("WC Public : "+getAdresseLocation(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .position(new LatLng(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.toilet)));
        }

    }

    private void addMarkerAtm() throws IOException {
        AmenitiesAtm amenitiesT = new AmenitiesAtm();
        for (int i = 0; i<amenitiesT.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .title("ATM : " +getAdresseLocation(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .position(new LatLng(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.atm)));
        }

    }

    private void addMarkerBanque() throws IOException {
        AmenitiesBank amenitiesT = new AmenitiesBank();
        for (int i = 0; i<amenitiesT.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .title("Banque : " +getAdresseLocation(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .position(new LatLng(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bank1)));
        }

    }

    private void addMarkerParking() throws IOException {
        AmenitiesParking amenitiesT = new AmenitiesParking();
        for (int i = 0; i<amenitiesT.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .title("Parking : " +getAdresseLocation(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .position(new LatLng(amenitiesT.get(i).getLat(), amenitiesT.get(i).getLng()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking)));
        }

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

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class JSONAmenimapsTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            AmenimapsItem[] tab = new AmenimapsItem[10];

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String amenimapsData = buffer.toString();
                Log.d("Debug", amenimapsData);
                JSONObject parentObject = new JSONObject(amenimapsData);
                JSONArray parentArray = parentObject.getJSONArray("markers");
                JSONObject finalObject;
                double lat, lng;
                for (int i = 0; i < parentArray.length(); i++) {
                    finalObject = parentArray.getJSONObject(i);
                    lat = finalObject.getDouble("latitude");
                    lng = finalObject.getDouble("longitude");
                    /*amenimapsData.add(new AmenimapsItem(lat,lng));*/
                    dataAmenities[i] = new AmenimapsItem(lat, lng);
                }
                return amenimapsData;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for(int i = 0; i < dataAmenities.length; i++){
            mMap.addMarker(new MarkerOptions()
                    .title("" + SpinnerValue)
                    .position(new LatLng(dataAmenities[i].getLat(), dataAmenities[i].getLng())));
            }

        }

    }

    private void sendRequest() {

        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer l'adresse d'origine SVP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer l'adresse de destination SVP", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Geocoder geocoder;
        String bestProvider;
        double curLat, curLng;
        List<Address> user = null;

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        bestProvider = lm.getBestProvider(criteria, false);
        Location location = lm.getLastKnownLocation(bestProvider);

        if (location == null) {
            Toast.makeText(this, "Location Not found", Toast.LENGTH_LONG).show();
        } else {
            geocoder = new Geocoder(this);
            try {
                user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                curLat = (double) user.get(0).getLatitude();
                curLng = (double) user.get(0).getLongitude();
                this.setLat(curLat);
                this.setLng(curLng);
                curLocality = user.get(0).getLocality();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mMap = googleMap;
        LatLng hcmus = new LatLng(this.getLat(), this.getLng());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 10));
        try {
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Votre position : " +getAdresseLocation(this.getLat(), this.getLng()))
                    .position(hcmus)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        Toast.makeText(this, "Localité actuelle : " + curLocality, Toast.LENGTH_LONG).show();
        System.out.println(this.getLat() +"," +this.getLng());
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Veuillez patientez",
                "Nous cherchons la direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.RED).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

}








