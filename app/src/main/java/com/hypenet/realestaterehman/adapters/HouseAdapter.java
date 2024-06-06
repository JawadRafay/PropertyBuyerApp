package com.hypenet.realestaterehman.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.database.RoomDatabase;
import com.hypenet.realestaterehman.databinding.HouseItemBinding;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;
    Context context;
    List<House> data,allData;
    RoomDatabase roomDatabase;
    private static final String TAG = "HouseAdapter";

    public HouseAdapter(Context context, List<House> data) {
        this.context = context;
        this.data = data;
        allData = new ArrayList<>();
        allData.addAll(data);
        roomDatabase = RoomDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.house_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        HouseItemBinding binding;
        public MyViewHolder(@NonNull HouseItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void bind(int position){
            House house = data.get(position);
            if (house.getProperty_images().size()==0){
                binding.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.house,null));
            }else{
                Glide.with(context).load(house.getProperty_images().get(0).getImage()).into(binding.image);
            }
            double dis = Utils.distance(Double.parseDouble(house.getLatitude()),Double.parseDouble(house.getLongitude()));
            binding.distance.setText(String.format("%.1f",dis));
            binding.setHouse(house);
            if (roomDatabase.houseDao().checkFavourite(house.getId()) == null)
                binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.bookmark,null));
            else
                binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.bookmark_selected,null));

            binding.bookMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (roomDatabase.houseDao().checkFavourite(house.getId()) == null){
                        roomDatabase.houseDao().insert(house);
                        binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.bookmark_selected,null));
                    } else {
                        roomDatabase.houseDao().delete(house);
                        binding.bookMark.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.bookmark,null));
                    }
                }
            });

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(house);
                }
            });

            Log.d(TAG, "bind:Kyc_status "+house.getSeller().getKyc_status());
            if (house.getSeller().getKyc_status() != null){
                if (house.getSeller().getKyc_status().equalsIgnoreCase("Verified")){
                    binding.verified.setVisibility(View.VISIBLE);
                }else {
                    binding.verified.setVisibility(View.GONE);
                }
            }else{
                binding.verified.setVisibility(View.GONE);
            }

        }
    }

    public void filter(String title){

    }

    public interface OnItemClickListener{
        void onClick(House house);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
