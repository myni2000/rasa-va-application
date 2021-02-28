package com.myni.Network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ApiClient {

    public static final String BASE_URL = "http://rasa-myni.herokuapp.com/webhooks/rest/webhook/";

    private static ApiInterface apiService;

    public static ApiInterface getApiService() {

        if (apiService == null) {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).baseUrl(BASE_URL).build();

            apiService = retrofit.create(ApiInterface.class);
            return apiService;
        } else {
            return apiService;
        }
    }
}
