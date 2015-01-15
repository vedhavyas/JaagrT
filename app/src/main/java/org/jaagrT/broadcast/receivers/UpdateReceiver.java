package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.parse.ParseUser;

import org.jaagrT.helpers.Constants;
import org.jaagrT.services.ObjectService;

public class UpdateReceiver extends BroadcastReceiver {
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
            ObjectService.updateObjects();
        } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_CIRCLES)) {
            ObjectService.updateCircles();
        }
    }
}
