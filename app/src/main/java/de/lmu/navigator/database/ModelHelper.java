package de.lmu.navigator.database;

import org.apache.commons.lang3.text.WordUtils;

public class ModelHelper {

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

}
