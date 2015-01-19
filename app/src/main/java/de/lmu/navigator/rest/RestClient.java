package de.lmu.navigator.rest;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.List;

import de.lmu.navigator.model.Building;
import de.lmu.navigator.model.BuildingPart;
import de.lmu.navigator.model.City;
import de.lmu.navigator.model.Floor;
import de.lmu.navigator.model.Room;
import de.lmu.navigator.model.Street;
import de.lmu.navigator.model.Version;

@Rest(rootUrl = "http://141.84.213.246:8080/lmu-navigator/rest", converters = { FormHttpMessageConverter.class, GsonHttpMessageConverter.class })
public interface RestClient extends RestClientErrorHandling {

    //DATA VERSION
    @Get("/version")
    Version getVersion();

    //CITIES
    @Get("/cities")
    List<City> getCities();

    //STREETS
    @Get("/streets")
    List<Street> getStreets();

    @Get("/streets?code={streetCode}&city={cityCode}")
    List<Street> getStreetsFiltered(String streetCode, String cityCode);

    @Get("/streets/{code}")
    Street getStreet(String code);

    @Get("/buildings/{buildingID}?noFloors={noFloors}")
    Building getBuildingsFiltered(int buildingID, boolean noFloors);


    //BUILDINGS
    @Get("/buildings")
    List<Building> getBuildings();

    @Get("/buildings?building={buildingCode}&street={streetCode}")
    List<Building> getBuildingsFiltered(String buildingCode, String streetCode);

    @Get("/buildings/{code}")
    Building getBuilding(String code);


    //BUILDINGPARTS
    @Get("/buildingparts")
    List<BuildingPart> getBuildingParts();

    @Get("/buildingparts/{code}")
    BuildingPart getBuildingPart(String code);


    //FLOORS
    @Get("/floors")
    List<Floor> getFloors();

    @Get("/floors?buildingId={buildingID}&start={startID}&end={endID}&buildingPart={buildingPart}&level={level}&noRooms={noRooms}")
    List<Floor> getFloors(int buildingID, int startID, int endID, String buildingPart, String level, boolean noRooms);

    @Get("/floors/{floorID}?noRooms={noRooms}")
    List<Floor> getFloorsFiltered(int floorID, boolean noRooms);

    @Get("/floors/{floorID}")
    Floor getFloor(int floorID);


    //ROOMS
    @Get("/rooms")
    List<Room> getRooms();

    @Get("/rooms?floor={floorID}")
    List<Room> getRoomsFromFloor(int floorID);


    //TEST
    @Get("/ping")
    String getPong();

}
