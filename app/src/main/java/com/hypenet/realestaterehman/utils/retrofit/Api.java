package com.hypenet.realestaterehman.utils.retrofit;



import com.hypenet.realestaterehman.model.ApiResponse;
import com.hypenet.realestaterehman.model.ChatBotCompletion;
import com.hypenet.realestaterehman.model.ChatBotResponse;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.model.NotificationModel;
import com.hypenet.realestaterehman.model.User;
import com.hypenet.realestaterehman.model.UserResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface Api {

    @POST("login_buyer")
    Call<UserResponse> login(@Body User user);

    @Multipart
    @POST("register_buyer")
    Call<UserResponse> register(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("phone") RequestBody phone,
            @Part("city") RequestBody city,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("update_buyer_profile/{id}")
    Call<UserResponse> update_profile(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part("city") RequestBody city,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part image
    );

    @POST("update_geo/{id}")
    Call<UserResponse> update_location(@Path("id") int id, @Body User user);
    @GET("get_properties")
    Call<ApiResponse<List<House>>> get_properties();

    @GET("get_cities")
    Call<ApiResponse<List<City>>> get_cities();

    @POST("completions")
    Call<ChatBotResponse> chat(@Body ChatBotCompletion botCompletion);

    @POST
    Call<UserResponse> setNotification(@Header("Authorization") String auth, @Url String url, @Body NotificationModel model);

}



