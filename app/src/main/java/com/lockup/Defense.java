package com.lockup;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Defense extends AppCompatActivity {

    Context context;

    Defense(Context denfeseContext) {
        context = denfeseContext;
    }

    DevicePolicyManager devicePolicyManager;

    public void protect_device_run() {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, AdminReceiver.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String response = preferences.getString("desiredResponse", "Factory Reset");
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putBoolean("compromised", true);
        prefEditor.apply();
        if (active) {
             // try to wipe and then lock
             switch (response) {
                case "Lock":
                    devicePolicyManager.lockNow();
                case "Factory Reset":
                    devicePolicyManager.wipeData(0);
                    devicePolicyManager.lockNow();
                default:
                     devicePolicyManager.wipeData(0);
                     devicePolicyManager.lockNow();
            }
        } else {
            Log.d("LockUp", "Unable to properly defend this device. Failing open.");
        }
    }
}
