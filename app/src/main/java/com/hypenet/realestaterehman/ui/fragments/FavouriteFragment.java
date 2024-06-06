package com.hypenet.realestaterehman.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.HouseAdapter;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.FragmentFavouriteBinding;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.ui.activities.HouseDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class FavouriteFragment extends Fragment {

    RoomDatabase roomDatabase;
    Activity activity;
    FragmentFavouriteBinding binding;
    List<House> data;
    HouseAdapter adapter;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favourite,container,false);
        init();
        loadData();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    public void init(){
        roomDatabase = RoomDatabase.getInstance(activity);
        data = new ArrayList<>();
        adapter = new HouseAdapter(activity,data);
        adapter.setOnItemClickListener(new HouseAdapter.OnItemClickListener() {
            @Override
            public void onClick(House house) {
                startActivity(new Intent(activity, HouseDetailsActivity.class).putExtra("data",house));
            }
        });
        binding.recycler.setAdapter(adapter);
    }

    public void loadData(){
        roomDatabase.houseDao().getHouses().observe(getViewLifecycleOwner(), new Observer<List<House>>() {
            @Override
            public void onChanged(List<House> houses) {
                data.clear();
                data.addAll(houses);
                adapter.notifyDataSetChanged();
                if (data.size() == 0){
                    binding.msg.setVisibility(View.VISIBLE);
                }else{
                    binding.msg.setVisibility(View.GONE);
                }
            }
        });
    }
}
