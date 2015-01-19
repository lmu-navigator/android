package de.lmu.navigator.search;

import android.os.Parcelable;

public interface Searchable extends Parcelable {

    String getPrimaryText();

    String getSecondaryText();

    Long getId();
}
