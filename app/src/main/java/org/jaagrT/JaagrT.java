package org.jaagrT;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;

import org.jaagrT.utilities.Constants;

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

    }
}
