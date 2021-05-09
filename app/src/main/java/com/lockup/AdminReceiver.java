package com.lockup;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d("LockUpAdmin","Device admin has been enabled.");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.d("LockUpAdmin","Device admin has been disabled.");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        DevicePolicyManager deviceManger = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceManger.lockNow();
        return "Thank you for choosing LockUp. Please come back again!";
    }

}
