package com.myni.Network;

import com.myni.Model.UserMessage;
import com.myni.Model.ask;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiInterface {
        @POST("webhook")
        Call<List<UserMessage>> sendMessage(@Body ask userMessage);


}
