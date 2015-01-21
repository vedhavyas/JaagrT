package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.Utilities;

public class ConnectivityChangeReceiver extends BroadcastReceiver {


    private static long UPDATE_INTERVAL = 10200000;

    public ConnectivityChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo.isConnected() && Utilities.haveNetworkAccess() && canUpdateObjects(context)) {
                Intent updateIntent = new Intent(context, UpdateReceiver.class);
                updateIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
                context.sendBroadcast(updateIntent);
            }
        }
    }

    private boolean canUpdateObjects(Context context) {
        BasicController basicController = BasicController.getInstance(context);
        SharedPreferences prefs = basicController.getPrefs();
        long lastUpdate = prefs.getLong(Constants.LAST_UPDATE, 0);
        if (System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL) {
            Utilities.writeToFile("condition met..");
            prefs.edit().putLong(Constants.LAST_UPDATE, System.currentTimeMillis()).apply();
            return true;
        }
        return false;
    }
}
