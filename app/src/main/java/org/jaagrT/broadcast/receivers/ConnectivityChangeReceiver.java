package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.Utilities;

public class ConnectivityChangeReceiver extends BroadcastReceiver {


    public ConnectivityChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo.isConnected() && Utilities.haveNetworkAccess()) {
                Intent updateIntent = new Intent(context, UpdateReceiver.class);
                updateIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
                context.sendBroadcast(updateIntent);
            }
        }
    }
}
