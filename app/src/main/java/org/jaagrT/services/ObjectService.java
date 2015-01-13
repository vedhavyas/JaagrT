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

    private static final int MIN_IN_MILLIS = 60000;
    private static final int INTERVAL_IN_MINS = 60;
    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;
    private static Thread objectThread;
    private static UpdateRunnable updateRunnable;
    private static boolean shouldLoop;

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

    public static boolean getShouldLoop() {
        return shouldLoop;
    }

    public static void setShouldLoop(boolean decision) {
        shouldLoop = decision;
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
        }
    }

    public static void startObjectUpdateThread() {
        if (updateRunnable == null) {
            updateRunnable = new UpdateRunnable();
        }

        //TODO check this later
        if (objectThread != null) {
            objectThread.interrupt();
        }

        setShouldLoop(true);
        objectThread = new Thread(updateRunnable);
        objectThread.start();
    }

    private static void clearAllObjects() {
        userCircles = null;
        userDetailsObject = null;
        userPreferenceObject = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        basicController = BasicController.getInstance(this);
        startObjectUpdateThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setShouldLoop(false);
        clearAllObjects();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            Utilities.writeToLog("Started Thread...");
            while (getShouldLoop()) {
                Utilities.writeToLog("Updating objects...");
                fetchObjectsSequentially();
                try {
                    Utilities.writeToLog("trying to sleep...");
                    Thread.sleep(MIN_IN_MILLIS * INTERVAL_IN_MINS);
                } catch (InterruptedException e) {
                    setShouldLoop(false);
                    ErrorHandler.handleError(null, e);
                    Utilities.writeToLog("thread interrupted...");
                    startObjectUpdateThread();
                }
            }
        }
    }
}
