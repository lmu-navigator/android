package de.lmu.navigator.database;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.database.model.BuildingPart;
import de.lmu.navigator.database.model.Floor;

public class ModelHelper {

    public static final String BUILDING_CODE = "code";
    public static final String BUILDING_NAME = "displayName";
    public static final String BUILDING_STARRED = "starred";

    public static final String BUILDING_PART_CODE = "code";

    public static final String FLOOR_BUILDING_CODE = "buildingPart.building.code";

    public static final String ROOM_CODE = "code";
    public static final String ROOM_NAME = "name";
    public static final String ROOM_FLOOR_CODE = "floor.code";
    public static final String ROOM_FLOOR_MAP_URI = "floor.mapUri";
    public static final String ROOM_BUILDING_CODE = "floor.buildingPart.building.code";

    private static final String TILES_BASE_PATH = "http://lmu-navigator.github.io/data/tiles/";
    private static final String PHOTO_BASE_PATH = "http://lmu-navigator.github.io/data/photos/";
    private static final String THUMBNAIL_BASE_PATH = "http://lmu-navigator.github.io/data/photos/thumbnails/";

    public static final List<String> FLOOR_ORDER = Arrays.asList("UG2", "UG1", "EG", "ZG", "OG1",
            "ZG1", "OG2", "ZG2", "OG3", "OG4", "OG5", "OG6");

    private ModelHelper() {
    }

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

    public static String getPictureUrl(Building building) {
        return PHOTO_BASE_PATH + building.getCode() + ".jpg";
    }

    public static String getThumbnailUrl(Building building) {
        return THUMBNAIL_BASE_PATH + building.getCode() + ".jpg";
    }

    public static Comparator<Floor> floorComparator = new Comparator<Floor>() {
        @Override
        public int compare(Floor lhs, Floor rhs) {
            int i = FLOOR_ORDER.indexOf(lhs.getLevel());
            int i2 = FLOOR_ORDER.indexOf(rhs.getLevel());

            if (i == i2)
                return 0;
            if (i < i2)
                return -1;

            return 1;
        }
    };

    public static String getFloorNameFixed(String name) {
        return name.replace(".", ". ");
    }

    public static String getFloorLevelFixed(String level, String name) {
        if (name.equals("2. Untergeschoss"))
            return "UG2";
        if (name.equals("1. Untergeschoss"))
            return "UG1";
        if (name.equals("Erdgeschoss"))
            return "EG";
        if (name.equals("1. Obergeschoss"))
            return "OG1";
        if (name.equals("Zwischengeschoss"))
            return "ZG";
        if (name.equals("2. Obergeschoss"))
            return "OG2";
        if (name.equals("1. Zwischengeschoss"))
            return "ZG1";
        if (name.equals("3. Obergeschoss"))
            return "OG3";
        if (name.equals("2. Zwischengeschoss"))
            return "ZG2";
        if (name.equals("4. Obergeschoss"))
            return "OG4";
        if (name.equals("5. Obergeschoss"))
            return "OG5";
        if (name.equals("6. Obergeschoss"))
            return "OG6";
        return level;
    }

    public static String getBuildingPartNameFixed(String name) {
        int index = name.indexOf("(");
        if (index < 0) {
            return "";
        }
        return name.substring(index).replace("(", "").replace(")", "").trim();
    }

    public static Comparator<BuildingPart> buildingPartComparator = new Comparator<BuildingPart>() {
        @Override
        public int compare(BuildingPart lhs, BuildingPart rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    };
}
