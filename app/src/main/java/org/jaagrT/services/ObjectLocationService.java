package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.jaagrT.controller.ObjectRetriever;

public class ObjectLocationService extends Service {

    private ObjectRetriever objectRetriever;

    public ObjectLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        objectRetriever = ObjectRetriever.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        objectRetriever.fetchObjectsFromCloud();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
