package de.lmu.navigator.database;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.Floor;

public class ModelHelper {

    public static final String BUILDING_CODE = "code";
    public static final String BUILDING_NAME = "displayName";
    public static final String BUILDING_STARRED = "starred";

    public static final String BUILDING_PART_CODE = "code";

    public static final String ROOM_CODE = "code";

    private static final String TILES_BASE_PATH = "http://141.84.213.246/navigator/tiles/";

    public static final List<String> FLOOR_ORDER = Arrays.asList("2.Untergeschoss",
            "1.Untergeschoss", "Erdgeschoss", "Zwischengeschoss", "1.Obergeschoss",
            "1.Zwischengeschoss", "2.Obergeschoss", "2.Zwischengeschoss", "3.Obergeschoss",
            "4.Obergeschoss", "5.Obergeschoss", "6.Obergeschoss");

    private ModelHelper() {}

    public static String getBuildingNameFixed(String name) {
        String formattedName = name
                .replace("STR.", "STRASSE")
                .replace(" - ", "-");

        formattedName = WordUtils.capitalizeFully(formattedName, '-', ' ');
        formattedName = formattedName
                .replace("strasse", "straße")
                .replace("Strasse", "Straße");

        // TODO: Fix all errors in data?

        return formattedName;
    }

    public static LatLng getBuildingLatLng(Building building) {
        return new LatLng(building.getCoordLat(), building.getCoordLong());
    }

    public static String getFloorTilesPath(Floor floor, String detailLevel) {
        return TILES_BASE_PATH + floor.getMapUri().split("\\.")[0] + "/" + detailLevel
                + "/%col%/%row%.png";
    }

    public static String getFloorSamplePath(Floor floor) {
        return "samples/" + floor.getMapUri().split("\\.")[0] + ".png";
    }

    public static Comparator<Floor> floorComparator = new Comparator<Floor>() {
        @Override
        public int compare(Floor lhs, Floor rhs) {
            int i = FLOOR_ORDER.indexOf(lhs.getName());
            int i2 = FLOOR_ORDER.indexOf(rhs.getName());

            if (i == i2)
                return 0;
            if (i < i2)
                return -1;

            return 1;
        }
    };
}
