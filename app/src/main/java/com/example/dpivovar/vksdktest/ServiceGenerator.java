package com.example.dpivovar.vksdktest;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by dpivovar on 16.08.2016.
 */
public class ServiceGenerator {
    private static final String TAG = "ServiceGenerator";

    private static RestAdapter.Builder builder = new RestAdapter.Builder().setClient(new OkClient(new OkHttpClient()));

    public static <S> S createService(Class<S> serviceClass, String url) {
        Uri uri = Uri.parse(url);
        String host = "https://" + uri.getHost()/* + uri.getEncodedPath()*/;
        Log.d(TAG, "host: " + host);
        builder.setEndpoint(host);
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }
}
