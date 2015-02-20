package de.lmu.navigator.app;

import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.common.eventbus.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Version;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.outdoor.BuildingDetailActivity_;
import de.lmu.navigator.outdoor.LMUMapFragment_;
import de.lmu.navigator.search.AbsSearchActivity;
import de.lmu.navigator.search.SearchBuildingActivity_;
import de.lmu.navigator.update.RestService;
import de.lmu.navigator.update.RetrofitRestClient;
import de.lmu.navigator.update.UpdateService;
import me.alexrs.prefs.lib.Prefs;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends LocationActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_CODE_SEARCH_BUILDING = 1;
    public static final int REQUEST_CODE_ADD_FAVORITE = 2;

    public static final String EXTRA_BUILDING_FOR_MAP = "EXTRA_BUILDING_FOR_MAP";

    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.navigation_drawer_list)
    ListView mDrawerListView;

    @InjectView(R.id.drawer_layout_update)
    View mDrawerItemUpdate;

    private MergeAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    private MaterialDialog mUpdateProgressDialog;

    private PrimaryDrawerItem mSelectedItem = PrimaryDrawerItem.FAVORITES;

    public enum PrimaryDrawerItem {
        FAVORITES(FavoritesFragment.class, R.string.drawer_item_favs, R.drawable.ic_account,
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

    public static Intent newIntent(Context context, BuildingOld buildingForMap) {
        return new Intent(context, MainActivity.class)
                .putExtra(EXTRA_BUILDING_FOR_MAP, buildingForMap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        // TODO: registerSticky()? move to onResume?
        EventBus.getDefault().register(this);

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

        Bundle args = null;
        if (getIntent().hasExtra(EXTRA_BUILDING_FOR_MAP)) {
            BuildingOld buildingForMap = getIntent().getExtras().getParcelable(EXTRA_BUILDING_FOR_MAP);
            mSelectedItem = PrimaryDrawerItem.MAP;
            args = new Bundle(1);
            args.putParcelable(LMUMapFragment_.M_SELECTED_BUILDING_ARG, buildingForMap);
        } else {
            mSelectedItem = PrimaryDrawerItem.FAVORITES;
        }

        setTitle(mSelectedItem.titleResId);
        mDrawerListView.setItemChecked(mSelectedItem.ordinal(), true);
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

    private void checkVersion() {
        // for testing
        Prefs.with(this).remove(Preferences.DATA_VERSION);
        Prefs.with(this).remove(Preferences.UPDATE_PENDING);

        if (Prefs.with(this).getBoolean(Preferences.UPDATE_PENDING, false)) {
            mDrawerItemUpdate.setVisibility(View.VISIBLE);
            return;
        }

        mDrawerItemUpdate.setVisibility(View.GONE);
        RestService restService = RetrofitRestClient.create();
        restService.getVersionAsync(new Callback<Version>() {
            @Override
            public void success(Version version, Response response) {
                if (version.version > Prefs.with(MainActivity.this).getInt(Preferences.DATA_VERSION,
                        Preferences.SHIPPED_DATA_VERSION)) {
                    showUpdateDialog();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(LOG_TAG, "Could not check data version! Cause:\n" + error.getMessage());
            }
        });
    }

    private void showUpdateDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.update_dialog_title)
                .content(R.string.update_dialog_message)
                .cancelable(false)
                .positiveText(R.string.update_dialog_accept)
                .negativeText(R.string.update_dialog_postpone)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        startService(new Intent(MainActivity.this, UpdateService.class));
                        showUpdateProgressDialog();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        Prefs.with(MainActivity.this).save(Preferences.UPDATE_PENDING, true);
                        mDrawerItemUpdate.setVisibility(View.VISIBLE);
                        // TODO: show hint! open drawer?
                    }
                })
                .show();
    }

    private void showUpdateProgressDialog() {
        mUpdateProgressDialog = new MaterialDialog.Builder(this)
                .content(R.string.update_progress_message)
                .progress(true, 0)
                .cancelable(false)
                .negativeText(R.string.update_progress_cancel)
                .autoDismiss(false) // will be dismissed in the callback
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        onUpdateFinished(false);
                    }
                })
                .show();
    }

    private void onUpdateFinished(boolean success) {
        if (success) {
            mUpdateProgressDialog.dismiss();
            mDrawerItemUpdate.setVisibility(View.GONE);
            new MaterialDialog.Builder(this)
                    .title(R.string.update_success_title)
                    .content(R.string.update_success_message)
                    .positiveText(R.string.update_button_ok)
                    .show();
        } else {
            mUpdateProgressDialog.dismiss();
            mDrawerItemUpdate.setVisibility(View.VISIBLE);
            new MaterialDialog.Builder(this)
                    .title(R.string.update_failure_title)
                    .content(R.string.update_failure_message)
                    .positiveText(R.string.update_button_ok)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SEARCH_BUILDING:
                if (resultCode == RESULT_OK) {
                    onSearchResult(data);
                }
                break;

            case REQUEST_CODE_ADD_FAVORITE:
                if (resultCode == RESULT_OK) {
                    onAddFavoriteResult(data);
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onSearchResult(Intent data) {
        BuildingOld building = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
        BuildingDetailActivity_.intent(this)
                .mBuilding(building)
                .start();
    }

    private void onAddFavoriteResult(Intent data) {
        BuildingOld building = data.getParcelableExtra(AbsSearchActivity.KEY_SEARCH_RESULT);
        building.setFavorite(true);
    }

    @Subscribe
    public void onEventMainThread(UpdateService.UpdateSuccessEvent e) {
        onUpdateFinished(true);
    }

    @Subscribe
    public void onEventMainThread(UpdateService.UpdateFailureEvent e) {
        onUpdateFinished(false);
    }

    @Subscribe
    public void onEventMainThread(UpdateService.UpdateCancelEvent e) {
        onUpdateFinished(false);
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

    @OnItemClick(R.id.navigation_drawer_list)
    void onDrawerItemClicked(int position) {
        Object item = mDrawerAdapter.getItem(position);
        if (item instanceof PrimaryDrawerItem) {
            onPrimaryItemClicked((PrimaryDrawerItem) item);
        } else if (item instanceof SecondaryDrawerItem) {
            onSecondaryItemClicked((SecondaryDrawerItem) item);
        }
        mDrawerLayout.closeDrawers();
    }

    @OnClick(R.id.drawer_item_update)
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

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
