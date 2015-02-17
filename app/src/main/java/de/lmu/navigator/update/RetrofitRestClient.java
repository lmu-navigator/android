package de.lmu.navigator.update;


import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.City;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.database.model.Street;
import de.lmu.navigator.database.model.Version;
import retrofit.RestAdapter;
import retrofit.http.GET;

public class RetrofitRestClient implements RestClient {

    private static final String REST_ENDPOINT = "http://141.84.213.246:8080/lmu-navigator/rest";

    private final RetrofitService mRetrofitService;

    private interface RetrofitService {

        @GET("/version")
        Version getVersion();

        @GET("/cities")
        List<City> getCities();

        @GET("/streets")
        List<Street> getStreets();

        @GET("/buildings")
        List<Building> getBuildings();

        @GET("/buildingparts")
        List<BuildingPart> getBuildingParts();

        @GET("/floors")
        List<Floor> getFloors();

        @GET("/rooms")
        List<Room> getRooms();
    }

    public RetrofitRestClient() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setEndpoint(REST_ENDPOINT)
                .build();

        mRetrofitService = restAdapter.create(RetrofitService.class);
    }

    @Override
    public Version getVersion() {
        return mRetrofitService.getVersion();
    }

    @Override
    public List<City> getCities() {
        return mRetrofitService.getCities();
    }

    @Override
    public List<Street> getStreets() {
        return mRetrofitService.getStreets();
    }

    @Override
    public List<Building> getBuildings() {
        return mRetrofitService.getBuildings();
    }

    @Override
    public List<BuildingPart> getBuildingParts() {
        return mRetrofitService.getBuildingParts();
    }

    @Override
    public List<Floor> getFloors() {
        return mRetrofitService.getFloors();
    }

    @Override
    public List<Room> getRooms() {
        return mRetrofitService.getRooms();
    }
}
