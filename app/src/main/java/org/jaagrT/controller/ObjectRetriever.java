package org.jaagrT.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

/**
 * Authored by vedhavyas on 14/12/14.
 * Project JaagrT
 */
public class ObjectRetriever {
    private static ObjectRetriever objectRetriever;
    private Context context;
    private ParseObject userDetailsObject, userPreferenceObject;
    private SharedPreferences prefs;

    private ObjectRetriever(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    public static ObjectRetriever getInstance(Context context) {
        if (objectRetriever == null) {
            objectRetriever = new ObjectRetriever(context);
        }

        return objectRetriever;
    }

    public ParseObject getUserDetailsObject() {
        if (userDetailsObject == null) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    userDetailsObject = parseObject;
                                    getUserPreferenceObject();
                                } else {
                                    Utilities.logIt(String.valueOf(e.getCode()));
                                }
                            }
                        });
            }
            return null;
        } else {
            return userDetailsObject;
        }
    }

    public void setUserDetailsObject(ParseObject userDetailsObject) {
        this.userDetailsObject = userDetailsObject;
    }

    public ParseObject getUserPreferenceObject() {
        if (userPreferenceObject == null) {
            if (userDetailsObject != null) {
                userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    userPreferenceObject = parseObject;
                                } else {
                                    Utilities.logIt(e.getMessage());
                                }
                            }
                        });
            } else {
                getUserDetailsObject();
            }
            return null;
        } else {
            return userPreferenceObject;
        }
    }

    public void setUserPreferenceObject(ParseObject userPreferenceObject) {
        this.userPreferenceObject = userPreferenceObject;
    }

    public User getLocalUser() {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public Bitmap getUserPicture() {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return db.getUserPicture(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public void clearAllObjects() {
        if (objectRetriever != null) {
            objectRetriever = null;
        }
    }

    public void fetchObjectsFromCloud() {
        if (userDetailsObject != null) {
            userDetailsObject.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        userDetailsObject = parseObject;
                        userPreferenceObject.fetchInBackground();
                    }
                }
            });
        } else {
            getUserDetailsObject();
        }
    }

}
