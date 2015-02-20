package de.lmu.navigator.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import de.greenrobot.event.EventBus;

public class LocationActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private final static String LOG_TAG = LocationActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private final static int LOCATION_UPDATE_INTERVAL = 15;

    private GoogleApiClient mLocationClient;
    private EventBus mEventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPlayServicesAvailable()) {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            mLocationClient.disconnect();
        }
        super.onStop();
    }

    private boolean checkPlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(LOG_TAG, "Google Play services is available.");
            return true;
        } else {
            Log.e(LOG_TAG, "Google Play services not available! Trying to resolve...");
            showErrorDialog(resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(LOG_TAG, "Resolution successful! Trying to connect again...");
                    // TODO
//                    if (mLocationClient == null) {
//                        mLocationClient = new LocationClient(this, this, this);
//                    }
//                    mLocationClient.connect();
                } else {
                    Log.e(LOG_TAG, "Resolution failed!");
                }
                break;

            case PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(LOG_TAG, "Resolution successful! Trying to connect again...");
//                    mLocationClient = new LocationClient(this, this, this);
//                    mLocationClient.connect();
                } else {
                    Log.e(LOG_TAG, "Resolution failed!");
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "LocationClient connected!");
        LocationRequest request = new LocationRequest()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            Log.e(LOG_TAG, "LocationClient connection failed! Trying to resolve...");
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "LocationClient connection failed! No resolution available.");
            showErrorDialog(connectionResult.getErrorCode(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO
    }

    @Override
    public void onLocationChanged(Location location) {
        mEventBus.post(new LocationUpdateEvent(location));
    }

    public GoogleApiClient getLocationClient() {
        return mLocationClient;
    }

    private void showErrorDialog(int errorCode, int requestCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                requestCode);

        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), "LMUNavi");
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static class LocationUpdateEvent {
        private Location mLocation;

        public LocationUpdateEvent(Location location) {
            mLocation = location;
        }

        public Location getLocation() {
            return mLocation;
        }
    }
}
