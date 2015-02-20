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

    @GET("/version")
    Version getVersion();

    @GET("/version")
    void getVersionAsync(Callback<Version> callback);

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
