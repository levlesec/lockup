package com.lockup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    /*
        This is a proof of concept and a thought experiment. Do not run this on your personal
        device.
                                                                            - Level
    */


public class LockUpService extends Service {

    boolean isRunning = true;
    Looper looper;
    LockUpServiceHandler LockUpServiceHandler;
    Context context;
    Defense defense;

    public LockUpService(Context serviceContext) {
        context = serviceContext;
    }

    public LockUpService() {
    }

    String[] CB_ELEVATOR_HASHES = new String[] {
            "df89c654afaad67ac7e85e5d34072b5463ea0849ef3e8462a13e299baa6aa6dc",
            "a944fd9e66d6b56bd031a3236b6ae8daee4b90e4689f167c22f7c0d02eaa3a98",
            "b5804223bdf91099de465a9cdc3d6bc8aec01f19a8856f6e9bdaec3a2a941035",
            "ab8130caac25334f2ae46781e4ef625020bd95b215a427785798c729d81fc410",
            "5e8dce4e63214099c35f342c6c5a548be30ed6341ec9f4655827f28473333b6b",
            "9da883c6999f2f333ab91631946984893fc51163e0c109e8683131b4775accdb",
            "49b7368bd28c936ac824bf6040101f57f2c42ce4e9e16877dab2f44922683213",
            "ca52579b3ded35fc8337ff9763634c4bf5407a97c85d0c03fb3de16b22d7638b",
            "37178f4d2711fc132b8ef19499c1517a70f73d29dc204116a85f5e6caa025034",
            "bbe87d1cd44869ee1ba8dbca4a671965765fcd5818f16efa434bbe972961de1c",
            "3541425a729454569dccdf713f84102a8b2efeeb77abd34a6c210dd4b9b61cf4",
            "6a14b252b01f7ddc21a20a4a9577cb76c82f459652845959040eae677c0df3ce",
            "47e7ddbadcb1ec5c228413e93baa029e06f24f44e77a83e817ba48770ffddd10",
            "08853c949bbc98b9e334ae52d6d344f219e225914abaf7e813c81307a8e3bcb5",
            "856ef82bb6f1e7d940be20a049a3566842a0c9b2c8635d646fdd487a00bd051a",
            "859192e3d697a8522a7f9c19088d125b723193ce27df348712701ee78aea286c",
            "c58a06ba1b0be01c564629f76f496fc3b7e2d971c807ab6fe06ade291fafb3cc",
            "872944fcf79fb92eb5f4134d87b6f6caabc5dec919070a1109ee820442942250",
            "ab9bc4c15b1589e45dcf9c604617eaa67034c388c8b8df3af7c71dc50b29bef5",
            "23d9ce4a7df20b28d0b5b24ac320a9643c1716987dd5f29110bff71f775dca8d",
            "d3aa3c7b75e986d2addf4364f90fb948addee91deade17765a383ec69f582806",
            "07e3923c52e1c73e6ba314e230605e2ddbb5033aab75495654c131ff8b1b32a6",
            "829dd6f9fc7b39d16b3f75428b3b8417e3b411a9dbb422030f4a2d0e93dbbe3f",
            "17afd50ea379a6669e59af03fa73af816f3947ab0bc979b41e8828916c63dc49",
            "39560ebb98e6973c88e4a92fb20e5b216616e4c7f607432329f26538aff77694",
            "96ea633fe332a30f65e80e1c48ca9a0158b6e02c33afcb4d05d9d96d62135aab",
            "962390deb44b84d5781c5f0868aaea7f55d374b2db161598bebe3dd90abc1564",
            "1226873d2fac892c3187b7a7da3a1c4b2a5c31d7ecdcd0c18f7ecba4197b05f0",
            "3c6dee8fcf4a47c70be5b2b1af6a84c7b54f4941b2a341f23547f1a1db64d034",
            "064af61a468a719291779c2aab1d8fd152e66d422b402ebf112c5d896f268a52",
            "6a4e49d86f471cd5be05af0737fcc8a7f58922c591c0d03c446bd0cf3bb0d8cb",
            "6bea0ed340e33aa457ca91a986ed266906e3639bccf7ee6cdf1597ba683d25cb",
            "e228cf22fa4934168fbde171e67729c8774df3813c1d31d9e5e206a19097ab87",
            "2a797d28f44e9865d0dc0a9b7393b62e468fde03a8e1982ddb32142f7570b23b",
            "f9ca7565b557f315a9a4fc0d77571206ba8471713160784b570bebc541a29fac",
            "cac5fdb8be75968291b42ab75dd654b3b21cf5dd1a5539490e6e8e015cb2a4ff",
            "5a14004e4c1a67acfd124ff21703b78ef3710c8e81677013234b4deb6af7da66",
            "97496d8bbfb71bff2ea121ae731da8e474d1cdd6aabdb2bf1d592afe3bd649de",
            "7b6343e560c51c0152af0479a8cd9e7c74b9464f16deaaf07a7263f3b876c6e2",
            "98e2efe999a859531af4940b4fcc63725f9b84f0c14645c32fd7def907e03030",
            "945a19f4a22daab9cdea1686971a0215f777d3b19f5ee45ffcf846b3a0638cd3",
            "7e898e7d0f04e798954f285d50feb44f9697b47a443bfac27a7bedee52942bf6",
            "5de2e8d08d246a7bb5c8ddd020853dbb5896cec4149527fa9256a786ebf3541d",
            "460ba81b6dc17efe179cf7d0617ff99dc66be3be4375348f1c97a88dfa4f39ea",
            "7546f3e3e6bfd3cc3b0d91f7883e39a1c9cd39be7a3a2c72cd76d9ca5378c540",
            "5c3d38ad5f26606ca19d277ee7392b039555449ac66cc96e9ad2c818857a3b31",
            "a4ea82cd91d99bf26ca19dfd4b1969c42b5551151f1d3bdb635ebb2567e5a741",
            "6c6909839af1076330fdfd64a82d2209030fcfd3f7819acbfccc7697d7d3b5ae",
            "46a202a88af8c7208d552904cd16d28580a24830011a536c431703848c5739fb",
            "a8212687308d7067af3d25664e84ace7f84fd939914a96a1c47e67c613c7b225",
            "a75788739e8f97fd9901496f2a69d5b6b24892992c106a1e8ee06faa25076c18",
            "7efdbc53f31c24bfe0046452e902be8fd7a500aae1a97d0d554ebe2bf8f2f9d8",
            "2db036febbbad09586eac6279a4de4852c353444c4cacb7f926605d174cc7e6e",
            "8e13aff69bc075116194f9d4e2dc7f2ef38ab502903649b4da8b9fc9ce2347e4",
            "6c66e32cc3c16dca941b9322e21463bb57c99cd7f3909e748fdfa671171cf66d",
            "3254e75112c3d219e1af7bbb7be73d2cea76e4786aa9678683bf8d77f1ffbde2",
            "0519d111df96968e376a0dc1fb4c37eef35829dd0f472953e71e19d05b15eb47",
            "ffc118fc66a09e5a421f2aa5a036c0b49fb178dc9a18d847592d68ddf21cbd1e",
            "93028ad412cfc3a792614596d5db155470840bb5614eb416d9e3265b959fc95c",
            "a2ea5e0d94abb55ade88a6ffe40a73f49329347c96d334c58426e1bc0c41e72b"
    };

