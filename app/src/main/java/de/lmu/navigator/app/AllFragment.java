package de.lmu.navigator.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import io.realm.RealmResults;

public class AllFragment extends BaseFragment implements BuildingAdapter.OnBuildingClickedListener {
    private static final String LOG_TAG = AllFragment.class.getSimpleName();

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RealmResults<Building> buildings = ((TabActivity) getActivity()).getBuildings();
        AllAdapter adapter = new AllAdapter(getActivity(), buildings, true);
        adapter.setOnBuildingClickListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onBuildingClicked(Building building) {
        startActivity(BuildingDetailActivity.newIntent(getActivity(), building.getCode()));
    }
}
