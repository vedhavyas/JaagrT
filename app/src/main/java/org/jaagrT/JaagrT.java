package org.jaagrT;

import android.app.Application;
import android.content.Intent;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.jaagrT.helpers.Constants;
import org.jaagrT.services.LocationService;
import org.jaagrT.services.ObjectService;

/**
 * Authored by vedhavyas on 1/12/14.
 * Project JaagrT
 */


public class JaagrT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        {
            Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_ID);
            ParseInstallation.getCurrentInstallation().saveInBackground();
            ParseACL defaultACL = new ParseACL();
            defaultACL.setPublicReadAccess(true);
            defaultACL.setPublicWriteAccess(false);
            ParseACL.setDefaultACL(defaultACL, true);
        }


        {
            if (ParseUser.getCurrentUser() != null) {
                Intent objectService = new Intent(getBaseContext(), ObjectService.class);
                startService(objectService);

                Intent locationService = new Intent(getBaseContext(), LocationService.class);
                startService(locationService);
            }
        }

    }
}
