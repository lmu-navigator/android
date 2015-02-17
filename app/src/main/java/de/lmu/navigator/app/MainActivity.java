package de.lmu.navigator.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.springframework.web.client.ResourceAccessException;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.outdoor.BuildingDetailActivity_;
import de.lmu.navigator.outdoor.LMUMapFragment_;
import de.lmu.navigator.rest.RestClient;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchBuildingActivity_;
import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends LocationActivity
        implements ISimpleDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_CODE_SEARCH_BUILDING = 1;

    public static final int REQUEST_CODE_UPDATE = 2;

    public static final int REQUEST_CODE_ADD_FAVORITE = 3;

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.navigation_drawer_list)
    ListView mDrawerListView;

    @ViewById(R.id.drawer_layout_update)
    View mDrawerItemUpdate;

    @RestService
    RestClient mRestClient;

    @Pref
    PreferencesHelper_ mPrefs;

    @Extra
    BuildingOld mBuildingForMap;

    private Bundle mFragments = new Bundle();

    private MergeAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private PrimaryDrawerItem mSelectedItem = PrimaryDrawerItem.FAVORITES;

    public enum PrimaryDrawerItem {
        FAVORITES(FavoritesFragment_.class, R.string.drawer_item_favs, R.drawable.ic_account,
                R.drawable.ic_account_checked),
        MAP(LMUMapFragment_.class, R.string.drawer_item_map, R.drawable.ic_map,
                R.drawable.ic_map_checked),
        NEARBY(NearbyFragment_.class, R.string.drawer_item_near, R.drawable.ic_map_marker,
                R.drawable.ic_map_marker_checked);

        public Class<? extends Fragment> fragmentClass;

        public final int titleResId;

        public final int iconResId;

        public final int iconResIdChecked;

        PrimaryDrawerItem(Class<? extends Fragment> fragmentClass, int titleResId, int iconResId,
                          int iconResIdChecked) {
            this.fragmentClass = fragmentClass;
            this.titleResId = titleResId;
            this.iconResId = iconResId;
            this.iconResIdChecked = iconResIdChecked;
        }
    }

    public enum SecondaryDrawerItem {
        SETTINGS(R.string.drawer_item_settings),
        IMPRESSUM(R.string.drawer_item_impressum);

        public final int titleResId;

        SecondaryDrawerItem(int titleResId) {
            this.titleResId = titleResId;
        }
    }

    @AfterViews
    void init() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mSelectedItem.titleResId);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.lmu_navigator);
            }

            // TODO: animate title change in onDrawerSlide() (nice-to-have)
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        setDrawerItems();

        mSelectedItem = (mBuildingForMap == null) ? PrimaryDrawerItem.FAVORITES : PrimaryDrawerItem.MAP;
        setTitle(mSelectedItem.titleResId);
        mDrawerListView.setItemChecked(mSelectedItem.ordinal(), true);

        Bundle args = null;
        if (mSelectedItem == PrimaryDrawerItem.MAP) {
            args = new Bundle(1);
            args.putParcelable(LMUMapFragment_.M_SELECTED_BUILDING_ARG, mBuildingForMap);
        }
        showFragment(mSelectedItem, args);

        checkVersion();
    }

    private void setDrawerItems() {
        mDrawerAdapter = new MergeAdapter();
        mDrawerAdapter.addAdapter(new PrimaryDrawerAdapter(this));
        View divider = LayoutInflater.from(this).inflate(R.layout.divider_drawer, mDrawerListView, false);
        mDrawerAdapter.addView(divider, false);
        mDrawerAdapter.addAdapter(new SecondaryDrawerAdapter(this));
        mDrawerListView.setAdapter(mDrawerAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO: pass to active fragment!
    }

    @Background
    void checkVersion() {
        // for testing
        //mPrefs.dataVersion().remove();
        //mPrefs.updatePending().remove();
        //mPrefs.updatePending().put(false);

        if (mPrefs.updatePending().get()) {
            mDrawerItemUpdate.setVisibility(View.VISIBLE);
            return;
        }

        mDrawerItemUpdate.setVisibility(View.GONE);
        try {
            int serverVersion = mRestClient.getVersion().version;
            if (serverVersion > mPrefs.dataVersion().get()) {
                showUpdateDialog();
            }
        } catch (ResourceAccessException e) {
            Log.i(LOG_TAG, "Could not check data version! Cause:\n" + e.getMessage());
        }
    }

    @UiThread
    void showUpdateDialog() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(R.string.update_dialog_title)
                .setMessage(R.string.update_dialog_message)
                .setCancelable(false)
                .setCancelableOnTouchOutside(false)
                .setPositiveButtonText(R.string.update_dialog_accept)
                .setNegativeButtonText(R.string.update_dialog_postpone)
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int i) {
        UpdateActivity_.intent(this).startForResult(REQUEST_CODE_UPDATE);
    }

    @Override
    public void onNegativeButtonClicked(int i) {
        mPrefs.updatePending().put(true);
        mDrawerItemUpdate.setVisibility(View.VISIBLE);
        // TODO: show hint! open drawer?
    }

    @Override
    public void onNeutralButtonClicked(int i) {
        // ignore
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.search) {
            SearchBuildingActivity_.intent(this).startForResult(REQUEST_CODE_SEARCH_BUILDING);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnActivityResult(REQUEST_CODE_SEARCH_BUILDING)
    void onSearchResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            BuildingOld building = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
            BuildingDetailActivity_.intent(this)
                    .mBuilding(building)
                    .start();
        } else {
            Log.w(LOG_TAG, "Search returned with unknown result code");
        }
    }
    @OnActivityResult(REQUEST_CODE_UPDATE)
    void onUpdateResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            int newVersion = data.getIntExtra(UpdateActivity.RESULT_EXTRA_NEW_VERSION, 0);
            mPrefs.dataVersion().put(newVersion);
            mPrefs.updatePending().put(false);
            mDrawerItemUpdate.setVisibility(View.GONE);
        } else {
            mPrefs.updatePending().put(true);
            mDrawerItemUpdate.setVisibility(View.VISIBLE);
        }
    }

    @OnActivityResult(REQUEST_CODE_ADD_FAVORITE)
    void onAddFavoriteResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            BuildingOld building = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
            building.setFavorite(true);
        } else {
            Log.w(LOG_TAG, "Search returned with unknown result code");
        }
    }

    private void showFragment(PrimaryDrawerItem item, Bundle args) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment fragment = manager.findFragmentByTag(item.fragmentClass.getName());
        if (fragment != null) {
            transaction.attach(fragment);
        } else {
            fragment = Fragment.instantiate(this, item.fragmentClass.getName());
            fragment.setArguments(args);
            transaction.add(R.id.content_frame, fragment, item.fragmentClass.getName());
        }

        Fragment currentFragment = manager.findFragmentById(R.id.content_frame);
        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }

        transaction.commit();
    }

    @ItemClick(R.id.navigation_drawer_list)
    void onDrawerItemClicked(int position) {
        Object item = mDrawerAdapter.getItem(position);
        if (item instanceof PrimaryDrawerItem) {
            onPrimaryItemClicked((PrimaryDrawerItem) item);
        } else if (item instanceof SecondaryDrawerItem) {
            onSecondaryItemClicked((SecondaryDrawerItem) item);
        }
        mDrawerLayout.closeDrawers();
    }

    @Click(R.id.drawer_item_update)
    void onUpdateClicked() {
        mDrawerLayout.closeDrawers();
        showUpdateDialog();
    }

    private void onPrimaryItemClicked(PrimaryDrawerItem item) {
        if (mSelectedItem == item) {
            return;
        }
        showFragment(item, null);
        mSelectedItem = item;
    }

    private void onSecondaryItemClicked(SecondaryDrawerItem item) {
        Toast.makeText(this, item.titleResId, Toast.LENGTH_SHORT).show();
        switch (item) {
            case SETTINGS:
                // TODO
                break;
            case IMPRESSUM:
                // TODO
                break;
        }
    }

}
