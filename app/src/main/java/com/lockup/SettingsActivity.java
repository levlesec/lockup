package com.lockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

    public static class PrefFrag extends PreferenceFragment
    {

        SwitchPreference plausibleLock;
        SwitchPreference notifyUser;
        EditTextPreference deniabilityPw;
        ListPreference desiredResponse;

        Context configContext;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.configContext = context;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            desiredResponse = (ListPreference)findPreference("desiredResponse");
            plausibleLock = (SwitchPreference)findPreference("plausible");
            deniabilityPw = (EditTextPreference)findPreference("deniabilityPw");
            notifyUser = (SwitchPreference)findPreference("notifyUser");

            final SharedPreferences prefCheck = PreferenceManager.getDefaultSharedPreferences(configContext);
            if (!prefCheck.contains("initialized")) {
                SharedPreferences.Editor prefEditor = prefCheck.edit();
                prefEditor.putString("desiredResponse", "Factory Reset");
                prefEditor.putString("deniabilityPw", getResources().getString(R.string.deniabilityPw_default));
                prefEditor.putBoolean("plausible", false);
                prefEditor.putBoolean("accessibility", false);
                prefEditor.putBoolean("runAtBoot", true);
                prefEditor.putBoolean("compromised", false);
                prefEditor.putBoolean("initialized", false);
                prefEditor.putBoolean("notifyUser", true);
                prefEditor.apply();
            }

            if (plausibleLock.isChecked()) { deniabilityPw.setEnabled(true); }
            plausibleLock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(configContext);
                    SharedPreferences.Editor prefEditor = preferences.edit();
                    SwitchPreference plausiblePref = (SwitchPreference)preference;
                    try {
                        Boolean checked = plausiblePref.isChecked();
                        prefEditor.putBoolean("plausible",checked);
                        prefEditor.apply();
                        if (checked) {
                            deniabilityPw.setEnabled(true);
                            if (preferences.contains("accessibility") && !preferences.getBoolean("accessibility", false)) {
                                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivity(intent);
                            }
                        } else {
                            deniabilityPw.setEnabled(false);
                        }
                    } catch (Exception e) {
                        Log.d("SettingsActivity","Could not set the plausible deniability settings Reason: " + e);
                    }
                    return true;
                }
            });

            deniabilityPw.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(configContext);
                    SharedPreferences.Editor prefEditor = preferences.edit();
                    try {
                        prefEditor.putString("deniabilityPw",newValue.toString());
                        prefEditor.commit();
                        prefEditor.apply();
                    } catch (Exception e) {
                        Log.d("SettingsActivity","Could not update the deniability password. Reason: " + e);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFrag()).commit();
    }

}
