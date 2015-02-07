package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.parse.ParseUser;

import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.services.LocationService;
import org.jaagrT.services.ObjectService;

public class UpdateReceiver extends BroadcastReceiver {

    private static final long UPDATE_INTERVAL = 10200000;

    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                Intent objectService = new Intent(context, ObjectService.class);
                context.startService(objectService);

                Intent locationService = new Intent(context, LocationService.class);
                context.startService(locationService);
            }
        } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_OBJECTS)) {
            BasicController basicController = BasicController.getInstance(context);
            SharedPreferences prefs = basicController.getPrefs();
            long lastDataUpdate = prefs.getLong(Constants.LAST_DATA_UPDATED, 0);
            if (System.currentTimeMillis() - lastDataUpdate > UPDATE_INTERVAL) {
                Utilities.writeToFile("condition met to update Data..");
                prefs.edit().putLong(Constants.LAST_DATA_UPDATED, System.currentTimeMillis()).apply();
                ObjectService.updateObjects();
                ObjectService.updateCircles();
            } else if (isObjectsNull()) {
                prefs.edit().putLong(Constants.LAST_DATA_UPDATED, System.currentTimeMillis()).apply();
                Utilities.writeToFile("Objects are Null...");
                ObjectService.updateObjects();
                ObjectService.updateCircles();
            }

            long lastImageUpdate = prefs.getLong(Constants.LAST_IMAGE_UPDATED, 0);
            if (System.currentTimeMillis() - lastImageUpdate > UPDATE_INTERVAL * 2) {
                Utilities.writeToFile("Condition met to update images...");
                prefs.edit().putLong(Constants.LAST_IMAGE_UPDATED, System.currentTimeMillis()).apply();
                ObjectService.updateUserImages();
                ObjectService.updateAllCirclesImages();
            }

            ObjectService.updateInvitations();
        }
    }

    private boolean isObjectsNull() {
        return ObjectService.getUserDetailsObject() == null || ObjectService.getUserPreferenceObject() == null;
    }
}
