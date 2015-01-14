package org.jaagrT.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jaagrT.broadcast.receivers.UpdateReceiver;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;

import java.util.Calendar;
import java.util.List;

public class ObjectService extends Service {

    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;
    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;

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

    public static void updateObjects() {
        new Thread(new UpdateObjects()).start();
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

    private void cleanUp() {
        Utilities.writeToLog("Cleaning up...");
        alarmManager.cancel(alarmPendingIntent);
        alarmManager = null;
        alarmPendingIntent = null;
        userCircles = null;
        userDetailsObject = null;
        userPreferenceObject = null;
        basicController = null;
    }

    private void initiateTheServiceObjects() {
        //initiate objects
        basicController = BasicController.getInstance(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, UpdateReceiver.class);

        receiverIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
        alarmPendingIntent = PendingIntent.getBroadcast(this, Constants.ACTION_UPDATE_OBJECTS_CODE, receiverIntent, 0);

        //get current time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, alarmPendingIntent);
        Utilities.writeToLog("Started Alarm..");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initiateTheServiceObjects();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static class UpdateObjects implements Runnable {

        @Override
        public void run() {
            Utilities.writeToLog("Thread started...");
            fetchObjectsSequentially();
            Utilities.writeToLog("Objects updated...");
        }
    }
}
