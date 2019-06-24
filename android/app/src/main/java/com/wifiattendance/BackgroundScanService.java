package com.wifiattendance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wifiattendance.BeaconDataModal;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackgroundScanService extends Service {

    public static final String FOREGROUND = "com.attendance.beacon.FOREGROUND";
    public static final String TAG = "BackgroundScanService";
    public static final String ACTION_DEVICE_DISCOVERED = "DeviceDiscoveredAction";
    public static final String ACTION_RESTART_SERVICE = "RestartService";
    public static final String EXTRA_DEVICE = "DeviceExtra";
    public static final String EXTRA_DEVICES_COUNT = "DevicesCountExtra";

    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

    private final Handler handler = new Handler();
    private ProximityManager proximityManager;
    private boolean isRunning; // Flag indicating if service is already running.
    private int devicesCount; // Total dishandlercovered devices count


    int mMaxRssi;
    BeaconDataModal beaconDataModel = null;


    @Override
    public void onCreate() {
        super.onCreate();

        setupProximityManager();
        isRunning = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void setupProximityManager() {
        //Create proximity manager instance
        proximityManager = ProximityManagerFactory.create(this);

        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED);

        //Setting up iBeacon and Eddystone listeners
        proximityManager.setIBeaconListener(createIBeaconListener());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Check if service is already active
        if (isRunning) {
           // Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
            return START_STICKY;
        }
        startScanning();
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
                devicesCount = 0;
                Toast.makeText(BackgroundScanService.this, "Scanning service started.", Toast.LENGTH_SHORT).show();
            }
        });
        //stopAfterDelay();
    }

    private void stopAfterDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proximityManager.disconnect();
                stopSelf();
            }
        }, TIMEOUT);
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
                super.onIBeaconsUpdated(ibeacons, region);

                for (IBeaconDevice device : ibeacons) {
                    BeaconDataModal beacon=new BeaconDataModal(device);
                  //  Log.e("SAMPLE>>",beacon.id);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ScanningBroadcastReceiver br = new ScanningBroadcastReceiver();
                        IntentFilter intentFilter = new IntentFilter(ACTION_DEVICE_DISCOVERED);
                        registerReceiver(br, intentFilter);
//                        Intent intent = new Intent();
//                        intent.setAction(ACTION_DEVICE_DISCOVERED);
//                        intent.putExtra("device_id", device.getUniqueId());
//                        sendBroadcast(intent);

                        BackgroundScanService.this.sendMessage(beacon);

                    } else {
//                        Intent intent = new Intent();
//                        intent.setAction(ACTION_DEVICE_DISCOVERED);
//                        intent.putExtra("device_id", device.getUniqueId());
//                        sendBroadcast(intent);
                        BackgroundScanService.this.sendMessage(beacon);
                    }
                }


            }

            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                super.onIBeaconDiscovered(ibeacon, region);
            }
        };
    }

    private void sendMessage(BeaconDataModal beaconDataModal) {
        try {
            Intent intent = new Intent("BeaconDetectionUpdate");
            intent.putExtra("message", new Gson().toJson(beaconDataModal));
            intent.putExtra("id",beaconDataModal.id);
            Log.e("SAMPLE1>>",beaconDataModal.id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BackgroundScanService.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent();
        intent.setAction(ACTION_RESTART_SERVICE);
        sendBroadcast(intent);

    }
}
