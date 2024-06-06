package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityNearByPlacesBinding;
import com.hypenet.realestaterehman.utils.CommonFeatures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class NearByPlacesActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityNearByPlacesBinding binding;
    String type, lat, lng,house_type;
    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_near_by_places);
        type = getIntent().getStringExtra("type");
        house_type = getIntent().getStringExtra("house_type");
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        init();
        setListeners();
    }

    public void init() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void setListeners() {
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.icon(house_type.equals("Plot") ? getPlotMarkerIcon() : getHouseMarkerIcon());
        mGoogleMap.addMarker(markerOptions);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15f));
        getNearbyPlaces(location);
    }

    private BitmapDescriptor getHouseMarkerIcon() {
        int markerSize = 200;
        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(NearByPlacesActivity.this, R.drawable.house_marker);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getPlotMarkerIcon() {
        int markerSize = 200;

        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(NearByPlacesActivity.this, R.drawable.plot_marker);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getNearbyPlaces(LatLng location) {
        String apiKey = "AIzaSyCnkwwTsjN0opFb3sjxjt8aTSzKgGDSf5U";
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.latitude + "," + location.longitude +
                "&radius=3000" +
                "&type="+type +
                "&key=" + apiKey;

        // Make a HTTP request to the Places API Web Service
        new FetchPlacesTask().execute(url);
    }

    private class FetchPlacesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

                bufferedReader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray results = jsonObject.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject place = results.getJSONObject(i);
                        String placeName = place.getString("name");
                        JSONObject geometry = place.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                        // Add a marker for each nearby place
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(placeName);
                        markerOptions.icon(type.equals("school") ? getSchoolMarkerIcon() : type.equals("hospital") ? getHospitalMarkerIcon() : getParkMarkerIcon());
                        mGoogleMap.addMarker(markerOptions);
                    }
                    if(results.length() == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(NearByPlacesActivity.this);
                        builder.setMessage("This "+house_type+" don\'t have near by "+type+"s");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        builder.create().show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(NearByPlacesActivity.this, "Error fetching places", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private BitmapDescriptor getParkMarkerIcon() {
        int markerSize = 160;
        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(NearByPlacesActivity.this, R.drawable.park);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getHospitalMarkerIcon() {
        int markerSize = 160;
        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(NearByPlacesActivity.this, R.drawable.hospital);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getSchoolMarkerIcon() {
        int markerSize = 160;
        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(NearByPlacesActivity.this, R.drawable.school);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}