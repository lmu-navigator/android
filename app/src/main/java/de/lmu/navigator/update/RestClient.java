package de.lmu.navigator.update;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.City;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;
import de.lmu.navigator.database.model.Street;
import de.lmu.navigator.database.model.Version;

public interface RestClient {

    Version getVersion();

    List<City> getCities();

    List<Street> getStreets();

    List<Building> getBuildings();

    List<BuildingPart> getBuildingParts();

    List<Floor> getFloors();

    List<Room> getRooms();

}
