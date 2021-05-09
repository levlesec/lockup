package com.lockup;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

// https://stackoverflow.com/questions/7690350/android-start-service-on-boot
public class BootNoticeReceiver extends BroadcastReceiver {

    // https://stackoverflow.com/questions/17588910/check-if-service-is-running-on-android?lq=1
    // getRunningServices is deprecated. need a replacement
    private boolean isServiceRunning(Class serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isServiceRunning(LockUpService.class, context)) {
            Intent lu_svc = new Intent(context, LockUpService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("LockUpBoot", "Starting the LockUp service.");
                context.startForegroundService(lu_svc);
            } else {
                Log.d("LockUpBoot", "Starting the LockUp service.");
                context.startService(lu_svc);
            }
            Intent lu_pls_svc = new Intent(context, LockUpPlausibleService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("LockUpBoot", "Starting the LockUp Plausible service.");
                context.startForegroundService(lu_pls_svc);
            } else {
                Log.d("LockUpBoot", "Starting the LockUp Plausible service.");
                context.startService(lu_pls_svc);
            }
        } else {
            Log.d("LockUpBoot", "The service is already running.");
        }
    }
}
