package com.hypenet.realestaterehman.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.FirebaseDatabase;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.databinding.ActivityMainBinding;
import com.hypenet.realestaterehman.model.User;
import com.hypenet.realestaterehman.model.UserResponse;
import com.hypenet.realestaterehman.ui.fragments.FavouriteFragment;
import com.hypenet.realestaterehman.ui.fragments.ChatFragment;
import com.hypenet.realestaterehman.ui.fragments.HomeFragment;
import com.hypenet.realestaterehman.ui.fragments.ProfileFragment;
import com.hypenet.realestaterehman.utils.CommonFeatures;
import com.hypenet.realestaterehman.utils.PrefManager;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    String[] fragmentTags = {"homeFragment", "chatFragment", "aiBotFragment", "profileFragment"};
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;
    boolean gps_enabled = false;
    boolean isPermissionRequested = false;
    private boolean isDialogueShowing = false;
    private static final int PERMISSION_CODE_LOCATION = 1022;
    PrefManager prefManager;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        init();
        icon(R.id.homeFragment);
        setListeners();
        checkAndRequestLocationPermissions();
    }

    private void init(){
        prefManager = new PrefManager(this);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void setListeners(){
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                icon(item.getItemId());

                if (item.getItemId()==R.id.homeFragment){
                    binding.chatBot.setVisibility(View.VISIBLE);
                    changeFragment(fragmentTags[0]);
                    return true;
                }else if (item.getItemId()==R.id.chatFragment){
                    binding.chatBot.setVisibility(View.VISIBLE);
                    changeFragment(fragmentTags[1]);
                    return true;
                }else if (item.getItemId()==R.id.aiBotFragment){
                    binding.chatBot.setVisibility(View.GONE);
                    changeFragment(fragmentTags[2]);
                    return true;
                }else if (item.getItemId()==R.id.profileFragment){
                    binding.chatBot.setVisibility(View.GONE);
                    changeFragment(fragmentTags[3]);
                    return true;
                }else {
                    return false;
                }
            }
        });

        binding.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.logout:
                                FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(prefManager.getId())).child("token").setValue("");
                                prefManager.setId(0);
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                finish();
                                break;
                            case R.id.term:
                                openLink("https://www.google.com/");
                                break;
                            case R.id.privacy:
                                openLink("https://www.google.com/");
                                break;
                        }
                        return true;
                    }
                });

                popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        // Respond to popup being dismissed.
                    }
                });

                // Show the popup menu.
                popup.show();
            }
        });

        binding.chatBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ChotbotAcitivity.class));
            }
        });

    }

    public void icon(int id){
        Menu menu = binding.bottomNavigation.getMenu();
        menu.findItem(R.id.homeFragment).setIcon(id==R.id.homeFragment ? R.drawable.home_active : R.drawable.home);
        menu.findItem(R.id.chatFragment).setIcon(id==R.id.chatFragment ? R.drawable.chat_active : R.drawable.chat);
        menu.findItem(R.id.aiBotFragment).setIcon(id==R.id.aiBotFragment ? R.drawable.features_selected : R.drawable.features);
        menu.findItem(R.id.profileFragment).setIcon(id==R.id.profileFragment ? R.drawable.user_active : R.drawable.user);
    }

    public void loadFragments(){
        HomeFragment homeFragment = new HomeFragment();
        ChatFragment chatFragment = new ChatFragment();
        FavouriteFragment aiBotFragment = new FavouriteFragment();
        ProfileFragment profileFragment = new ProfileFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment, homeFragment, "homeFragment");
        transaction.add(R.id.fragment, chatFragment, "chatFragment");
        transaction.add(R.id.fragment, aiBotFragment, "aiBotFragment");
        transaction.add(R.id.fragment, profileFragment, "profileFragment");
        transaction.hide(chatFragment);
        transaction.hide(aiBotFragment);
        transaction.hide(profileFragment);
        transaction.commit();
    }

    public void changeFragment(String id){
        if (id.equals(fragmentTags[0]))
            binding.title.setText("Home");
        else if (id.equals(fragmentTags[1]))
            binding.title.setText("Chat");
        else if (id.equals(fragmentTags[2]))
            binding.title.setText("Favourites");
        else
            binding.title.setText("Profile");

        transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        for (String name : fragmentTags){
            if (name.equals(id))
                transaction.show(fragmentManager.findFragmentByTag(name));
            else
                transaction.hide(fragmentManager.findFragmentByTag(name));
        }
        transaction.commit();
    }

    private void checkAndRequestLocationPermissions() {
        int fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_CODE_LOCATION);
        } else {
            checkGPS();
        }
    }

    private void checkGPS() {
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }

        Log.d(TAG, "checkGPS: " + gps_enabled);
        if (!gps_enabled) {
            if (!isDialogueShowing) {
                isDialogueShowing = true;
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Please turn on your location")
                        .setCancelable(false)
                        .setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                paramDialogInterface.cancel();
                                isDialogueShowing = false;
                                isPermissionRequested = false;
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .show();
            }
        } else {
            currentLocation();
        }
    }

    private void currentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CommonFeatures.showLoadingDialogue(MainActivity.this);
        mFusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken()
        ).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    CommonFeatures.hideDialogue();
                    CommonFeatures.location = location;
                    loadFragments();
                }else{
                    getLastLocation();
                }
            }
        });
    }

    public void getLastLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    CommonFeatures.hideDialogue();
                    CommonFeatures.location = location;
                    loadFragments();
                }
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE_LOCATION: {

                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        checkGPS();
                    } else {
                        explain("You need to give Location permission to get your location");
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void explain(String msg) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        checkAndRequestLocationPermissions();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void updateLocation(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String cityName = "";
        String stateName = "";
        String countryName = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            cityName = addresses.get(0).getLocality();
            stateName = addresses.get(0).getAdminArea();
            countryName = addresses.get(0).getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        User user = new User(latitude,longitude);
        Call<UserResponse> call = RetrofitClint.getInstance().getApi().update_location(prefManager.getId(),user);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.d(TAG, "onResponse: ");
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    public void openLink(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}