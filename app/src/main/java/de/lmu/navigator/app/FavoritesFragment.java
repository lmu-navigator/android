package de.lmu.navigator.app;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import butterknife.InjectView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.search.SearchBuildingActivity;
import de.lmu.navigator.view.DividerItemDecoration;
import io.realm.RealmResults;

public class FavoritesFragment extends BaseFragment implements FavoritesAdapter.OnBuildingClickedListener {
    // TODO: add nice empty view

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.fab)
    FloatingActionButton mActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RealmResults<Building> favorites =
                (RealmResults<Building>) mDatabaseManager.getStarredBuildings(true);
        FavoritesAdapter adapter = new FavoritesAdapter(getActivity(), favorites, true, this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                R.drawable.divider_with_image_padding));
        mRecyclerView.setAdapter(adapter);

        mActionButton.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onBuildingClicked(Building building) {
        startActivity(BuildingDetailActivity.newIntent(getActivity(), building.getCode()));
    }

    @OnClick(R.id.fab)
    void addFavorite() {
        getActivity().startActivityForResult(SearchBuildingActivity.newIntent(getActivity()),
                TabActivity.REQUEST_CODE_ADD_FAVORITE);
    }
}
