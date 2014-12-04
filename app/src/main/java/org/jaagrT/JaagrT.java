package org.jaagrT;

import android.app.Application;

import com.facebook.SessionDefaultAudience;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

import org.jaagrT.utils.Utilities;

/**
 * Authored by vedhavyas on 1/12/14.
 * Project JaagrT
 */


public class JaagrT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        {
            Logger.DEBUG_WITH_STACKTRACE = true;

            // initialize facebook configuration
            Permission[] permissions = new Permission[]{
                    Permission.PUBLIC_PROFILE,
                    Permission.USER_PHOTOS,
                    Permission.EMAIL
            };

            SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                    .setAppId("630451703747709")
                    .setNamespace("jaagrt_name_space")
                    .setPermissions(permissions)
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                    .setAskForAllPermissionsAtOnce(false)
                    .build();

            SimpleFacebook.setConfiguration(configuration);
        }

        {
            Utilities.logIt("Initiating parse");
            Parse.initialize(this, "ONhYLeLqasDx84ABEAB6utZqB1LtSsVnbPq0nyOO", "mLQqVwHEuxJFaJnhc7qbLvbX7UwTNo2KPp5RS2ww");
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

    }
}
