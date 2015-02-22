package de.lmu.navigator.app;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.androidannotations.annotations.EFragment;

import de.lmu.navigator.R;

@EFragment(R.layout.fragment_near)
public class NearbyFragment extends ListFragment implements LocationListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_near, container, false);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO
    }
}
