package de.lmu.navigator.search;

import org.androidannotations.annotations.EActivity;

import java.util.List;

import de.lmu.navigator.R;
import de.lmu.navigator.model.BuildingOld;

@EActivity(R.layout.activity_search)
public class SearchBuildingActivity extends AbsSearchActivity {

    @Override
    public List<BuildingOld> getItems() {
        return BuildingOld.getAll();
    }

    @Override
    public int getSearchHintResId() {
        return R.string.search_hint_buildings;
    }
}
