package com.android.devthien.currencyexchange.apis;

import com.android.devthien.currencyexchange.models.ResponseModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiServices {
    Gson gson = new GsonBuilder().create();
    //http://api.geonames.org/countryInfoJSON?formatted=true&lang=it&username=thienv29&style=full&fbclid=IwAR0-M-iwVsSj0OZwULO9428GiGzxgAoZGb0oyiQLiFFLpjRtoNC0LR7jloI
    ApiServices apiServices = new Retrofit.Builder()
            .baseUrl("http://api.geonames.org/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiServices.class);
    @GET("countryInfoJSON")
    Call<ResponseModel> getListNation(@Query("formatted") String formatted,
                                      @Query("lang") String lang,
                                      @Query("username") String username,
                                      @Query("style") String style,
                                      @Query("fbclid") String fbclid
                                    );
}
