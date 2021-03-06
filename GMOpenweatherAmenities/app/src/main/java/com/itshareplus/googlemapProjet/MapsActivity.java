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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    public static double lat;
    public static double lng;
    public static String curLocality;
    public static AmenimapsItem response = new AmenimapsItem();

    /*public double getLat() {
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
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnMeteo = (Button) findViewById(R.id.btnMeteo);
        final Spinner amenimaps = (Spinner) findViewById(R.id.spinnerAmenimaps);
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
        amenimaps.setAdapter(adapter);
        amenimaps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos > 0) {
                    String valueSelected = amenimaps.getSelectedItem().toString();
                    JSONAmenimapsTask task = new JSONAmenimapsTask();
                    task.execute(new String[]{valueSelected});

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

    private class JSONAmenimapsTask extends AsyncTask<String, Void, AmenimapsItem> {

        LatLng test2 = new LatLng(lat, lng);
        double latAmeni = test2.latitude;
        double lngAmeni = test2.longitude;

        @Override
        protected AmenimapsItem doInBackground(String... params) {
            String data = ((new AmenimapsHttpClient()).getAmenimapsData(params[0], latAmeni, lngAmeni));

            try {
                response = JSONAmenimapsParser.getItems(data);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(AmenimapsItem amenimapsItem) {
            super.onPostExecute(amenimapsItem);
            LatLng hcmus = new LatLng(response.getLat(), response.getLng());
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("test")
                    .position(hcmus)));
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
                    //this.setLat(curLat);
                    //this.setLng(curLng);
                    lat = curLat;
                    lng = curLng;
                    curLocality = user.get(0).getLocality();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mMap = googleMap;
            LatLng hcmus = new LatLng(lat, lng);
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








