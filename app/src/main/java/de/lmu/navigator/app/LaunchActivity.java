package de.lmu.navigator.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.lmu.navigator.R;
import de.lmu.navigator.database.UpdateService;
import me.alexrs.prefs.lib.Prefs;

public class LaunchActivity extends AppCompatActivity {

    private static final int NO_DATA = -1;

    @Bind(R.id.message)
    TextView mTextMessage;

    private LocalBroadcastManager mBroadcastManager;
    private IntentFilter mIntentFilter;
    private boolean mUpdateRunning = false;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(UpdateService.ACTION_SUCCESS)) {
                onUpdateSuccess();
            } else if (action.equals(UpdateService.ACTION_ERROR)) {
                Throwable t = (Throwable) intent.getSerializableExtra(UpdateService.EXTRA_THROWABLE);
                onUpdateError(t);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shouldUpdate()) {
            setContentView(R.layout.activity_launch);
            ButterKnife.bind(this);

            int currentDataVersion = Prefs.with(this).getInt(Preferences.DATA_VERSION, NO_DATA);
            if (currentDataVersion == NO_DATA) {
                mTextMessage.setText(R.string.launch_db_init);
            } else {
                mTextMessage.setText(R.string.launch_db_update);
            }

            mBroadcastManager = LocalBroadcastManager.getInstance(this);
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(UpdateService.ACTION_SUCCESS);
            mIntentFilter.addAction(UpdateService.ACTION_ERROR);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldUpdate()) {
            startService(new Intent(this, UpdateService.class));
            mBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
            mUpdateRunning = true;
        } else if (!isFinishing()) {
            mUpdateRunning = false;
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUpdateRunning) {
            mBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void onUpdateSuccess() {
        startActivity(new Intent(LaunchActivity.this, MainActivity.class));
        finish();
    }

    private void onUpdateError(Throwable e) {
        Toast.makeText(LaunchActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        Log.e(LaunchActivity.class.getSimpleName(), "Error loading database!", e);

        // Log to crashlytics
        Crashlytics.log("Loading database from assets failed!");
        Crashlytics.logException(e);
    }

    private boolean shouldUpdate() {
        return Prefs.with(this).getInt(Preferences.DATA_VERSION, NO_DATA)
                != Preferences.SHIPPED_DATA_VERSION;
    }
}
