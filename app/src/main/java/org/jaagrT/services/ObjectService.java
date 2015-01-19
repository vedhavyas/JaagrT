package org.jaagrT.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jaagrT.broadcast.receivers.UpdateReceiver;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ObjectService extends Service {

    private static ParseObject userDetailsObject, userPreferenceObject;
    private static List<ParseObject> userCircles;
    private static BasicController basicController;
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

    private static void fetchAndUpdateUserDetailsObject() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            try {
                userDetailsObject = parseUser.getParseObject(Constants.USER_DETAILS_ROW).fetch();
                User localUser = basicController.getLocalUser();
                if (localUser.getFirstName() != null) {
                    userDetailsObject.put(Constants.USER_FIRST_NAME, localUser.getFirstName());
                }
                if (localUser.getLastName() != null) {
                    userDetailsObject.put(Constants.USER_LAST_NAME, localUser.getLastName());
                }
                if (localUser.getPhoneNumber() != null) {
                    userDetailsObject.put(Constants.USER_PRIMARY_PHONE, localUser.getPhoneNumber());
                }
                if (localUser.getPictureRaw() != null) {
                    ParseFile pictureFile = new ParseFile(Constants.USER_PICTURE_FILE_NAME, localUser.getPictureRaw());
                    pictureFile.save();
                    userDetailsObject.put(Constants.USER_PROFILE_PICTURE, pictureFile);
                }
                if (localUser.getThumbnailPictureRaw() != null) {
                    ParseFile thumbFile = new ParseFile(Constants.USER_THUMBNAIL_PICTURE_FILE_NAME, localUser.getThumbnailPictureRaw());
                    thumbFile.save();
                    userDetailsObject.put(Constants.USER_THUMBNAIL_PICTURE, thumbFile);
                }
                localUser.setMemberOfMasterCircle(userDetailsObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE_VERIFIED, localUser.isPhoneVerified());
                userDetailsObject.saveEventually();
                basicController.updateUser(localUser);
                Utilities.writeToFile("Updated user details...");
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    private static void fetchAndUpdateUserPreferenceObject() {
        if (userDetailsObject != null) {
            try {
                userPreferenceObject = userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW).fetch();
                SharedPreferences prefs = basicController.getPrefs();
                userPreferenceObject.put(Constants.SEND_SMS, prefs.getBoolean(Constants.SEND_SMS, true));
                userPreferenceObject.put(Constants.SEND_EMAIL, prefs.getBoolean(Constants.SEND_EMAIL, true));
                userPreferenceObject.put(Constants.SEND_PUSH, prefs.getBoolean(Constants.SEND_PUSH, true));
                userPreferenceObject.put(Constants.SHOW_POP_UPS, prefs.getBoolean(Constants.SHOW_POP_UPS, true));
                userPreferenceObject.put(Constants.RECEIVE_SMS, prefs.getBoolean(Constants.RECEIVE_SMS, true));
                userPreferenceObject.put(Constants.RECEIVE_PUSH, prefs.getBoolean(Constants.RECEIVE_PUSH, true));
                userPreferenceObject.put(Constants.RECEIVE_EMAIL, prefs.getBoolean(Constants.RECEIVE_EMAIL, true));
                userPreferenceObject.put(Constants.NOTIFY_WITH_IN, prefs.getInt(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE));
                userPreferenceObject.put(Constants.RESPOND_ALERT_WITH_IN, prefs.getInt(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE));
                userPreferenceObject.put(Constants.ALERT_MESSAGE, prefs.getString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE));
                userPreferenceObject.saveEventually();
                Utilities.writeToFile("Updated preferences...");
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    private static void fetchAndUpdateUserCircles() {
        if (userDetailsObject != null) {
            ParseRelation<ParseObject> circleRelation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
            try {
                userCircles = circleRelation.getQuery().find();
                List<String> objectIDs = basicController.getCircleObjectIDs();
                List<User> updatedCircles = new ArrayList<>();
                if (objectIDs != null) {
                    for (ParseObject parseObject : userCircles) {
                        if (objectIDs.contains(parseObject.getObjectId())) {
                            User circle = new User();
                            circle.setObjectID(parseObject.getObjectId());
                            if (parseObject.getString(Constants.USER_FIRST_NAME) == null) {
                                String[] emailSet = parseObject.getString(Constants.USER_PRIMARY_EMAIL).split("@");
                                circle.setFirstName(emailSet[0]);
                            } else {
                                circle.setFirstName(parseObject.getString(Constants.USER_FIRST_NAME));
                            }
                            circle.setLastName(parseObject.getString(Constants.USER_LAST_NAME));
                            circle.setPhoneNumber(parseObject.getString(Constants.USER_PRIMARY_PHONE));
                            circle.setPhoneVerified(parseObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                            circle.setMemberOfMasterCircle(parseObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                            circle.setEmail(parseObject.getString(Constants.USER_PRIMARY_EMAIL));
                            if (parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                                try {
                                    circle.setThumbnailPicture(Utilities.getBitmapFromBlob(parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE).getData()));
                                } catch (ParseException e) {
                                    ErrorHandler.handleError(null, e);
                                }
                            }
                            if (parseObject.getParseFile(Constants.USER_PROFILE_PICTURE) != null) {
                                try {
                                    circle.setPicture(Utilities.getBitmapFromBlob(parseObject.getParseFile(Constants.USER_PROFILE_PICTURE).getData()));
                                } catch (ParseException e) {
                                    ErrorHandler.handleError(null, e);
                                }
                            }
                            updatedCircles.add(circle);
                        } else {
                            circleRelation.remove(parseObject);
                        }
                    }

                    basicController.updateCircles(updatedCircles);
                } else {
                    for (ParseObject object : userCircles) {
                        circleRelation.remove(object);
                    }
                }
                userDetailsObject.saveEventually();
                Utilities.writeToFile("Updated circles...");

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
                        basicController.updateCirclesThroughObjects(userCircles);
                    } catch (ParseException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
            }
        }).start();

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
        objectAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent receiverIntent = new Intent(this, UpdateReceiver.class);

        receiverIntent.setAction(Constants.ACTION_UPDATE_OBJECTS);
        objectPendingIntent = PendingIntent.getBroadcast(this, Constants.ACTION_UPDATE_OBJECTS_CODE, receiverIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        objectAlarm.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR * 3, objectPendingIntent);
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
            Utilities.writeToFile("Updating Objects...");
            fetchAndUpdateUserDetailsObject();
            fetchAndUpdateUserPreferenceObject();
        }
    }

    private static class UpdateCircles implements Runnable {

        @Override
        public void run() {
            Utilities.writeToFile("Updating circles...");
            if (userDetailsObject != null) {
                fetchAndUpdateUserCircles();
            } else {
                fetchAndUpdateUserDetailsObject();
                fetchAndUpdateUserCircles();
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
