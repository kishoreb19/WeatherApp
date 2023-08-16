package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;

import android.util.Log;

import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    ScrollView scrollView;
    ProgressBar progressBar;
    TextView city, city_address, temp, weather_state, weather_desc, min_temp, feels_like, max_temp,
            weather_humidity, weather_pressure, weather_visibility, wind_speed, wind_degree,
            weather_cloud, sunrise, sunset;
    ImageView map_img, weather_img;
    LinearLayout mainL, header1, temp_details, div1, div2, div3, div4, div5;
    //String latitude, longitude;
    LocationManager locationManager;

    //Fused Location API
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    private static final int REQUEST_LOCATION = 1;

//    private void onGPS() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Enable GPS").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                finish();
//            }
//        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//                manualSearch();
//            }
//        });
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }

//    private void getLocation() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]
//                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//        } else {
//            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//            if (LocationGps != null) {
//                latitude = Double.toString(LocationGps.getLatitude());
//                longitude = Double.toString(LocationGps.getLongitude());
//            } else if (LocationNetwork != null) {
//                latitude = Double.toString(LocationNetwork.getLatitude());
//                longitude = Double.toString(LocationNetwork.getLongitude());
//            } else if (LocationPassive != null) {
//                latitude = Double.toString(LocationPassive.getLatitude());
//                longitude = Double.toString(LocationPassive.getLongitude());
//            } else {
//                Toast.makeText(this, "Error Getting Location", Toast.LENGTH_SHORT).show();
//                manualSearch();
//            }
//        }
//    }

    public String title(String s) {
        char c[] = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (i == 0) {
                c[i] = Character.toUpperCase(c[i]);
            }
            if (c[i] == ' ') {
                if (i + 1 <= c.length - 1) {
                    c[i + 1] = Character.toUpperCase(c[i + 1]);
                }
            }
        }
        return new String(c);
    }

    public String toFahrenheit(int c) {
        int f = (((c * 9)) / 5) + 32;
        return Integer.toString(f);
    }

    public void manualSearch() {
        Dialog Dialog = new Dialog(MainActivity.this);
        //View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.search_city);
        Dialog.setContentView(R.layout.search_city);
        EditText city_name = (EditText) Dialog.findViewById(R.id.city_name);
        Button search_btn = Dialog.findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillDetails(city_name.getText().toString().strip());
                Dialog.dismiss();
            }
        });
        Dialog.show();
    }

    void fillDetails(String latitude, String longitude) {
        //Calling API
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=e53301e27efa0b66d05045d91b2742d3";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //GeoCoder
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    ArrayList<Address> address = null;
                    try {
                        address=(ArrayList<Address>)geocoder.getFromLocation(Double.parseDouble(latitude),Double.parseDouble(longitude),1);
                        //address = (ArrayList<Address>) geocoder.getFromLocation(response.getJSONObject("coord").getDouble("lat"), response.getJSONObject("coord").getDouble("lon"), 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    city_address.setText(address.get(0).getAddressLine(0));
                    city.setText(response.getString("name"));
                    temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp") - 273) + "°C");

                    //Change colour
                    int tempD = response.getJSONObject("main").getInt("temp") - 273;
                    if( 30<tempD &&tempD<40){
                        temp_details.setBackgroundResource(R.drawable.orange);
                    } else if (20<tempD &&tempD<=30) {
                        temp_details.setBackgroundResource(R.drawable.yellow);
                    } else if (tempD>=40) {
                        temp_details.setBackgroundResource(R.drawable.red);
                    } else if (0<tempD && tempD<=20) {
                        temp_details.setBackgroundResource(R.drawable.circular_box);
                    } else if (tempD<=5) {
                        temp_details.setBackgroundResource(R.drawable.dark_blue);
                    }
                    if(response.getJSONArray("weather").getJSONObject(0).getString("main").equals("Rain")){
                        temp_details.setBackgroundResource(R.drawable.rainy);
                    }

                    final int[] c = {1};
                    temp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (c[0] % 2 != 0) {
                                    temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp") - 273) + "°F");
                                    min_temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp_min") - 273) + "°F");
                                    feels_like.setText(toFahrenheit(response.getJSONObject("main").getInt("feels_like") - 273) + "°F");
                                    max_temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp_max") - 273) + "°F");
                                    c[0]++;
                                } else {
                                    c[0]++;
                                    temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp") - 273) + "°C");
                                    min_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_min") - 273) + "°C");
                                    feels_like.setText(Integer.toString(response.getJSONObject("main").getInt("feels_like") - 273) + "°C");
                                    max_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_max") - 273) + "°C");
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            ;
                        }
                    });
                    String w_state = response.getJSONArray("weather").getJSONObject(0).getString("main");
                    weather_state.setText(w_state);

                    if (title(response.getJSONArray("weather").getJSONObject(0).getString("description")).equals(title(response.getJSONArray("weather").getJSONObject(0).getString("main")))) {
                        weather_desc.setVisibility(View.INVISIBLE);
                    } else {
                        weather_desc.setText(title(response.getJSONArray("weather").getJSONObject(0).getString("description")));
                    }
                    Glide.with(MainActivity.this).load("https://openweathermap.org/img/wn/" + response.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png").into(weather_img);
                    min_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_min") - 273) + "°C");
                    feels_like.setText(Integer.toString(response.getJSONObject("main").getInt("feels_like") - 273) + "°C");
                    max_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_max") - 273) + "°C");
                    weather_humidity.setText(response.getJSONObject("main").getString("humidity") + "%");
                    weather_pressure.setText(response.getJSONObject("main").getString("pressure") + "hPA");
                    weather_visibility.setText(response.getString("visibility") + "km");
                    wind_speed.setText(Integer.toString((int) (response.getJSONObject("wind").getInt("speed") * 3.6)) + "km/h");
                    wind_degree.setText(response.getJSONObject("wind").getString("deg") + "°");
                    weather_cloud.setText(response.getJSONObject("clouds").getString("all") + "%");
                    sunrise.setText(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(response.getJSONObject("sys").getInt("sunrise") * 1000)));
                    sunset.setText(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(response.getJSONObject("sys").getInt("sunset") * 1000)));
                    //Visibility
                    //mainL.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    //Animations
                    temp_details.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha1));
                    map_img.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce));
                    city_address.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
                    div1.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div3.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div4.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div5.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error Fetching Details ", Toast.LENGTH_SHORT).show();

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    void fillDetails(String cityname) {
        //Calling API
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityname + "&appid=e53301e27efa0b66d05045d91b2742d3";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //GeoCoder
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    ArrayList<Address> address = null;
                    try {
                        address = (ArrayList<Address>) geocoder.getFromLocation(response.getJSONObject("coord").getDouble("lat"), response.getJSONObject("coord").getDouble("lon"), 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    city_address.setText(address.get(0).getAddressLine(0));
                    city.setText(response.getString("name"));
                    temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp") - 273) + "°C");
                    final int[] c = {1};
                    temp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (c[0] % 2 != 0) {
                                    temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp") - 273) + "°F");
                                    min_temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp_min") - 273) + "°F");
                                    feels_like.setText(toFahrenheit(response.getJSONObject("main").getInt("feels_like") - 273) + "°F");
                                    max_temp.setText(toFahrenheit(response.getJSONObject("main").getInt("temp_max") - 273) + "°F");
                                    c[0]++;
                                } else {
                                    c[0]++;
                                    temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp") - 273) + "°C");
                                    min_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_min") - 273) + "°C");
                                    feels_like.setText(Integer.toString(response.getJSONObject("main").getInt("feels_like") - 273) + "°C");
                                    max_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_max") - 273) + "°C");
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            ;
                        }
                    });

                    //Change colour
                    int tempD = response.getJSONObject("main").getInt("temp") - 273;
                    if( 30<tempD &&tempD<40){
                        temp_details.setBackgroundResource(R.drawable.orange);
                    } else if (20<tempD &&tempD<=30) {
                        temp_details.setBackgroundResource(R.drawable.yellow);
                    } else if (tempD>=40) {
                        temp_details.setBackgroundResource(R.drawable.red);
                    } else if (0<tempD && tempD<=20) {
                        temp_details.setBackgroundResource(R.drawable.circular_box);
                    } else if (tempD<=5) {
                        temp_details.setBackgroundResource(R.drawable.dark_blue);
                    }
                    if(response.getJSONArray("weather").getJSONObject(0).getString("main").equals("Rain")){
                        temp_details.setBackgroundResource(R.drawable.rainy);
                    }

                    weather_state.setText(response.getJSONArray("weather").getJSONObject(0).getString("main"));
                    if (title(response.getJSONArray("weather").getJSONObject(0).getString("description")).equals(title(response.getJSONArray("weather").getJSONObject(0).getString("main")))) {
                        weather_desc.setVisibility(View.INVISIBLE);
                    } else {
                        weather_desc.setText(title(response.getJSONArray("weather").getJSONObject(0).getString("description")));
                    }
                    Glide.with(MainActivity.this).load("https://openweathermap.org/img/wn/" + response.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png").into(weather_img);
                    min_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_min") - 273) + "°C");
                    feels_like.setText(Integer.toString(response.getJSONObject("main").getInt("feels_like") - 273) + "°C");
                    max_temp.setText(Integer.toString(response.getJSONObject("main").getInt("temp_max") - 273) + "°C");
                    weather_humidity.setText(response.getJSONObject("main").getString("humidity") + "%");
                    weather_pressure.setText(response.getJSONObject("main").getString("pressure") + "hPA");
                    weather_visibility.setText(response.getString("visibility") + "km");
                    wind_speed.setText(Integer.toString((int) (response.getJSONObject("wind").getInt("speed") * 3.6)) + "km/h");
                    wind_degree.setText(response.getJSONObject("wind").getString("deg") + "°");
                    weather_cloud.setText(response.getJSONObject("clouds").getString("all") + "%");
                    sunrise.setText(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(response.getJSONObject("sys").getInt("sunrise") * 1000)));
                    sunset.setText(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(response.getJSONObject("sys").getInt("sunset") * 1000)));
                    //Visibility
                    scrollView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    //mainL.setVisibility(View.VISIBLE);
                    //Animations
                    temp_details.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha1));
                    map_img.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce));
                    city_address.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in));
                    div1.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div3.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div4.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                    div5.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha2));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error Fetching Details ", Toast.LENGTH_SHORT).show();
                //fillDetails();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        context = MainActivity.this;
        //Initialising Views
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);
        mainL = (LinearLayout) findViewById(R.id.mainL);
        header1 = (LinearLayout) findViewById(R.id.header1);
        temp_details = (LinearLayout) findViewById(R.id.temp_details);
        div1 = (LinearLayout) findViewById(R.id.div1);
        div2 = (LinearLayout) findViewById(R.id.div2);
        div3 = (LinearLayout) findViewById(R.id.div3);
        div4 = (LinearLayout) findViewById(R.id.div4);
        div5 = (LinearLayout) findViewById(R.id.div5);
        city = (TextView) findViewById(R.id.city);
        city_address = (TextView) findViewById(R.id.city_address);
        temp = (TextView) findViewById(R.id.temp);
        weather_state = (TextView) findViewById(R.id.weather_state);
        weather_desc = (TextView) findViewById(R.id.weather_desc);
        map_img = (ImageView) findViewById(R.id.map_img);
        weather_img = (ImageView) findViewById(R.id.weather_img);
        min_temp = (TextView) findViewById(R.id.min_temp);
        max_temp = (TextView) findViewById(R.id.max_temp);
        feels_like = (TextView) findViewById(R.id.feels_like);
        weather_humidity = (TextView) findViewById(R.id.weather_humidity);
        weather_pressure = (TextView) findViewById(R.id.weather_pressure);
        weather_visibility = (TextView) findViewById(R.id.weather_visibility);
        wind_speed = (TextView) findViewById(R.id.wind_speed);
        wind_degree = (TextView) findViewById(R.id.wind_degree);
        weather_cloud = (TextView) findViewById(R.id.weather_cloud);
        sunrise = (TextView) findViewById(R.id.sunrise);
        sunset = (TextView) findViewById(R.id.sunset);

//        fillDetails("28.704060","77.102493");


        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualSearch();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkSettingsAndStartLocationUpdates();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //New Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Check Location Permissions
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context
                , Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            checkSettingsAndStartLocationUpdates();
        }

    }
    //Get Latitude & Longitude
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            fusedLocationProviderClient.removeLocationUpdates(this);
            if(locationResult != null && locationResult.getLocations().size()>0){
                int latestLocationIndex = locationResult.getLocations().size()-1;
                Location i = locationResult.getLocations().get(latestLocationIndex);
                String lat  = Double.toString(i.getLatitude());
                String longi = Double.toString(i.getLongitude());
                fillDetails(lat,longi);
            }

        }
    };

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Toast.makeText(MainActivity.this, "All Settings Ok", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //to automatically turn on GPS
                if(e instanceof ResolvableApiException){
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        ((ResolvableApiException) e).startResolutionForResult(MainActivity.this,REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}