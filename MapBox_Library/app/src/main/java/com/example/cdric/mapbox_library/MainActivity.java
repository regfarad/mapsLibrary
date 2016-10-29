package com.example.cdric.mapbox_library;

import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import android.app.Activity;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import android.graphics.Color;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute = null;
    private Button btnFindPath;
    private EditText OriginAdress;
    private EditText DestinationAdress;
    final Position origin = Position.fromCoordinates(-3.588098, 37.176164);
    final Position destination = Position.fromCoordinates(-3.601845, 37.184080);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        btnFindPath = (Button) findViewById(R.id.SearchButton);
        OriginAdress = (EditText) findViewById(R.id.adressOrigin);
        DestinationAdress = (EditText) findViewById(R.id.adressDestination);

        // Create a mapView qui est l'objet map qui est affichée (l'image de la map)
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // C'est ici qu'on doit appeller la méthode qui géolocalise + qui met un marqueur spécifique
                // Créer l'objet mapbox qui est l'objet qui sert à mettre des icone, inscription, etc. Sur la map.
                map = mapboxMap;
                geolocalise();
                // Créer et affiche les 2 marqueur sur la map
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
                        .title("Vous êtes ici")
                        .snippet(""));
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(destination.getLatitude(), destination.getLongitude()))
                        .title("Destination")
                        .snippet(""));

                map.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(origin.getLatitude(), origin.getLongitude()))
                        .zoom(16)
                        .build());

                // Appel le méthode getRoute et catch l'erreur s'il y en a une
                try {
                    getRoute(origin, destination);
                } catch (ServicesException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Méthode qui va calculer l'itinéraire entre 2 point
    public void getRoute(Position origin, Position destination) throws ServicesException {
        // Créer l'objet MapboxDirections qui est l'objet qui va calculer la map
        // NOTE: on peux changer dans .setProfile si l'utilisateur est en voiture, velo, a pied, etc.
        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                .setAccessToken(this.getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                }

                // C'est ici qu'on récupère les infos concernant la distance à parcourir etc.
                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(MainActivity.this, "Route is " +  currentRoute.getDistance() + " meters long.", Toast.LENGTH_SHORT).show();

                // appelle la méthode qui trace la ligne sur la map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode qui trace la ligne sur la map entre 2 point
    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }
    public void geolocalise(){
        // on géolocalise l'utilisateur
    }

    public void getDestination(){
        // On transforme la position donnée dans le champ textview en LatLng
    }

    public void getDestination(String position){
        // Surcharge de la méthode pour renvoyer à l'appli GPS les coordonnées LatLng
    }

    public void getActualPosition(){
        // On transforme la position donnée dans le champs textview en LatLng
    }

        @Override
        public void onResume(){
            super.onResume();
            mapView.onResume();
        }

        @Override
        public void onPause(){
            super.onPause();
            mapView.onPause();
        }

        @Override
        public void onSaveInstanceState(Bundle outState){
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }

        @Override
        public void onLowMemory(){
            super.onLowMemory();
            mapView.onLowMemory();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mapView.onDestroy();
        }
}
