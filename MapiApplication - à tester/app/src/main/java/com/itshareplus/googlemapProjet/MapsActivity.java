package com.itshareplus.googlemapProjet;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;
import Modules.Weather;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private Button btnMeteo;
    public  String SpinnerValue;
    public static ArrayList<AmenimapsItem> amenimapsData = new ArrayList<>();
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private double lat;
    private double lng;
    public static String curLocality;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                if (pos > 0) {
                    SpinnerValue = amenimapSpinner.getSelectedItem().toString();
                    new JSONAmenimapsTask().execute("http://amenimaps.com/amenimapi.php?amenity="+SpinnerValue+"&mylat=50.8504500&mylon=4.3487800&mode=json&name=ruky91&key=cfee105e7cddd0a5563057b9a547dcc5");


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
    }

    private class JSONAmenimapsTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String AmenimapsData = buffer.toString();
                JSONObject parentObject = new JSONObject(AmenimapsData);
                JSONArray parentArray = parentObject.getJSONArray("markers");
                for(int i = 0; i <= parentArray.length(); i++){
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    double lat = finalObject.getDouble("latitude");
                    double lng = finalObject.getDouble("longitude");
                    amenimapsData.add(new AmenimapsItem(lat,lng));
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if(connection != null){
                    connection.disconnect();
                }
                if(reader != null) {
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        protected void onPostExecute(ArrayList<AmenimapsItem> a) {
            for(int i = 0; i <= a.size(); i++){
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(a.get(i).getLat(), a.get(i).getLng()))
                        .title(""+ SpinnerValue));
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
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Votre position")
                    .position(hcmus)));

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








