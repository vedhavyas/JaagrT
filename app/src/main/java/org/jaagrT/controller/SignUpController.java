package org.jaagrT.controller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.jaagrT.listeners.BasicListener;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 12/12/14.
 * Project JaagrT
 */
public class SignUpController {

    private Activity activity;
    private SweetAlertDialog pDialog;
    private User localUser;
    private BasicListener listener;
    private ObjectRetriever objectRetriever;


    public SignUpController(Activity activity, BasicListener listener, SweetAlertDialog pDialog) {
        this.activity = activity;
        this.listener = listener;
        this.pDialog = pDialog;
        this.objectRetriever = ObjectRetriever.getInstance(activity);
    }

    public void registerUser(String email, String password) {
        final ParseUser newUser = new ParseUser();
        newUser.setUsername(email);
        newUser.setPassword(password);
        newUser.setEmail(email);

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    pDialog.setTitleText("Registration Successful..");
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    if (parseUser != null) {
                        localUser = new User();
                        localUser.setEmail(parseUser.getEmail());
                        saveUserDetails(parseUser);
                    }
                } else {
                    pDialog.cancel();
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
    }

    private void saveUserDetails(final ParseUser parseUser) {
        pDialog.setTitleText("Finalising Registration...");
        final ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        objectRetriever.setUserDetailsObject(userDetailsObject);
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveDefaultPreferences(parseUser);
                } else {
                    new SaveUserLocally().execute();
                }
            }
        });

        parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
        parseUser.saveInBackground();
    }

    public void facebookRegistration() {
        localUser = new User();
        Session session = ParseFacebookUtils.getSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser fbUser, Response response) {
                if (response != null) {
                    try {
                        String email = (String) fbUser.getProperty("email");
                        ParseUser parseUser = ParseUser.getCurrentUser();
                        parseUser.setEmail(email);
                        parseUser.saveInBackground();
                        localUser.setEmail(email);
                        localUser.setFirstName(fbUser.getFirstName());
                        localUser.setLastName(fbUser.getLastName());
                        saveFbUserDetails(parseUser, fbUser);
                    } catch (Exception e) {
                        pDialog.cancel();
                        AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                    }

                }
            }
        }).executeAsync();
    }

    private void saveFbUserDetails(final ParseUser parseUser, GraphUser fbUser) {
        pDialog.setTitleText("Completing Registration...");
        final ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        objectRetriever.setUserDetailsObject(userDetailsObject);
        userDetailsObject.put(Constants.USER_FIRST_NAME, fbUser.getFirstName());
        userDetailsObject.put(Constants.USER_LAST_NAME, fbUser.getLastName());
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveDefaultPreferences(parseUser);
                } else {
                    new SaveUserLocally().execute();
                }
            }
        });
        parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
        parseUser.saveInBackground();

    }

    private void saveDefaultPreferences(final ParseUser parseUser) {
        final ParseObject userPreferenceObject = new ParseObject(Constants.USER_COMMUNICATION_PREFERENCE_CLASS);
        userPreferenceObject.put(Constants.SEND_SMS, true);
        userPreferenceObject.put(Constants.SEND_EMAIL, true);
        userPreferenceObject.put(Constants.SEND_PUSH, true);
        userPreferenceObject.put(Constants.SHOW_POP_UPS, true);
        userPreferenceObject.put(Constants.RECEIVE_SMS, true);
        userPreferenceObject.put(Constants.RECEIVE_PUSH, true);
        userPreferenceObject.put(Constants.RECEIVE_EMAIL, true);
        userPreferenceObject.put(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE);
        userPreferenceObject.put(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE);
        userPreferenceObject.put(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE);
        ParseACL preferenceAcl = new ParseACL(parseUser);
        preferenceAcl.setPublicWriteAccess(false);
        preferenceAcl.setPublicReadAccess(false);
        userPreferenceObject.setACL(preferenceAcl);
        userPreferenceObject.saveInBackground();
        objectRetriever.setUserPreferenceObject(userPreferenceObject);

        parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                .fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject userDetailsObject, ParseException e) {
                        if (e == null) {
                            userDetailsObject.put(Constants.USER_COMMUNICATION_PREFERENCE_ROW, userPreferenceObject);
                            userDetailsObject.saveInBackground();
                        }
                    }
                });

        new SaveUserLocally().execute();
    }


    private class SaveUserLocally extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText("Saving Data...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences prefs = objectRetriever.getPrefs();
            prefs.edit().putBoolean(Constants.SEND_SMS, true).apply();
            prefs.edit().putBoolean(Constants.SEND_EMAIL, true).apply();
            prefs.edit().putBoolean(Constants.SEND_PUSH, true).apply();
            prefs.edit().putBoolean(Constants.SHOW_POP_UPS, true).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_SMS, true).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_PUSH, true).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_EMAIL, true).apply();
            prefs.edit().putInt(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE).apply();
            prefs.edit().putInt(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE).apply();
            prefs.edit().putString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE).apply();

            Database db = Database.getInstance(activity, Database.USER_TABLE);
            localUser.setMemberOfMasterCircle(false);
            int result = (int) db.saveUser(localUser);
            if (result > 0) {
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            listener.onComplete();
        }
    }
}