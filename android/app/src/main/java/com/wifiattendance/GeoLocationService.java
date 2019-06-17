package com.wifiattendance;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.wifiattendance.MainActivity;
import com.wifiattendance.R;

public class GeoLocationService extends Service {
    public static final String FOREGROUND = "com.app_name.location.FOREGROUND";
    private static int GEOLOCATION_NOTIFICATION_ID = 12345689;
    LocationManager locationManager = null;
    LocationListener locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        GeoLocationService.this.sendMessage(location);
      }
  
      @Override
      public void onStatusChanged(String s, int i, Bundle bundle) {}
  
      @Override
      public void onProviderEnabled(String s) {}
  
      @Override
      public void onProviderDisabled(String s) {}
    };
  
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onCreate() {
      locationManager = getSystemService(LocationManager.class);
  
      int permissionCheck = ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION);
      if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, locationListener);
      }
    }
  
    private void sendMessage(Location location) {
      try {
        Intent intent = new Intent("GeoLocationUpdate");
        intent.putExtra("message", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  
    @Override
    public void onDestroy() {
      locationManager.removeUpdates(locationListener);
      super.onDestroy();
    }
  
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      startForeground(GEOLOCATION_NOTIFICATION_ID, getCompatNotification());
      return START_STICKY;
    }
  
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
  
    private Notification getCompatNotification() {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      String str = "Is using your location in the background";
      builder
        .setSmallIcon(R.drawable.redbox_top_border_background)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
        .setContentTitle("App Name")
        .setContentText(str)
        .setTicker(str)
        .setWhen(System.currentTimeMillis());
      Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
      PendingIntent contentIntent = PendingIntent.getActivity(this, 1000, startIntent, 0);
      builder.setContentIntent(contentIntent);
      return builder.build();
    }
  }
  