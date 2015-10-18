package de.lmu.navigator.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import butterknife.Bind;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.search.SearchBuildingActivity;
import io.realm.RealmResults;

public class FavoritesFragment extends BaseFragment
        implements BuildingsAdapter.OnBuildingClickedListener {

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    @Bind(R.id.fab)
    FloatingActionButton mActionButton;

    @Bind(R.id.empty_view)
    View mEmptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RealmResults<Building> favorites = ((MainActivity) getActivity()).getBuildings()
                .where().equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        final FavoritesAdapter adapter = new FavoritesAdapter(getActivity(), favorites, true);
        adapter.setOnBuildingClickListener(this);

        if (favorites.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (adapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
                MainActivity.REQUEST_CODE_ADD_FAVORITE);
    }

}
