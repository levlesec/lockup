package com.lockup;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Process;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

    /*
        This is a proof of concept and a thought experiment. Do not run this on your personal
        device. This _will_ expose not just your plausible deniability password to LockUp but also
        _your_real_password_. Do you want your _real_password_ exposed to LockUp? Probably not.

                                                                            - Level
    */

public class LockUpPlausibleService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.contains("accessibility") && !preferences.getBoolean("accessibility", false)) {
            Log.d("LockUpPlausibleService","Enabled accessibility service.");
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putBoolean("accessibility", true);
            prefEditor.apply();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.contains("accessibility") && preferences.getBoolean("accessibility", false)) {
            Log.d("LockUpPlausibleService","Disabled accessibility service.");
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putBoolean("accessibility", false);
            prefEditor.apply();
        }
    }

    Integer pointsHave = 0;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.getBoolean("accessibility", false) && preferences.getBoolean("plausible", false)) {
            String deniabilityPw = preferences.getString("deniabilityPw", "LockUp");
            Integer pointsNeeded = deniabilityPw.length() - 1;
            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            boolean isAwake = (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());
            if (isAwake) {
                // inKeyguardRestrictedInputMode is deprecated now. xref: https://developer.android.com/reference/android/app/KeyguardManager#inKeyguardRestrictedInputMode()
                // Maybe a good replacement isDeviceLocked. xref: https://developer.android.com/reference/android/app/KeyguardManager#isDeviceLocked()
                KeyguardManager keyMgr = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
                if (keyMgr.inKeyguardRestrictedInputMode()) {
                    switch (event.getEventType()) {
                        case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                            char[] txtCharArray = event.getText().toString().toCharArray();
                            char[] deniabilityPwCharArray = deniabilityPw.toCharArray();
                            for (int i = 0; i < deniabilityPwCharArray.length; i++) {
                                try {
                                  if (Integer.valueOf(deniabilityPwCharArray[i]).equals(Integer.valueOf(txtCharArray[i + 1]))) {
                                        pointsHave++;
                                        if (pointsHave >= pointsNeeded) {
                                            Defense defense = new Defense(getApplicationContext());
                                            defense.protect_device_run();
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                    }
                } else {
                    // phone unlocked
                    pointsHave = 0;
                }
            }
        }
    }

    LockUpPlausibleServiceHandler LockUpPlausibleServiceHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerthread = new HandlerThread("LockUpPlausibleSvcThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        Looper looper = handlerthread.getLooper();
        LockUpPlausibleServiceHandler = new LockUpPlausibleServiceHandler(looper);
    }
    private final class LockUpPlausibleServiceHandler extends Handler {
        public LockUpPlausibleServiceHandler(Looper looper) {
            super(looper);
        }
    }
}
