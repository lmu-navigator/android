package de.lmu.navigator.database;

import java.util.List;

import de.lmu.navigator.database.model.Building;

public interface DatabaseManager {

    List<Building> getAllBuildings(boolean sorted);

    List<Building> getStarredBuildings(boolean sorted);

    void setBuildingStarred(Building building, boolean star);

}
