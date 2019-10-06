package de.lmu.navigator.preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import androidx.appcompat.app.AlertDialog;
import android.webkit.WebView;
import android.widget.Toast;

import de.lmu.navigator.R;
import de.psdev.licensesdialog.LicensesDialog;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final PreferenceScreen prefs = getPreferenceScreen();

        prefs.findPreference(Preferences.KEY_ABOUT).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showAbout();
                        return true;
                    }
                });

        prefs.findPreference(Preferences.KEY_FEEDBACK).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        sendFeedback();
                        return true;
                    }
                });

        prefs.findPreference(Preferences.KEY_LICENSES).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showLicences();
                        return true;
                    }
                });
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:lmu.roomfinder@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.prefs_feedback_email_subject));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.prefs_feedback_no_app_msg, Toast.LENGTH_LONG).show();
        }
    }

    private void showAbout() {
        final WebView webView = new WebView(getActivity());
        webView.loadUrl("file:///android_asset/about.html");

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.prefs_about_title)
                .setPositiveButton(R.string.prefs_about_close, null)
                .setView(webView)
                .show();
    }

    private void showLicences() {
        new LicensesDialog.Builder(getActivity())
                .setNotices(R.raw.licences)
                .setTitle(R.string.prefs_licences_title)
                .build()
                .show();
    }
}
