package org.jaagrT.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.ObjectFetcher;
import org.jaagrT.listeners.OnCompleteListener;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */
public class LoginController {

    private static final String FETCHING_DATA = "Fetching data...";
    private static final String FETCHING_SETTINGS = "Fetching settings...";
    private Activity activity;
    private OnCompleteListener listener;
    private User localUser;
    private SweetAlertDialog pDialog;
    private BasicController basicController;
    private ParseObject userDetailsObject, userPreferenceObject;
    private BitmapHolder bitmapHolder;

    public LoginController(Activity activity, OnCompleteListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.basicController = BasicController.getInstance(activity);
        this.bitmapHolder = BitmapHolder.getInstance(activity);
        this.localUser = new User();
    }

    public void getUserData(final ParseUser parseUser, final SweetAlertDialog pDialog) {
        this.pDialog = pDialog;
        pDialog.setTitleText(FETCHING_DATA);
        if (parseUser != null) {
            parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                    .fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                userDetailsObject = parseObject;
                                localUser.setObjectID(userDetailsObject.getObjectId());
                                localUser.setFirstName(userDetailsObject.getString(Constants.USER_FIRST_NAME));
                                localUser.setLastName(userDetailsObject.getString(Constants.USER_LAST_NAME));
                                localUser.setPhoneNumber(userDetailsObject.getString(Constants.USER_PRIMARY_PHONE));
                                localUser.setPhoneVerified(userDetailsObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                                localUser.setMemberOfMasterCircle(userDetailsObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                                localUser.setEmail(userDetailsObject.getString(Constants.USER_PRIMARY_EMAIL));
                                fetchUserPreferences();
                            } else {
                                clearUser(e);
                            }
                        }
                    });
        } else {
            pDialog.cancel();
            AlertDialogs.showErrorDialog(activity, ErrorHandler.ERROR, ErrorHandler.ERROR_UNKNOWN, ErrorHandler.OKAY);
        }
    }


    private void fetchUserPreferences() {
        pDialog.setTitleText(FETCHING_SETTINGS);
        userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                .fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            userPreferenceObject = parseObject;
                            SharedPreferences prefs = basicController.getPrefs();
                            prefs.edit().putBoolean(Constants.SEND_SMS, userPreferenceObject.getBoolean(Constants.SEND_SMS)).apply();
                            prefs.edit().putBoolean(Constants.SEND_EMAIL, userPreferenceObject.getBoolean(Constants.SEND_EMAIL)).apply();
                            prefs.edit().putBoolean(Constants.SEND_PUSH, userPreferenceObject.getBoolean(Constants.SEND_PUSH)).apply();
                            prefs.edit().putBoolean(Constants.SHOW_POP_UPS, userPreferenceObject.getBoolean(Constants.SHOW_POP_UPS)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_SMS, userPreferenceObject.getBoolean(Constants.RECEIVE_SMS)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_PUSH, userPreferenceObject.getBoolean(Constants.RECEIVE_PUSH)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_EMAIL, userPreferenceObject.getBoolean(Constants.RECEIVE_EMAIL)).apply();
                            prefs.edit().putInt(Constants.NOTIFY_WITH_IN, userPreferenceObject.getInt(Constants.NOTIFY_WITH_IN)).apply();
                            prefs.edit().putInt(Constants.RESPOND_ALERT_WITH_IN, userPreferenceObject.getInt(Constants.RESPOND_ALERT_WITH_IN)).apply();
                            prefs.edit().putString(Constants.ALERT_MESSAGE, userPreferenceObject.getString(Constants.ALERT_MESSAGE)).apply();
                            new SaveLocalUser().execute();
                        } else {
                            clearUser(e);
                        }

                    }
                });
    }

    private void clearUser(ParseException e) {
        ParseUser.logOut();
        userDetailsObject = null;
        userPreferenceObject = null;
        pDialog.cancel();
        ErrorHandler.handleError(activity, e);
    }

    private void startObjectService() {
        Intent serviceIntent = new Intent(activity, ObjectService.class);
        activity.startService(serviceIntent);
        ObjectService.setUserDetailsObject(userDetailsObject);
        ObjectService.setUserPreferenceObject(userPreferenceObject);
        ObjectService.setBasicController(basicController);
        ObjectService.setBitmapHolder(bitmapHolder);
        ObjectFetcher.getCirclesFirstTime();
        ObjectFetcher.getUserMiscDetails();
    }

    private class SaveLocalUser extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText(Constants.SAVING);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int result = basicController.saveUser(localUser);
            if (result > 0) {
                SharedPreferences prefs = basicController.getPrefs();
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result > 0) {
                startObjectService();
                pDialog.cancel();
                listener.onComplete();
            } else {
                clearUser(null);
            }
        }
    }

}
