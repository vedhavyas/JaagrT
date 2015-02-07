package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import org.jaagrT.helpers.Utilities;

public class LocationService extends Service implements LocationListener {

    private static final long MIN_TIME_TO_UPDATE = 1800000;
    private static final float MIN_DISTANCE_TO_UPDATE = 250;
    protected LocationManager locationManager;
    private Location lastKnownLocation;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initiateServiceObjects();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            lastKnownLocation = location;
            Utilities.writeToFile("Location changed...");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void initiateServiceObjects() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_TO_UPDATE, MIN_DISTANCE_TO_UPDATE, this);
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}
