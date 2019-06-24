package com.wifiattendance;

import com.kontakt.sdk.android.common.profile.IBeaconDevice;

import java.io.Serializable;


public class BeaconDataModal implements Serializable {

    public String id;
    public String name;
    public int minorId;
    public int majorId;
    public String proximityUuid;
    public double distance;
    public int tXPower;
    public int rssi;

    public BeaconDataModal(IBeaconDevice device){
        this.id = device.getUniqueId();
        this.name = device.getName();
        this.minorId = device.getMinor();
        this.majorId = device.getMajor();
        this.proximityUuid = device.getProximityUUID()+"";
        this.distance = device.getDistance();
        this.tXPower = device.getTxPower();
        this.rssi = device.getRssi();
    }

    public BeaconDataModal(String beaconId, String beaconName, int beaconMinorId, int beaconMajorId, String beaconProximityUUUD, double beaconDistance, int txPower, int rssi) {
        this.id = beaconId;
        this.name = beaconName;
        this.minorId = beaconMinorId;
        this.majorId = beaconMajorId;
        this.proximityUuid = beaconProximityUUUD;
        this.distance = beaconDistance;
        this.tXPower = txPower;
        this.rssi = rssi;
    }
}

