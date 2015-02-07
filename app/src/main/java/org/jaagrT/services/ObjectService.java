package org.jaagrT.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.ParseObject;
import com.parse.ParseRelation;

import org.jaagrT.broadcast.receivers.UpdateReceiver;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ObjectFetcher;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.Database;

import java.util.Calendar;
import java.util.List;

public class ObjectService extends Service {

    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;
    private static boolean updatingObjects, updatingCircles, updatingInvitations, updatingUserImages, updatingCircleImages;
    private static BitmapHolder bitmapHolder;
    private AlarmManager objectAlarm;
    private PendingIntent objectPendingIntent;

    public ObjectService() {
    }

    public static ParseObject getUserDetailsObject() {
        return userDetailsObject;
    }

    public static void setUserDetailsObject(ParseObject userDetailsObject) {
        ObjectService.userDetailsObject = userDetailsObject;
    }

    public static BasicController getBasicController() {
        return basicController;
    }

    public static void setBasicController(BasicController basicController) {
        ObjectService.basicController = basicController;
    }

    public static BitmapHolder getBitmapHolder() {
        return bitmapHolder;
    }

    public static void setBitmapHolder(BitmapHolder bitmapHolder) {
        ObjectService.bitmapHolder = bitmapHolder;
    }

    public static ParseObject getUserPreferenceObject() {
        return userPreferenceObject;
    }

    public static void setUserPreferenceObject(ParseObject userPreferenceObject) {
        ObjectService.userPreferenceObject = userPreferenceObject;
    }

    public static void setUserCircles(List<ParseObject> circles) {
        ObjectService.userCircles = circles;
    }

    public static void updateObjects() {
        if (!updatingObjects) {
            updatingObjects = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utilities.writeToFile("Updating Objects...");
                    ObjectFetcher.fetchAndUpdateUserDetailsObject();
                    ObjectFetcher.fetchAndUpdateUserPreferenceObject();
                    updatingObjects = false;
                }
            }).start();
        }
    }

    public static void updateCircles() {
        if (!updatingCircles) {
            updatingCircles = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utilities.writeToFile("Updating circles...");
                    if (userDetailsObject != null) {
                        ObjectFetcher.fetchAndUpdateUserCircles();
                    } else {
                        ObjectFetcher.fetchAndUpdateUserDetailsObject();
                        ObjectFetcher.fetchAndUpdateUserCircles();
                    }

                    updatingCircles = false;
                }
            }).start();
        }
    }

    public static void updateAllCirclesImages() {
        if (!updatingCircleImages) {
            updatingCircleImages = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ObjectFetcher.fetchAndUpdateAllCircleImages();
                    updatingCircleImages = false;
                }
            }).start();
        }
    }

    public static void updateCircleImages(final List<String> objectIds) {
        if (objectIds.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String objectId : objectIds) {
                        ObjectFetcher.fetchAndUpdateCircleImage(objectId);
                    }
                }
            }).start();
        }
    }

    public static void updateUserImages() {
        if (!updatingUserImages) {
            updatingUserImages = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ObjectFetcher.fetchAndUpdateUserImages();
                    updatingUserImages = false;
                }
            }).start();
        }
    }

    public static void removeCircles(final List<String> objectIDs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (objectIDs.size() > 0 && userCircles != null && userDetailsObject != null) {
                    ParseRelation<ParseObject> relation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                    for (ParseObject circle : userCircles) {
                        if (objectIDs.contains(circle.getObjectId())) {
                            relation.remove(circle);
                        }
                    }
                    userDetailsObject.saveEventually();
                }
            }
        }).start();
    }

    public static void updateInvitations() {
        if (!updatingInvitations) {
            updatingInvitations = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] invitations = basicController.getInvitations();
                    if (invitations.length > 0) {
                        ObjectFetcher.updateInvitations(invitations);
                        basicController.dropTable(Database.INVITATION_TABLE);
                    }
                    updatingInvitations = false;
                }
            }).start();
        }
    }

    private void cleanUp() {
        objectAlarm.cancel(objectPendingIntent);
        objectAlarm = null;
        objectPendingIntent = null;
        userCircles = null;
        userDetailsObject = null;
        userPreferenceObject = null;
        basicController = null;
    }

    private void initiateTheServiceObjects() {

        basicController = BasicController.getInstance(this);
        bitmapHolder = BitmapHolder.getInstance(this);
        objectAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, UpdateReceiver.class);

        receiverIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
        objectPendingIntent = PendingIntent.getBroadcast(this, Constants.ACTION_UPDATE_OBJECTS_CODE, receiverIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour += 3;
        if (hour > 24) {
            hour -= 24;
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        Utilities.writeToFile("Setting alarm at - " + hour);
        objectAlarm.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR * 3, objectPendingIntent);
        updatingObjects = false;
        updatingCircles = false;
        if (userDetailsObject == null || userPreferenceObject == null) {
            updateObjects();
        }
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

}
