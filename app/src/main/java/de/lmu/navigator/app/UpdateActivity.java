package de.lmu.navigator.app;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.Building;
import de.lmu.navigator.model.BuildingPart;
import de.lmu.navigator.model.City;
import de.lmu.navigator.model.Floor;
import de.lmu.navigator.model.Room;
import de.lmu.navigator.model.Street;
import de.lmu.navigator.model.Version;
import de.lmu.navigator.rest.RestClient;

@EActivity(R.layout.activity_update)
public class UpdateActivity extends ActionBarActivity {

    private static final String LOG_TAG = UpdateActivity.class.getSimpleName();

    public static final int RESULT_ERROR = 10;

    public static final String RESULT_EXTRA_NEW_VERSION = "new_version";

    @RestService
    RestClient mRestClient;

    @Pref
    PreferencesHelper_ mPrefs;

    @ViewById(R.id.update_text_title)
    TextView mTextTitle;

    @ViewById(R.id.update_text_info)
    TextView mTextInfo;

    @ViewById(R.id.update_button)
    Button mButton;

    @ViewById(R.id.update_image)
    ImageView mImage;

    @ViewById(R.id.update_progress)
    ProgressBar mProgress;

    private boolean mUpdateRunning = false;

    private List<String> mFavorites = new ArrayList<String>();

    @AfterViews
    void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        startUpdate();
    }

    @Background
    void startUpdate() {
        // try to remember favorites and restore them later
        for (Building b : Building.getFavorites()) {
            mFavorites.add(b.getCode());
        }

        boolean updateSuccessful = false;
        mUpdateRunning = true;

        Log.d(LOG_TAG, "Starting update. Old version: " + mPrefs.dataVersion().get());

        try {
            // Download data
            Log.d(LOG_TAG, "Download cities...");
            List<City> cities = mRestClient.getCities();

            Log.d(LOG_TAG, "Download streets...");
            List<Street> streets = mRestClient.getStreets();

            Log.d(LOG_TAG, "Download buildings...");
            List<Building> buildings = mRestClient.getBuildings();

            Log.d(LOG_TAG, "Download buildingParts...");
            List<BuildingPart> buildingParts = mRestClient.getBuildingParts();

            Log.d(LOG_TAG, "Download floors...");
            List<Floor> floors = mRestClient.getFloors();

            Log.d(LOG_TAG, "Download rooms...");
            List<Room> rooms = mRestClient.getRooms();

            Log.d(LOG_TAG, "Download version...");
            Version version = mRestClient.getVersion();

            Log.d(LOG_TAG, "Download successful!");

            // Update database
            ActiveAndroid.beginTransaction();
            try {
                // Delete old data
                new Delete().from(City.class).execute();
                new Delete().from(Street.class).execute();
                new Delete().from(Building.class).execute();
                new Delete().from(BuildingPart.class).execute();
                new Delete().from(Floor.class).execute();
                new Delete().from(Room.class).execute();

                // Save new data
                Log.d(LOG_TAG, "Save cities...");
                for (City c : cities) {
                    c.save();
                }
                Log.d(LOG_TAG, "Save streets...");
                for (Street s : streets) {
                    s.save();
                }
                Log.d(LOG_TAG, "Save buildings...");
                for (Building b : buildings) {
                    // restore favorites
                    if (mFavorites.contains(b.getCode())) {
                        b.setStar(true);
                    }
                    b.save();
                }
                Log.d(LOG_TAG, "Save buildingParts...");
                for (BuildingPart bp : buildingParts) {
                    bp.save();
                }
                Log.d(LOG_TAG, "Save floors...");
                for (Floor f : floors) {
                    f.save();
                }
                Log.d(LOG_TAG, "Save rooms...");
                for (Room r : rooms) {
                    r.save();
                }

                ActiveAndroid.setTransactionSuccessful();
                updateSuccessful = true;
                setResult(RESULT_OK, new Intent().putExtra(RESULT_EXTRA_NEW_VERSION, version.version));
                Log.d(LOG_TAG, "DB update successful!");
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error saving update to database! Update cancelled!", e);
                updateSuccessful = false;
            } finally {
                ActiveAndroid.endTransaction();
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error downloading update from server! Update cancelled!", e);
            updateSuccessful = false;
        }

        mUpdateRunning = false;

        if (updateSuccessful) {
            showSuccess();
        } else {
            setResult(RESULT_ERROR, null);
            showError();
        }
    }

    @UiThread
    void showSuccess() {
        Log.d(LOG_TAG, "Update finished successfully! New version: " + mPrefs.dataVersion().get());
        mTextTitle.setText(R.string.update_title_success);
        mTextInfo.setText(R.string.update_info_success);
        mButton.setText(R.string.update_button_ok);
        mProgress.setVisibility(View.GONE);
        mImage.setVisibility(View.VISIBLE);
        // TODO: set image
    }

    @UiThread
    void showError() {
        Log.d(LOG_TAG, "Update failed!");
        mTextTitle.setText(R.string.update_title_error);
        mTextInfo.setText(R.string.update_info_error);
        mButton.setText(R.string.update_button_ok);
        mProgress.setVisibility(View.GONE);
        mImage.setVisibility(View.VISIBLE);
        // TODO: set image
    }

    @Click(R.id.update_button)
    void onButtonClick() {
        if (mUpdateRunning) {
            // TODO: show confirmation dialog before cancel!
            setResult(RESULT_CANCELED, null);
        }
        finish();
    }
}
