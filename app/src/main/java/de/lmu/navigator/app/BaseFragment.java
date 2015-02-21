package de.lmu.navigator.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import de.lmu.navigator.database.DatabaseManager;

public class BaseFragment extends Fragment {

    protected DatabaseManager mDatabaseManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDatabaseManager = ((BaseActivity) activity).mDatabaseManager;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }
}
