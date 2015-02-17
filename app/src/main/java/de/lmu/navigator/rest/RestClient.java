package de.lmu.navigator.rest;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.RestClientErrorHandling;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.List;

import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.model.BuildingPartOld;
import de.lmu.navigator.model.CityOld;
import de.lmu.navigator.model.FloorOld;
import de.lmu.navigator.model.RoomOld;
import de.lmu.navigator.model.StreetOld;
import de.lmu.navigator.database.model.Version;

@Rest(rootUrl = "http://141.84.213.246:8080/lmu-navigator/rest", converters = { FormHttpMessageConverter.class, GsonHttpMessageConverter.class })
public interface RestClient extends RestClientErrorHandling {

    //DATA VERSION
    @Get("/version")
    Version getVersion();

    //CITIES
    @Get("/cities")
    List<CityOld> getCities();

    //STREETS
    @Get("/streets")
    List<StreetOld> getStreets();

    @Get("/streets?code={streetCode}&city={cityCode}")
    List<StreetOld> getStreetsFiltered(String streetCode, String cityCode);

    @Get("/streets/{code}")
    StreetOld getStreet(String code);

    @Get("/buildings/{buildingID}?noFloors={noFloors}")
    BuildingOld getBuildingsFiltered(int buildingID, boolean noFloors);


    //BUILDINGS
    @Get("/buildings")
    List<BuildingOld> getBuildings();

    @Get("/buildings?building={buildingCode}&street={streetCode}")
    List<BuildingOld> getBuildingsFiltered(String buildingCode, String streetCode);

    @Get("/buildings/{code}")
    BuildingOld getBuilding(String code);


    //BUILDINGPARTS
    @Get("/buildingparts")
    List<BuildingPartOld> getBuildingParts();

    @Get("/buildingparts/{code}")
    BuildingPartOld getBuildingPart(String code);


    //FLOORS
    @Get("/floors")
    List<FloorOld> getFloors();

    @Get("/floors?buildingId={buildingID}&start={startID}&end={endID}&buildingPart={buildingPart}&level={level}&noRooms={noRooms}")
    List<FloorOld> getFloors(int buildingID, int startID, int endID, String buildingPart, String level, boolean noRooms);

    @Get("/floors/{floorID}?noRooms={noRooms}")
    List<FloorOld> getFloorsFiltered(int floorID, boolean noRooms);

    @Get("/floors/{floorID}")
    FloorOld getFloor(int floorID);


    //ROOMS
    @Get("/rooms")
    List<RoomOld> getRooms();

    @Get("/rooms?floor={floorID}")
    List<RoomOld> getRoomsFromFloor(int floorID);


    //TEST
    @Get("/ping")
    String getPong();

}
