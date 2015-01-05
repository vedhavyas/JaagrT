package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.utilities.Constants;

public class ObjectService extends Service {

    private static ParseObject userDetailsObject, userPreferenceObject;

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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userDetailsObject = null;
        userPreferenceObject = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fetchUserDetailsObject();
        return START_STICKY;
    }

    private void fetchUserDetailsObject() {
        if (userDetailsObject == null) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject userDetailsObject, ParseException e) {
                                if (e == null) {
                                    setUserDetailsObject(userDetailsObject);
                                    fetchUserPreferenceObject();
                                }
                            }
                        });
            }
        } else {
            userDetailsObject.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject userDetailsObject, ParseException e) {
                    if (e == null) {
                        setUserDetailsObject(userDetailsObject);
                        fetchUserPreferenceObject();
                    }
                }
            });
        }
    }

    private void fetchUserPreferenceObject() {
        if (userPreferenceObject == null) {
            if (userDetailsObject != null) {
                userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject userPreferenceObject, ParseException e) {
                                if (e == null) {
                                    setUserPreferenceObject(userPreferenceObject);
                                }
                            }
                        });
            } else {
                fetchUserDetailsObject();
            }
        } else {
            userPreferenceObject.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject userPreferenceObject, ParseException e) {
                    if (e == null) {
                        setUserPreferenceObject(userPreferenceObject);
                    }
                }
            });
        }
    }
}
