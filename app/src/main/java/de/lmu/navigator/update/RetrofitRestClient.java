package de.lmu.navigator.update;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public final class RetrofitRestClient {

    private static final String REST_ENDPOINT = "http://141.84.213.246:8080/lmu-navigator/rest";

    private RetrofitRestClient() {}

    public static RestService create() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setEndpoint(REST_ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .build();

        return restAdapter.create(RestService.class);
    }
}
