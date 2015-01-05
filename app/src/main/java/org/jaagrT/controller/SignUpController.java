package org.jaagrT.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.jaagrT.listeners.BasicListener;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 12/12/14.
 * Project JaagrT
 */
public class SignUpController {

    private static final String SIGN_UP_SUCCESS = "Signup Successful!!";
    private static final String ERROR = "Error";
    private static final String OKAY = "Okay";
    private static final String ERROR_UNKNOWN = "Unknown Error!!";
    private static final String FINALIZING_SIGNUP = "Finalizing Signup...";
    private static final String EMAIL = "email";
    private static final String SAVING_DATA = "Saving  Data...";
    private Activity activity;
    private SweetAlertDialog pDialog;
    private User localUser;
    private BasicListener listener;
    private ParseObject userDetailsObject, userPreferenceObject;
    private BasicController basicController;


    public SignUpController(Activity activity, BasicListener listener, SweetAlertDialog pDialog) {
        this.activity = activity;
        this.listener = listener;
        this.pDialog = pDialog;
        this.basicController = BasicController.getInstance(activity);
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
                    pDialog.setTitleText(SIGN_UP_SUCCESS);
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    if (parseUser != null) {
                        localUser = new User();
                        localUser.setEmail(parseUser.getEmail());
                        saveUserDetails(parseUser);
                    }
                } else {
                    pDialog.cancel();
                    AlertDialogs.showErrorDialog(activity, ERROR, e.getMessage(), OKAY);
                }
            }
        });
    }

    private void saveUserDetails(final ParseUser parseUser) {
        pDialog.setTitleText(FINALIZING_SIGNUP);
        userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
                    parseUser.saveInBackground();
                    saveDefaultPreferences(parseUser);
                } else {
                    clearUser();
                }
            }
        });
    }

    public void facebookRegistration() {
        localUser = new User();
        Session session = ParseFacebookUtils.getSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser fbUser, Response response) {
                if (response != null) {
                    try {
                        String email = (String) fbUser.getProperty(EMAIL);
                        ParseUser parseUser = ParseUser.getCurrentUser();
                        parseUser.setEmail(email);
                        parseUser.saveInBackground();
                        localUser.setEmail(email);
                        localUser.setFirstName(fbUser.getFirstName());
                        localUser.setLastName(fbUser.getLastName());
                        saveFbUserDetails(parseUser, fbUser);
                    } catch (Exception e) {
                        clearUser();
                    }

                } else {
                    clearUser();
                }
            }
        }).executeAsync();
    }

    private void saveFbUserDetails(final ParseUser parseUser, GraphUser fbUser) {
        pDialog.setTitleText(FINALIZING_SIGNUP);
        userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_FIRST_NAME, fbUser.getFirstName());
        userDetailsObject.put(Constants.USER_LAST_NAME, fbUser.getLastName());
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
                    parseUser.saveInBackground();
                    saveDefaultPreferences(parseUser);
                } else {
                    clearUser();
                }
            }
        });

    }

    private void saveDefaultPreferences(final ParseUser parseUser) {
        userPreferenceObject = new ParseObject(Constants.USER_COMMUNICATION_PREFERENCE_CLASS);
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
        userPreferenceObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    userDetailsObject.put(Constants.USER_COMMUNICATION_PREFERENCE_ROW, userPreferenceObject);
                    userDetailsObject.saveInBackground();
                    new SaveUserLocally().execute();
                } else {
                    clearUser();
                }
            }
        });
    }

    private void clearUser() {
        ParseUser.logOut();
        userDetailsObject = null;
        userPreferenceObject = null;
        pDialog.cancel();
        AlertDialogs.showErrorDialog(activity, ERROR, ERROR_UNKNOWN, OKAY);
    }

    private void startObjectService() {
        Intent serviceIntent = new Intent(activity, ObjectService.class);
        activity.startService(serviceIntent);
        ObjectService.setUserDetailsObject(userDetailsObject);
        ObjectService.setUserPreferenceObject(userPreferenceObject);
    }


    private class SaveUserLocally extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText(SAVING_DATA);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            SharedPreferences prefs = basicController.getPrefs();
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

            localUser.setMemberOfMasterCircle(false);
            int result = basicController.saveUser(localUser);
            if (result > 0) {
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result > 0) {
                pDialog.cancel();
                startObjectService();
                listener.onComplete();
            } else {
                clearUser();
            }
        }
    }
}