    String[] CB_ELEVATOR_DIRS = new String[] {
            "/data/local/tmp",
            "/data/local/tmp/cb",
            "/data/app-asec/data/local/tmp",
            "/cblr"
    };

    String[] CB_ELEVATOR_NAMES = new String[] {
            "nandreadStatic_7180",
            "pingroot_vultest",
            "DisableHuaweiLogging_2.1.5767a",
            "nandread64-pie-vold",
            "autonomous_app.apk",
            "exploits_2.1.5769.csv",
            "forensics",
            "nandreadStatic_1788",
            "rootspot_verify_env",
            "EnableHuaweiLogging_2.1.5767a",
            "frida_script_obfuscated.js",
            "manifest.webapp",
            "c2a_disable_selinux_64.ko",
            "EnableSharpRead_2.1.5767a",
            "com.mr.meeseeks.apk",
            "nandreadPie_7182",
            "salamtak32",
            "pingroot",
            "zergRush_2.1.5767a",
            "psneuter_2.1.5767a",
            "shellcode_32_iptables.bin",
            "dirtycow_32",
            "nandreadPie_1788",
            "shellcode_32_oatdump.bin",
            "django_2.1.5767a",
            "fourrunnerStatic_2.1.5767a",
            "index.html",
            "rosecure_2.1.5767a",
            "patcher.exe",
            "nandd",
            "c2a_disable_selinux_32.ko",
            "dirtycow",
            "setuid_2.1.5767a",
            "RecoveryImageMap.csv",
            "salamtak64",
            "gb_2.1.5767a",
            "nandreadStatic_7182",
            "nandread-pie_7182",
            "shellcode.bin",
            "nandread-pie-vold",
            "daemonize",
            "adbd.bin",
            "nandreadPie_7181",
            "rootspotter.apk"
    };

