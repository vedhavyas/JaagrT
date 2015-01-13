package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
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

    private static final int MILLIS = 60000;
    private static final int UPDATE_INTERVAL = 60;
    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;
    private static Handler handler;
    private static AsyncTaskRunnable asyncTaskRunnable;

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

    public static void startHandlerJob() {
        if (handler != null) {
            handler.removeCallbacks(asyncTaskRunnable);
        } else {
            handler = new Handler();
        }
        asyncTaskRunnable = new AsyncTaskRunnable();
        asyncTaskRunnable.run();
    }

    private static void stopHandlerJob() {
        if (handler != null) {
            handler.removeCallbacks(asyncTaskRunnable);
        }
    }

    private static void clearAllObjects() {
        userCircles = null;
        userDetailsObject = null;
        userPreferenceObject = null;
        asyncTaskRunnable = null;
        handler = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        basicController = BasicController.getInstance(this);
        startHandlerJob();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHandlerJob();
        clearAllObjects();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static class AsyncTaskRunnable implements Runnable {
        @Override
        public void run() {
            new UpdateObjects().execute();
            handler.postDelayed(asyncTaskRunnable, UPDATE_INTERVAL * MILLIS);
        }
    }

    private static class UpdateObjects extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Utilities.writeToLog("Updating objects...");
            fetchObjectsSequentially();
            return null;
        }
    }

}
