package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;

import java.util.List;

public class ObjectService extends Service {

    private static final String UPDATE_STARTED = "Update started...";
    private static final String UPDATING_OBJECTS = "Updating objects...";
    private static final int MILLIS = 60000;
    private static final int UPDATE_INTERVAL = 30;
    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;


    private boolean objectLogThreadStatus;

    public ObjectService() {
    }

    public static ParseObject getUserDetailsObject() {
        return userDetailsObject;
    }

    public static void setUserDetailsObject(ParseObject userDetailsObject) {
        ObjectService.userDetailsObject = userDetailsObject;
    }

    public static ParseObject getUserPreferenceObject() {
        return userPreferenceObject;
    }

    public static void setUserPreferenceObject(ParseObject userPreferenceObject) {
        ObjectService.userPreferenceObject = userPreferenceObject;
    }

    public static List<ParseObject> getUserCircles() {
        return userCircles;
    }

    private static void fetchObjectsSequentially() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            try {
                userDetailsObject = parseUser.getParseObject(Constants.USER_DETAILS_ROW).fetch();
                fetchUserCircles();
                fetchUserPreferenceObject();
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    private static void fetchUserPreferenceObject() {
        if (userDetailsObject != null) {
            try {
                userPreferenceObject = userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW).fetch();
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        } else {
            fetchObjectsSequentially();
        }
    }

    private static void fetchUserCircles() {
        if (userDetailsObject != null) {
            ParseRelation<ParseObject> circleRelation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
            try {
                userCircles = circleRelation.getQuery().find();
                if (basicController != null) {
                    basicController.updateCircles(userCircles);
                }
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        } else {
            fetchObjectsSequentially();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        basicController = BasicController.getInstance(this);
        startThreadIfPossible();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startThreadIfPossible() {
        if (!objectLogThreadStatus) {
            objectLogThreadStatus = true;
            Thread objectThread = new Thread(new ObjectUpdateRunnable(UPDATE_INTERVAL));
            objectThread.start();
        }
    }

    private class ObjectUpdateRunnable implements Runnable {

        private long updateInterval;

        private ObjectUpdateRunnable(int updateInterval) {
            this.updateInterval = updateInterval * MILLIS;
        }

        @Override
        public void run() {
            //TODO need to create a prefs to control the update
            while (true) {
                synchronized (this) {
                    Utilities.writeToLog(UPDATE_STARTED);
                    fetchObjectsSequentially();
                    try {
                        Thread.sleep(updateInterval);
                        Utilities.writeToLog(UPDATING_OBJECTS);
                    } catch (InterruptedException e) {
                        ErrorHandler.handleError(null, e);
                        Utilities.writeToLog("Thread interrupted...");
                    }
                }
            }
        }
    }
}
