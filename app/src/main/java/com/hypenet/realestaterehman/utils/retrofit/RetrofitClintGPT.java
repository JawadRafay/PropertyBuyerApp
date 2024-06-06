package com.hypenet.realestaterehman.utils.retrofit;

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

public class RetrofitClintGPT {
    public static Retrofit retrofit;
    public static final String BASE_URL = "https://api.openai.com/v1/chat/";
    public static RetrofitClintGPT instance;
    private RetrofitClintGPT(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer sk-proj-4N8nJDSyhhKPMZOnOyBUT3BlbkFJUI9ufj1hfrlURk6e5gV9").build();
                        return chain.proceed(request);
                    }
                })
                .build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetrofitClintGPT getInstance(){
        if (retrofit == null){
            instance = new RetrofitClintGPT();
            return  instance;
        }
        return instance;
    }
    public static Api getApi(){
        return retrofit.create(Api.class);
    }
}