    String[] bannedKeys = new String[]{
            "df89c654afaad67ac7e85e5d34072b5463ea0849ef3e8462a13e299baa6aa6dc",
            "a4ea82cd91d99bf26ca19dfd4b1969c42b5551151f1d3bdb635ebb2567e5a741",
            "6a14b252b01f7ddc21a20a4a9577cb76c82f459652845959040eae677c0df3ce",
            "f9ca7565b557f315a9a4fc0d77571206ba8471713160784b570bebc541a29fac",
            "5a14004e4c1a67acfd124ff21703b78ef3710c8e81677013234b4deb6af7da66",
            "49b7368bd28c936ac824bf6040101f57f2c42ce4e9e16877dab2f44922683213",
            "97496d8bbfb71bff2ea121ae731da8e474d1cdd6aabdb2bf1d592afe3bd649de",
            "93028ad412cfc3a792614596d5db155470840bb5614eb416d9e3265b959fc95c",
            "856ef82bb6f1e7d940be20a049a3566842a0c9b2c8635d646fdd487a00bd051a",
            "945a19f4a22daab9cdea1686971a0215f777d3b19f5ee45ffcf846b3a0638cd3",
            "962390deb44b84d5781c5f0868aaea7f55d374b2db161598bebe3dd90abc1564"
    };

    Map<Integer,String[]> bannedIssuers = new HashMap<Integer,String[]>(){
        {
            put(1,new String[]{"CN=Oleg Beloussov","OU=mobile secure","O=BeloussovOleg ltd.","L=Tel Aviv","ST=Israel","C=092"});
            put(2,new String[]{"C=IL"});
            put(3,new String[]{"CN=Cellebrite"});
            put(4,new String[]{"O=Cellebrite"});
            put(5,new String[]{"CN=mr meeseeks","OU=cell"});
            put(6,new String[]{"C=IL","L=Tel Aviv","O=Cellebrite","OU=Research","CN=Cellebrite Cellebrite"});
            put(7,new String[]{"CN=KYOCERA Corporation", "OU=KYOCERA Corporation", "O=KYOCERA Corporation", "L=Fushimi", "ST=Kyoto", "C=JP"});
            put(8,new String[]{"CN=aaa", "OU=aaa", "O=aaa", "L=aaaaa", "ST=aaa", "C=aaa"});
        }
    };

