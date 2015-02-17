package de.lmu.navigator.app;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.location.LocationListener;
import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingOld;
import de.lmu.navigator.outdoor.BuildingDetailActivity_;
import de.lmu.navigator.search.SearchBuildingActivity_;

@EFragment(R.layout.fragment_favs)
public class FavoritesFragment extends ListFragment implements LocationListener {
    // TODO: add nice empty view
    // TODO: add load spinner
    // TODO: fix empty view showing up on start

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @ViewById(R.id.fab)
    FloatingActionButton mActionButton;

    private List<BuildingOld> mFavBuildings;
    private FavoritesAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @AfterViews
    void init() {
        mActionButton.attachToListView(getListView());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    @Background
    void loadFavorites() {
        mFavBuildings = BuildingOld.getFavorites();
        onLoadFinished();
    }

    @UiThread
    void onLoadFinished() {
        mAdapter = new FavoritesAdapter(getActivity(), mFavBuildings);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        BuildingDetailActivity_.intent(getActivity())
                .mBuilding(mAdapter.getItem(position))
                .start();
    }

    @Click(R.id.fab)
    void addFavorite() {
        SearchBuildingActivity_.intent(getActivity()).startForResult(MainActivity.REQUEST_CODE_ADD_FAVORITE);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO
    }
}
