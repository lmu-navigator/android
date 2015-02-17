package de.lmu.navigator.database;

import de.lmu.navigator.database.model.Building;

public interface DatabaseManager {

    void setBuildingStarred(Building building, boolean star);

}