    private final BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Thread monitor_thread = new Thread() {
                @Override
                public void run() {
                    monitor_staging_dir();
                }
            };
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                    if (!monitor_thread.isAlive()) {
                        monitor_thread.start();
                    }
                case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                    if (monitor_thread.isAlive()) {
                        monitor_thread.interrupt();
                    }
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if (!monitor_thread.isAlive()) {
                        monitor_thread.start();
                    }
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    if (monitor_thread.isAlive()) {
                        monitor_thread.interrupt();
                    }
                default:
                    if (!monitor_thread.isAlive()) {
                        monitor_thread.start();
                    } else {
                        monitor_thread.interrupt();
                    }
            }
        }
    };

    private final BroadcastReceiver mAppInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                PackageManager pm = context.getPackageManager();
                List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo metadata : applications) {
                    Integer count = 0;
                    PackageInfo appInfo = pm.getPackageInfo(metadata.packageName, PackageManager.GET_SIGNATURES);
                    for (Signature appSig : appInfo.signatures) {
                        byte[] signature = appSig.toByteArray();
                        InputStream input = new ByteArrayInputStream(signature);
                        CertificateFactory factory = CertificateFactory.getInstance("X509");
                        X509Certificate x509 = (X509Certificate) factory.generateCertificate(input);
                        MessageDigest md = MessageDigest.getInstance("SHA256");
                        byte[] appPubKey = md.digest(x509.getEncoded());
                        StringBuffer appPubKeyHex = new StringBuffer();
                        for (int i = 0; i < 32; i++) {
                            appPubKeyHex.append(String.format("%02x", appPubKey[i]));
                        }
                        for (String appKey : bannedKeys) {
                            if (appKey.toUpperCase().equals(appPubKeyHex.toString().toUpperCase())) {
                                count++;
                            }
                        }
                        Principal principal_subject = x509.getSubjectDN();
                        String subjectDn = principal_subject.getName();
                        Principal principal_issuer = x509.getIssuerDN();
                        String issuerDn = principal_issuer.getName();
                        for (Map.Entry<Integer,String[]> entry : bannedIssuers.entrySet()) {
                            for (String piece : entry.getValue()) {
                                if (subjectDn.toUpperCase().contains(piece.toUpperCase()) || issuerDn.toUpperCase().contains(piece.toUpperCase())) {
                                    count++;
                                }
                            }
                        }
                        if (count > 0) {
                            defense.protect_device_run();
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("LockUpService","Encountered an exception while evaluating installed applications.");
            }
        }
    };

    private void monitor_staging_dir() {
        while (true) {
            for (String CB_ELEVATOR_DIR : CB_ELEVATOR_DIRS) {
                for (String CB_ELEVATOR_NAME : CB_ELEVATOR_NAMES) {
                    String CB_FULL_PATH = CB_ELEVATOR_DIR + "/" + CB_ELEVATOR_NAME;
                    try {
                        File file = new File(CB_FULL_PATH);
                        if (file.exists()) {
                            byte[] file_content = new byte[(int) new File(file.getAbsolutePath()).length()];
                            FileInputStream file_stream = new FileInputStream(new File(file.getAbsolutePath()));
                            file_stream.read(file_content);
                            file_stream.close();
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            digest.update(file_content);
                            StringBuilder file_hash_sb = new StringBuilder();
                            for (byte b : digest.digest()) {
                                file_hash_sb.append(String.format("%02x", b));
                            }
                            //Log.d("LockUpService",String.format("File: %1$s, File Hash: %2$s ",file.getAbsolutePath(),file_hash_sb.toString()));
                            for (String malware : CB_ELEVATOR_HASHES) {
                                if (malware.equals(file_hash_sb.toString())) {
                                    Log.d("LockUpService","Discovered a malicious file.");
                                    defense.protect_device_run();
                                }
                            }
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        Log.d("LockUpService", String.format("Encountered exception while evaluating %1$s. Reason: %2$s", CB_FULL_PATH, e.getMessage()));
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            defense = new Defense(getApplicationContext());
        } else {
            defense = new Defense(context);
        }
        HandlerThread handlerthread = new HandlerThread("LockUpSvcThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        looper = handlerthread.getLooper();
        LockUpServiceHandler = new LockUpServiceHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
        }

        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appFilter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        appFilter.addDataScheme("package");
        registerReceiver(mAppInstallReceiver, appFilter);

        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        usbFilter.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(mUsbAttachReceiver, usbFilter);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRunning) { isRunning = false; }

        unregisterReceiver(mUsbAttachReceiver);
        unregisterReceiver(mAppInstallReceiver);
        onCreate();
    }

    private final class LockUpServiceHandler extends Handler {
        public LockUpServiceHandler(Looper looper) {
            super(looper);
        }
    }

}

