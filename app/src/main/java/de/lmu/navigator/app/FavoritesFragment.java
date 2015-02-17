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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.DatabaseManager;
import de.lmu.navigator.database.RealmDatabaseManager;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.search.SearchBuildingActivity_;

public class FavoritesFragment extends ListFragment implements LocationListener {
    // TODO: add nice empty view
    // TODO: add load spinner
    // TODO: fix empty view showing up on start

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @InjectView(R.id.fab)
    FloatingActionButton mActionButton;

    private DatabaseManager mDatabaseManager;
    private List<Building> mFavBuildings;
    private FavoritesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseManager = new RealmDatabaseManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        mActionButton.attachToListView(getListView());

        mFavBuildings = mDatabaseManager.getStarredBuildings(true);
        mAdapter = new FavoritesAdapter(getActivity(), mFavBuildings);
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO
        /*
        BuildingDetailActivity_.intent(getActivity())
                .mBuilding(mAdapter.getItem(position))
                .start();
        */
    }

    @OnClick(R.id.fab)
    void addFavorite() {
        SearchBuildingActivity_.intent(getActivity()).startForResult(MainActivity.REQUEST_CODE_ADD_FAVORITE);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO
    }
}
