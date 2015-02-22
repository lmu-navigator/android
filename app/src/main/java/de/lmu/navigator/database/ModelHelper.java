package de.lmu.navigator.database;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.text.WordUtils;

import de.lmu.navigator.database.model.Building;

public class ModelHelper {

    public static final String BUILDING_CODE = "code";
    public static final String BUILDING_NAME = "displayName";
    public static final String BUILDING_STARRED = "starred";

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

}
