package com.mikkipastel.live500px.manager.http;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.POST;

public interface ApiService {
    @POST("list")
    Call<Objects> loadPhotoList();
}
