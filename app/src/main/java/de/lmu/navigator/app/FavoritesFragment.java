package de.lmu.navigator.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.OnClick;
import de.lmu.navigator.R;
import de.lmu.navigator.database.ModelHelper;
import de.lmu.navigator.database.model.Building;
import de.lmu.navigator.search.SearchBuildingActivity;
import io.realm.RealmResults;

public class FavoritesFragment extends BaseFragment
        implements BuildingsAdapter.OnBuildingClickedListener {

    private static final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton mActionButton;

    @BindView(R.id.empty_view)
    View mEmptyView;

    private FavoritesAdapter mAdapter;

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (mAdapter.getItemCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RealmResults<Building> favorites = ((MainActivity) getActivity()).getBuildings()
                .where().equalTo(ModelHelper.BUILDING_STARRED, true)
                .findAll();
        mAdapter = new FavoritesAdapter(getActivity(), favorites, true);
        mAdapter.setOnBuildingClickListener(this);

        if (favorites.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        mAdapter.registerAdapterDataObserver(mDataObserver);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mActionButton.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.unregisterAdapterDataObserver(mDataObserver);
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
