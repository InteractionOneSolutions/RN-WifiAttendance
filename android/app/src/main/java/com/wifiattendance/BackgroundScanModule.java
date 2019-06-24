package com.wifiattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;

import java.util.Date;

import javax.annotation.Nonnull;


public class BackgroundScanModule extends ReactContextBaseJavaModule  {

    public BackgroundScanModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        BroadcastReceiver geoLocationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               // BeaconDataModal message = intent.getParcelableExtra("message");
                String bStr=intent.getStringExtra("message");
                BeaconDataModal message=(BeaconDataModal)new Gson().fromJson(bStr,BeaconDataModal.class);
                String id=intent.getStringExtra("id");
                Log.e("SAMPLE2>>",""+message.id);
                Log.e("SAMPLE3>>",""+id);
                BackgroundScanModule.this.sendEvent(message);
            }
        };
        LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(geoLocationReceiver, new IntentFilter("BeaconDetectionUpdate"));
    }

    @Override
    public String getName() {
        return "BackgroundScan";
    }

    @ReactMethod
    public void startService(Promise promise) {
        String result = "Success";
        try {
            Intent intent = new Intent(BackgroundScanService.FOREGROUND);
            intent.setClass(this.getReactApplicationContext(), BackgroundScanService.class);
            getReactApplicationContext().startService(intent);
        } catch (Exception e) {
            promise.reject(e);
            return;
        }
        promise.resolve(result);
    }

    @ReactMethod
    public void stopService(Promise promise) {
        String result = "Success";
        try {
            Intent intent = new Intent(BackgroundScanService.FOREGROUND);
            intent.setClass(this.getReactApplicationContext(), BackgroundScanService.class);
            this.getReactApplicationContext().stopService(intent);
        } catch (Exception e) {
            promise.reject(e);
            return;
        }
        promise.resolve(result);
    }

    private void sendEvent(BeaconDataModal beacon) {
        WritableMap map = Arguments.createMap();
        WritableMap beaconDataMap = Arguments.createMap();
        beaconDataMap.putString("id", beacon.id);
        beaconDataMap.putString("name", beacon.name);
        beaconDataMap.putString("proximityUuid", beacon.proximityUuid);
        beaconDataMap.putDouble("distance", beacon.distance);
        beaconDataMap.putInt("majorId", beacon.majorId);
        beaconDataMap.putInt("minorId",beacon.minorId);
        beaconDataMap.putInt("rssi",beacon.rssi);
        beaconDataMap.putInt("tXPower",beacon.tXPower);

        map.putMap("beaconData", beaconDataMap);
        map.putDouble("timestamp", new Date().getTime());

        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("updateBeacons", map);
    }
}
