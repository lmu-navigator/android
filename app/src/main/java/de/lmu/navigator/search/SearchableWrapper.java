package de.lmu.navigator.search;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.Room;

public class SearchableWrapper implements Searchable {

    private String mPrimaryText;

    private String mSecondaryText;

    private String mCode;

    public static Searchable wrap(Room room, String buildingPartHint) {
        return new SearchableWrapper(room, buildingPartHint);
    }

    public static Searchable wrap(Building building) {
        return new SearchableWrapper(building);
    }

    private SearchableWrapper(Room room, String buildingPartHint) {
        mPrimaryText = room.getName();
        mSecondaryText = room.getFloor().getName();
        mCode = room.getCode();

        if (buildingPartHint != null) {
            mSecondaryText = mSecondaryText + " (" + buildingPartHint + ")";
        }
    }

    private SearchableWrapper(Building building) {
        mPrimaryText = building.getDisplayName();
        mSecondaryText = building.getStreet().getCity().getName();
        mCode = building.getCode();
    }


    @Override
    public String getPrimaryText() {
        return mPrimaryText;
    }

    @Override
    public String getSecondaryText() {
        return mSecondaryText;
    }

    @Override
    public String getCode() {
        return mCode;
    }
}
