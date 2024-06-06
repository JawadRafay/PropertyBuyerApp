package com.hypenet.realestaterehman.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hypenet.realestaterehman.R;
import com.hypenet.realestaterehman.adapters.ChatBotAdapter;
import com.hypenet.realestaterehman.adapters.ChatListAdapter;
import com.hypenet.realestaterehman.databinding.ActivityChotbotAcitivityBinding;
import com.hypenet.realestaterehman.model.Chat;
import com.hypenet.realestaterehman.model.ChatBotCompletion;
import com.hypenet.realestaterehman.model.ChatBotResponse;
import com.hypenet.realestaterehman.model.House;
import com.hypenet.realestaterehman.utils.Constants;
import com.hypenet.realestaterehman.utils.retrofit.RetrofitClintGPT;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChotbotAcitivity extends AppCompatActivity {

    ActivityChotbotAcitivityBinding binding;
    ChatBotAdapter adapter;
    ArrayList<Chat> chats,botChats;
    Call<ChatBotResponse> call;
    private static final String TAG = "ChotbotAcitivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chotbot_acitivity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getResources().getColor( R.color.light_gray));
        init();
        setListeners();
    }

    public void init(){
        botChats = new ArrayList<>();
        String systemInstructions = "You are helpful property assistant. You will guide the user with about properties. These are the properties list available. I am providing house details ";
        int i = 1;
        for (House house : Constants.data) {
            systemInstructions = systemInstructions + i +")" + " property name: "+house.getTitle()+", price:"+house.getPrice()+", city:"+house.getCity()+", area:"+house.getArea_name()+", property type (rent, sale):"+house.getType()+", latitude:"+house.getLatitude()+", longitude:"+house.getLongitude()+", phone:"+house.getPhone();
            i++;
        }
        botChats.add(new Chat("system",systemInstructions));
        chats = new ArrayList<>();
        chats.add(new Chat("system","How can i assist you today?"));
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatBotAdapter(this,chats);
        binding.recycler.setAdapter(adapter);
    }

    public void setListeners(){
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.msgedittext.getText().toString().isEmpty()){
                    Chat chat = new Chat("user",binding.msgedittext.getText().toString());
                    assistantChat(chat);
                    binding.msgedittext.setText("");
                }
            }
        });
    }

    public void assistantChat(Chat message){
        binding.typing.setVisibility(View.VISIBLE);
        ChatBotCompletion model = new ChatBotCompletion();
        chats.add(message);
        botChats.add(message);
        model.setMessages(botChats);
        Log.d(TAG, "assistantChat: "+new Gson().toJson(model));
        call = RetrofitClintGPT.getInstance().getApi().chat(model);
        call.enqueue(new Callback<ChatBotResponse>() {
            @Override
            public void onResponse(Call<ChatBotResponse> call, Response<ChatBotResponse> response) {
                Log.d(TAG, "onResponse: "+response);
                Log.d(TAG, "onResponse:headers "+response.headers());
                binding.typing.setVisibility(View.GONE);
                if (response.body() != null){
                    chats.add(response.body().getChoices().get(0).getMessage());
                    botChats.add(response.body().getChoices().get(0).getMessage());
                    adapter.notify(chats.size()-1);
                    binding.recycler.scrollToPosition(chats.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<ChatBotResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
                binding.typing.setVisibility(View.GONE);
                if (t.getMessage() != null && !t.getMessage().equalsIgnoreCase("Canceled")){
                    Toast.makeText(ChotbotAcitivity.this, "Network Error! Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null)
            call.cancel();
    }
}