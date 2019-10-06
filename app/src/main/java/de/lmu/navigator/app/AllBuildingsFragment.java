package de.lmu.navigator.app;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import de.lmu.navigator.R;
import de.lmu.navigator.database.model.Building;
import io.realm.RealmResults;

public class AllBuildingsFragment extends BaseFragment implements BuildingsAdapter.OnBuildingClickedListener {
    private static final String LOG_TAG = AllBuildingsFragment.class.getSimpleName();

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RealmResults<Building> buildings = ((MainActivity) getActivity()).getBuildings();
        AllBuildingsAdapter adapter = new AllBuildingsAdapter(getActivity(), buildings, true);
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
