package com.wifiattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.wifiattendance.BeaconDataModal;


public class ScanningBroadcastReceiver extends BroadcastReceiver {

    private BeaconDataModal mNearestBeaconDevice = null;
    private int mMaxRssi;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        switch (intent.getAction()) {
            case "DeviceDiscoveredAction":
                Toast.makeText(context, intent.getStringExtra("device_id"), Toast.LENGTH_LONG).show();
                break;
            case "restartservice":
                Toast.makeText(context, "Service Restarted", Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, BackgroundScanService.class));
                } else {
                    context.startService(new Intent(context, BackgroundScanService.class));
                }
                break;

        }

    }
}
