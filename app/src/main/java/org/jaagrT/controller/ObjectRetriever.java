package org.jaagrT.controller;

import android.app.Activity;
import android.content.SharedPreferences;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.listeners.ParseListener;
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
    private Activity activity;
    private ParseObject userDetailsObject, userPreferenceObject;
    private SharedPreferences prefs;

    private ObjectRetriever(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    public static ObjectRetriever getInstance(Activity activity) {
        if (objectRetriever == null) {
            objectRetriever = new ObjectRetriever(activity);
        }

        return objectRetriever;
    }

    public ParseObject getUserDetailsObject(final ParseListener listener) {
        if (userDetailsObject == null) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    userDetailsObject = parseObject;
                                    listener.onComplete(userDetailsObject);
                                } else {
                                    Utilities.logIt(e.getMessage());
                                }
                            }
                        });
            }
            return null;
        } else {
            return userDetailsObject;
        }
    }

    public ParseObject getUserPreferenceObject(final ParseListener listener) {
        if (userPreferenceObject == null) {
            if (userDetailsObject != null) {
                userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    userPreferenceObject = parseObject;
                                    listener.onComplete(userDetailsObject);
                                } else {
                                    Utilities.logIt(e.getMessage());
                                }
                            }
                        });
            }
            return null;
        } else {
            return userPreferenceObject;
        }
    }

    public User getLocalUser() {
        Database db = Database.getInstance(activity, Database.USER_TABLE);
        return db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void setUserDetailsObject(ParseObject userDetailsObject) {
        this.userDetailsObject = userDetailsObject;
    }

    public void setUserPreferenceObject(ParseObject userPreferenceObject) {
        this.userPreferenceObject = userPreferenceObject;
    }

}
