package de.lmu.navigator.database;

import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Floor;
import de.lmu.navigator.database.model.Room;

public interface DatabaseManager {

    List<Building> getAllBuildings(boolean sorted);

    List<Building> getStarredBuildings(boolean sorted);

    Building getBuilding(String code);

    void setBuildingStarred(Building building, boolean star);

    BuildingPart getBuildingPart(String code);

    Room getRoom(String code);

    List<Room> getRoomsForFloor(Floor f, boolean includeSameMap, boolean sorted);

    List<Room> getRoomsForBuilding(Building b, boolean includeSameMap, boolean sorted);

    void close();

}
