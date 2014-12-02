package org.jaagrT;

import android.app.Application;
import android.os.AsyncTask;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

/**
 * Authored by vedhavyas on 1/12/14.
 */


public class JaagrT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new SetupInstances().execute();
    }


    private class SetupInstances extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }
    }
}
