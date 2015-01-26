package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.parse.ParseUser;

import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.services.ObjectService;

public class UpdateReceiver extends BroadcastReceiver {

    private static long UPDATE_INTERVAL = 10200000;

    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                Intent serviceIntent = new Intent(context, ObjectService.class);
                context.startService(serviceIntent);
            }
        } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_OBJECTS)) {
            BasicController basicController = BasicController.getInstance(context);
            SharedPreferences prefs = basicController.getPrefs();
            long lastUpdate = prefs.getLong(Constants.LAST_UPDATE, 0);
            if (System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL) {
                Utilities.writeToFile("condition met..");
                prefs.edit().putLong(Constants.LAST_UPDATE, System.currentTimeMillis()).apply();
                ObjectService.updateObjects();
                ObjectService.updateCircles();
            } else if(isObjectsNull()){
                prefs.edit().putLong(Constants.LAST_UPDATE, System.currentTimeMillis()).apply();
                Utilities.writeToFile("Objects are Null...");
                ObjectService.updateObjects();
                ObjectService.updateCircles();
            }
        }
    }

    private boolean isObjectsNull() {
        return ObjectService.getUserDetailsObject() == null || ObjectService.getUserPreferenceObject() == null;
    }
}
