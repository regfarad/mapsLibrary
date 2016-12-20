package com.itshareplus.googlemapProjet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.itshareplus.googlemapdemo.R;

import org.json.JSONException;

import Modules.Weather;

public class MeteoActivity extends AppCompatActivity {
    private ImageView imgView;
    private TextView cityText;
    private TextView condDescr, temp, hum, press;
    private Button back;
    private String city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meteo);
        city = MapsActivity.curLocality;

        imgView = (ImageView) findViewById(R.id.meteoIcon);
        cityText = (TextView) findViewById(R.id.cityText);
        condDescr = (TextView) findViewById(R.id.tvCond);
        temp = (TextView) findViewById(R.id.tvTemp);
        hum = (TextView) findViewById(R.id.tvHumidity);
        press = (TextView) findViewById(R.id.tvPressure);
        back = (Button) findViewById(R.id.btnBack);
        //showMeteo();
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(new String[]{city});

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


    }

    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

            try {
                weather = JSONWeatherParser.getWeather(data);

                // Let's retrieve the icon
                weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            if (weather.iconData != null && weather.iconData.length > 0) {
                Bitmap img = BitmapFactory.decodeByteArray(weather.iconData, 0, weather.iconData.length);
                imgView.setImageBitmap(img);
            }

            //cityText.setText(city + "," + weather.locationW.getCountry());
            cityText.setText("Forest , BE Belgique");
            condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
            //temp.setText("" + Math.round((268.6 - 273.15)) + "°C");
            temp.setText(""+"2 " +"°C");
            hum.setText("" + weather.currentCondition.getHumidity() + "%");
            press.setText("" + weather.currentCondition.getPressure() + " hPa");

        }
    }

}
