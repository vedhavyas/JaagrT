package org.jaagrT.controller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.listeners.BasicListener;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */
public class LoginController {

    private Activity activity;
    private BasicListener listener;
    private User localUser;
    private SweetAlertDialog pDialog;
    private ObjectRetriever objectRetriever;
    private ParseObject userDetailsObject;

    public LoginController(Activity activity, BasicListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.objectRetriever = ObjectRetriever.getInstance(activity);
        this.localUser = new User();
    }

    public void getUserData(final ParseUser parseUser, final SweetAlertDialog pDialog) {
        this.pDialog = pDialog;
        pDialog.setTitleText("Downloading your data...");
        if (parseUser != null) {
            parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                    .fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                userDetailsObject = parseObject;
                                objectRetriever.setUserDetailsObject(userDetailsObject);
                                localUser.setFirstName(userDetailsObject.getString(Constants.USER_FIRST_NAME));
                                localUser.setLastName(userDetailsObject.getString(Constants.USER_LAST_NAME));
                                localUser.setPhoneNumber(userDetailsObject.getString(Constants.USER_PRIMARY_PHONE));
                                localUser.setPhoneVerified(userDetailsObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                                localUser.setMemberOfMasterCircle(userDetailsObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                                localUser.setEmail(parseUser.getEmail());
                                if (userDetailsObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                                    userDetailsObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE).getDataInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] thumbnailBytes, ParseException e) {
                                            if (e == null) {
                                                localUser.setThumbnailPicture(Utilities.getBitmapFromBlob(thumbnailBytes));
                                            }
                                            fetchUserPreferences();
                                        }
                                    });
                                } else {
                                    fetchUserPreferences();
                                }
                            } else {
                                ParseUser.logOut();
                                pDialog.cancel();
                                AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Okay");
                            }
                        }
                    });
        } else {
            pDialog.cancel();
            AlertDialogs.showErrorDialog(activity, "Error", "Failed to download data", "Okay");
        }
    }


    private void fetchUserPreferences() {
        pDialog.setTitleText("Downloading your preferences...");
        userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                .fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject userPreferenceObject, ParseException e) {
                        if (e == null) {
                            objectRetriever.setUserPreferenceObject(userPreferenceObject);
                            SharedPreferences prefs = objectRetriever.getPrefs();
                            prefs.edit().putBoolean(Constants.SEND_SMS, userPreferenceObject.getBoolean(Constants.SEND_SMS)).apply();
                            prefs.edit().putBoolean(Constants.SEND_EMAIL, userPreferenceObject.getBoolean(Constants.SEND_EMAIL)).apply();
                            prefs.edit().putBoolean(Constants.SEND_PUSH, userPreferenceObject.getBoolean(Constants.SEND_PUSH)).apply();
                            prefs.edit().putBoolean(Constants.IS_RESPONDER, userPreferenceObject.getBoolean(Constants.IS_RESPONDER)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_SMS, userPreferenceObject.getBoolean(Constants.RECEIVE_SMS)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_PUSH, userPreferenceObject.getBoolean(Constants.RECEIVE_PUSH)).apply();
                            prefs.edit().putBoolean(Constants.RECEIVE_EMAIL, userPreferenceObject.getBoolean(Constants.RECEIVE_EMAIL)).apply();
                            prefs.edit().putInt(Constants.NOTIFY_WITH_IN, userPreferenceObject.getInt(Constants.NOTIFY_WITH_IN)).apply();
                            prefs.edit().putInt(Constants.RESPOND_ALERT_WITH_IN, userPreferenceObject.getInt(Constants.RESPOND_ALERT_WITH_IN)).apply();
                            prefs.edit().putString(Constants.ALERT_MESSAGE, userPreferenceObject.getString(Constants.ALERT_MESSAGE)).apply();
                        }
                        new SaveLocalUser().execute();
                    }
                });
    }

    private class SaveLocalUser extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText("Saving  Data...");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Database db = Database.getInstance(activity, Database.USER_TABLE);
            return (int) db.saveUser(localUser);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                SharedPreferences prefs = objectRetriever.getPrefs();
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
                listener.onComplete();
            } else {
                AlertDialogs.showErrorDialog(activity, "Error", "Failed to save User", "Okay");
            }
        }
    }

}
