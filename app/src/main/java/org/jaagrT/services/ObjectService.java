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
    private AlarmManager objectAlarm, circleAlarm;
    private PendingIntent objectPendingIntent, circlePendingIntent;

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

    public static void updateObjects() {
        new Thread(new UpdateObjects()).start();
    }

    public static void updateCircles() {
        new Thread(new UpdateCircles()).start();
    }

    private static void fetchUserDetailsObject() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            try {
                userDetailsObject = parseUser.getParseObject(Constants.USER_DETAILS_ROW).fetch();
                //TODO check and update locally
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
                //TODO check and update locally
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
                    //TODO check if the user removed circles during offline
                    basicController.updateCircles(userCircles);
                }
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    public static void removeCircles(List<String> objectIDs) {
        new Thread(new RemoveUserCircles(objectIDs)).start();
    }

    public static void getCirclesFirstTime() {

        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();

    }

    private void cleanUp() {
        objectAlarm.cancel(objectPendingIntent);
        circleAlarm.cancel(circlePendingIntent);
        circleAlarm = null;
        circlePendingIntent = null;
        objectAlarm = null;
        objectPendingIntent = null;
        userCircles = null;
        userDetailsObject = null;
        userPreferenceObject = null;
        basicController = null;
    }

    private void initiateTheServiceObjects() {

        basicController = BasicController.getInstance(this);
        objectAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        circleAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, UpdateReceiver.class);

        receiverIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
        objectPendingIntent = PendingIntent.getBroadcast(this, Constants.ACTION_UPDATE_OBJECTS_CODE, receiverIntent, 0);

        receiverIntent.setAction(Constants.ACTION_UPDATE_CIRCLES);
        circlePendingIntent = PendingIntent.getBroadcast(this, Constants.ACTION_UPDATE_CIRCLES_CODE, receiverIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        objectAlarm.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, objectPendingIntent);

        int hour = calendar.get(Calendar.HOUR_OF_DAY) + 4;
        if (hour > 24) {
            hour -= 24;
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);

        circleAlarm.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, circlePendingIntent);
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
            Utilities.writeToLog("Updating Objects...");
            fetchUserDetailsObject();
            fetchUserPreferenceObject();
        }
    }

    private static class UpdateCircles implements Runnable {

        @Override
        public void run() {
            Utilities.writeToLog("Updating circles...");
            if (userDetailsObject != null) {
                fetchUserCircles();
            } else {
                fetchUserDetailsObject();
                fetchUserCircles();
            }
        }
    }

    private static class RemoveUserCircles implements Runnable {

        List<String> objectsIDs;

        private RemoveUserCircles(List<String> objectsIDs) {
            this.objectsIDs = objectsIDs;
        }

        @Override
        public void run() {
            if (objectsIDs.size() > 0 && userCircles != null && userDetailsObject != null) {
                ParseRelation<ParseObject> relation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                for (ParseObject circle : userCircles) {
                    if (objectsIDs.contains(circle.getObjectId())) {
                        relation.remove(circle);
                    }
                }
                userDetailsObject.saveEventually();
            }
        }
    }
}
