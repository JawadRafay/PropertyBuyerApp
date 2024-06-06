package com.hypenet.realestaterehman.ui.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.HouseAdapter;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.FragmentHomeBinding;
import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.ui.activities.HouseDetailsActivity;
import com.hypenet.realestaterehman.ui.activities.MainActivity;
import com.hypenet.realestaterehman.ui.activities.RegisterActivity;
import com.hypenet.realestaterehman.utils.CommonFeatures;
import com.hypenet.realestaterehman.utils.Constants;
import com.hypenet.realestaterehman.utils.Utils;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    HouseAdapter adapter;
    List<House> data;
    ProgressDialog progressDialog;
    GoogleMap mGoogleMap;
    Activity activity;
    FragmentHomeBinding binding;
    int selected_marked = -1;
    RoomDatabase roomDatabase;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    Marker currentMaker;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home,container,false);
        init();
        setListeners();
        getProperties();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public void init(){
        Places.initialize(activity, "AIzaSyCnkwwTsjN0opFb3sjxjt8aTSzKgGDSf5U");
        placesClient = Places.createClient(activity);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        roomDatabase = RoomDatabase.getInstance(activity);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                mGoogleMap = mMap;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(CommonFeatures.location.getLatitude(), CommonFeatures.location.getLongitude()),16f));
                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.getTag() != null){
                            selected_marked =  Integer.parseInt(marker.getTag().toString());
                            House house = data.get(selected_marked);

                            if (house.getSeller().getKyc_status().equalsIgnoreCase("Verified")){
                                binding.verified.setVisibility(View.VISIBLE);
                            }else {
                                binding.verified.setVisibility(View.GONE);
                            }
                            binding.name.setText(house.getTitle());
                            binding.isAvailable.setText(house.getSell_type());
                            binding.address.setText(house.getArea_name());
                            Glide.with(activity.getApplicationContext()).load(house.getProperty_images().get(0).getImage()).into(binding.image);
                            double dis = Utils.distance(Double.parseDouble(house.getLatitude()),Double.parseDouble(house.getLongitude()));
                            binding.distance.setText(String.format("%.1f",dis));
                            if (roomDatabase.houseDao().checkFavourite(house.getId()) == null)
                                binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.bookmark,null));
                            else
                                binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.bookmark_selected,null));

                            binding.bookMark.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (roomDatabase.houseDao().checkFavourite(house.getId()) == null){
                                        roomDatabase.houseDao().insert(house);
                                        binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.bookmark_selected,null));
                                    } else {
                                        roomDatabase.houseDao().delete(house);
                                        binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.bookmark,null));
                                    }
                                }
                            });

                            binding.houseItem.setAlpha(0f);
                            binding.houseItem.setVisibility(View.VISIBLE);
                            binding.houseItem.animate().alpha(1f).setDuration(1000);
                        }
                        return true;
                    }
                });
            }
        });
        data = new ArrayList<>();
        adapter = new HouseAdapter(activity,data);
        adapter.setOnItemClickListener(new HouseAdapter.OnItemClickListener() {
            @Override
            public void onClick(House house) {
                startActivity(new Intent(activity, HouseDetailsActivity.class).putExtra("data",house));
            }
        });
        binding.recycler.setAdapter(adapter);

        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                activity.startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    binding.searchBar.disableSearch();
                    binding.searchBar.clearSuggestions();
                }
            }
        });

        binding.searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                binding.searchBar.updateLastSuggestions(suggestionsList);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!binding.searchBar.isSuggestionsVisible()) {
                                            binding.searchBar.showSuggestionsList();
                                        }
                                    }
                                }, 1000);
                            }
                        } else {
                            Log.i(TAG, "prediction fetching task unSuccessful "+task.getException().getMessage());
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = binding.searchBar.getLastSuggestions().get(position).toString();
                binding.searchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.searchBar.clearSuggestions();
                    }
                }, 1000);

                InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(binding.searchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                            @Override
                            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                Place place = fetchPlaceResponse.getPlace();
                                Log.i(TAG, "place found " + place.getName() + place.getAddress());
                                LatLng latLng = place.getLatLng();
                                if (latLng != null) {
                                    if (mGoogleMap != null){
                                        if (currentMaker != null){
                                            currentMaker.remove();
                                            currentMaker = null;
                                        }
                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.position(latLng);

                                        currentMaker = mGoogleMap.addMarker(markerOptions);
                                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    apiException.printStackTrace();
                                    int statusCode = apiException.getStatusCode();
                                    Log.i(TAG, "place not found" + e.getMessage());
                                    Log.i(TAG, "status code : " + statusCode);
                                }
                            }
                        });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
            }
        });
    }

    public void setListeners(){
        binding.houseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, HouseDetailsActivity.class).putExtra("data",data.get(selected_marked)));
            }
        });
        binding.mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleMap != null){
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                binding.houseItem.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.GONE);
                binding.mapLayout.setVisibility(View.VISIBLE);
                setActive(R.id.mapView);
            }
        });
        binding.earthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleMap != null){
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                binding.houseItem.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.GONE);
                binding.mapLayout.setVisibility(View.VISIBLE);

                setActive(R.id.earthView);
            }
        });
        binding.gridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.houseItem.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.VISIBLE);
                binding.mapLayout.setVisibility(View.GONE);
                setActive(R.id.gridView);
            }
        });
    }

    public void setActive(int id){
        binding.mapView.setTextColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.mapView ? R.color.white : R.color.black,null));
        binding.mapView.setBackgroundColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.mapView ? R.color.blue : R.color.white,null));

        binding.earthView.setTextColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.earthView ? R.color.white : R.color.black,null));
        binding.earthView.setBackgroundColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.earthView ? R.color.blue : R.color.white,null));

        binding.gridView.setTextColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.gridView ? R.color.white : R.color.black,null));
        binding.gridView.setBackgroundColor(ResourcesCompat.getColor(activity.getResources(),id == R.id.gridView ? R.color.blue : R.color.white,null));
    }

    public void getProperties(){
        progressDialog.show();
        Call<ApiResponse<List<House>>> call = RetrofitClint.getInstance().getApi().get_properties();
        call.enqueue(new Callback<ApiResponse<List<House>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<House>>> call, Response<ApiResponse<List<House>>> response) {
                progressDialog.dismiss();
                if (response.body() !=  null){
                    Log.d(TAG, "onResponse:home "+new Gson().toJson(response.body()));
                    if (response.body().getMessage().equals("success")){
                       data.addAll(response.body().getData());
                       adapter.notifyDataSetChanged();
                        Constants.data.addAll(response.body().getData());
                        for (int i = 0; i < data.size(); i++) {
                            LatLng latLng = new LatLng(Double.parseDouble(data.get(i).getLatitude()), Double.parseDouble(data.get(i).getLongitude()));
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);

                            markerOptions.icon(data.get(i).getType().equalsIgnoreCase("House") ? getHouseMarkerIcon(activity, formatToK(Integer.parseInt(data.get(i).getPrice()))) : getPlotMarkerIcon(activity, formatToK(Integer.parseInt(data.get(i).getPrice()))));
                            mGoogleMap.addMarker(markerOptions).setTag(String.valueOf(i));
                        }
                    }else{
                        Toast.makeText(activity, "Something went wrong. Try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(activity, "Server Error! please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<House>>> call, Throwable t) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(activity, "Check you internet connection and try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String formatToK(int number) {
        if (number < 1000) {
            return String.valueOf(number); // No conversion needed for numbers less than 1000
        }

        int thousands = number / 1000; // Get the whole thousands
        int remainder = number % 1000; // Remainder after dividing by 1000

        if (remainder == 0) {
            return thousands + "k"; // If no remainder, return whole thousands with "k"
        } else {
            return thousands + "." + (remainder / 100) + "k"; // Include one decimal place if there's a remainder
        }
    }

    private BitmapDescriptor getHouseMarkerIcon(Context context, String text) {
        int markerSize = 200; // Adjust the size of the marker as needed
        int textSize = 30;   // Adjust the size of the text as needed

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#26aefc")); // Set the color of the circle
        paint.setStyle(Paint.Style.FILL);

        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.house_marker);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getPlotMarkerIcon(Context context, String text) {
        int markerSize = 200; // Adjust the size of the marker as needed
        int textSize = 30;   // Adjust the size of the text as needed

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#4f4ea5")); // Set the color of the circle
        paint.setStyle(Paint.Style.FILL);

        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.plot_marker);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getPropertyMarkerIcon(Context context, String text) {
        int markerSize = 100; // Adjust the size of the marker as needed
        int textSize = 30;   // Adjust the size of the text as needed

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#3cc989")); // Set the color of the circle
        paint.setStyle(Paint.Style.FILL);

        Bitmap bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(markerSize / 2, markerSize / 2, markerSize / 2, paint);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.house_marker);
        if (drawable != null) {
            int drawableSize = markerSize / 2;
            drawable.setBounds((markerSize - drawableSize) / 2, (markerSize - drawableSize) / 2,
                    (markerSize + drawableSize) / 2, (markerSize + drawableSize) / 2);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
