package com.example.dpivovar.vksdktest;

import org.json.JSONObject;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * Created by dpivovar on 16.08.2016.
 */
public interface VKUploadService {

    @Multipart
    @POST(value = "/c637929/upload.php")
    void upload(@QueryMap Map<String, String> queryMap,
                @Part("photo") TypedFile photo,
                Callback<JSONObject> cb);
}
