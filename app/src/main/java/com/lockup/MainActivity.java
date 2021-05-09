package com.lockup;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView compromisedFlag = findViewById(R.id.compromised_flag);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.contains("compromised")) {
            if (preferences.getBoolean("compromised", false)) {
                compromisedFlag.setVisibility(View.VISIBLE);
            }
        }

        Button pref = findViewById(R.id.pref_button);
        pref.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(MainActivity.this, AdminReceiver.class);
        boolean adminActive = devicePolicyManager.isAdminActive(componentName);
        if (!adminActive) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.deviceAdminMsg));
            startActivity(intent);
        }
        Intent lu_svc = new Intent(this, LockUpService.class);
        if (!isServiceRunning(LockUpService.class)) {
            startService(lu_svc);
        }
        Intent lu_pls_svc = new Intent(this, LockUpPlausibleService.class);
        if (!isServiceRunning(LockUpPlausibleService.class)) {
            startService(lu_pls_svc);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private boolean isServiceRunning(Class serviceClass) {
        // getRunningServices deprecated. xref: https://developer.android.com/reference/android/app/ActivityManager#getRunningServices(int)
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
