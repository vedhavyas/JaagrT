package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;

import java.io.IOException;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static boolean receivedFirst = true;

    public ConnectivityChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (receivedFirst) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo.isConnected() && isConnected()) {
                    Utilities.writeToFile("Got Network access...");
                    Intent updateIntent = new Intent(context, UpdateReceiver.class);
                    updateIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
                    context.sendBroadcast(updateIntent);
                }
                receivedFirst = false;
            } else {
                receivedFirst = true;
            }
        } else {
            receivedFirst = true;
        }
    }

    private boolean isConnected() {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException | InterruptedException e) {
            ErrorHandler.handleError(null, e);
        }

        return false;
    }


}
