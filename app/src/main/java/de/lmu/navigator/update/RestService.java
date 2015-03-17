package de.lmu.navigator.update;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.City;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.database.model.Street;
import de.lmu.navigator.database.model.Version;
import retrofit.Callback;
import retrofit.http.GET;

public interface RestService {

    @GET("/version.json")
    Version getVersion();

    @GET("/version.json")
    void getVersionAsync(Callback<Version> callback);

    @GET("/1_city.json")
    List<City> getCities();

    @GET("/2_street.json")
    List<Street> getStreets();

    @GET("/3_building.json")
    List<Building> getBuildings();

    @GET("/4_building_part.json")
    List<BuildingPart> getBuildingParts();

    @GET("/5_floor.json")
    List<Floor> getFloors();

    @GET("/6_room.json")
    List<Room> getRooms();

}
