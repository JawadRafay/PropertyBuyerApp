package com.hypenet.realestaterehman.utils.retrofit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClint {
    public static Retrofit retrofit;
    public static final String BASE_URL = "https://home.freelanceworking.site/public/api/";
    public static RetrofitClint instance;
    private RetrofitClint(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetrofitClint getInstance(){
        if (retrofit == null){
            instance = new RetrofitClint();
            return  instance;
        }
        return instance;
    }
    public static Api getApi(){
        return retrofit.create(Api.class);
    }
}
