package org.jaagrT.controller;

import android.app.Activity;
import android.content.Intent;
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
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.listeners.OnCompleteListener;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 12/12/14.
 * Project JaagrT
 */
public class SignUpController {

    private static final String SIGN_UP_SUCCESS = "Signup Successful!!";
    private static final String FINALIZING_SIGNUP = "Finalizing Signup...";
    private static final String INVITE_ACCEPTED = "Invitation Accepted";
    private Activity activity;
    private SweetAlertDialog pDialog;
    private User localUser;
    private OnCompleteListener listener;
    private ParseObject userDetailsObject, userPreferenceObject;
    private BasicController basicController;


    public SignUpController(Activity activity, OnCompleteListener listener, SweetAlertDialog pDialog) {
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
                    ErrorHandler.handleError(activity, e);
                }
            }
        });
    }

    private void saveUserDetails(final ParseUser parseUser) {
        pDialog.setTitleText(FINALIZING_SIGNUP);
        userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.put(Constants.USER_PRIMARY_EMAIL, parseUser.getEmail());
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    localUser.setObjectID(userDetailsObject.getObjectId());
                    parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
                    parseUser.saveInBackground();
                    saveDefaultPreferences(parseUser);
                } else {
                    clearUser(e);
                }
            }
        });
    }

    public void facebookRegistration() {
        localUser = new User();
        Session session = ParseFacebookUtils.getSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(final GraphUser fbUser, Response response) {
                if (response != null) {
                    try {
                        final String email = (String) fbUser.getProperty(Constants.EMAIL);
                        final ParseUser parseUser = ParseUser.getCurrentUser();
                        parseUser.setEmail(email);
                        parseUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    localUser.setEmail(email);
                                    localUser.setFirstName(fbUser.getFirstName());
                                    localUser.setLastName(fbUser.getLastName());
                                    saveFbUserDetails(parseUser, fbUser);
                                } else {
                                    parseUser.deleteInBackground();
                                    clearUser(e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        clearUser(null);
                    }

                } else {
                    clearUser(null);
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
        userDetailsObject.put(Constants.USER_PRIMARY_EMAIL, localUser.getEmail());
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    localUser.setObjectID(userDetailsObject.getObjectId());
                    parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
                    parseUser.saveInBackground();
                    saveDefaultPreferences(parseUser);
                } else {
                    clearUser(e);
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
    }


    private class SaveUserLocally extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText(Constants.SAVING);
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
                new Thread(new ChangeInviteStatus()).start();
            } else {
                clearUser(null);
            }
        }
    }

    private class ChangeInviteStatus implements Runnable {

        @Override
        public void run() {
            User user = basicController.getUser();
            ParseQuery<ParseObject> inviteSearchQuery = ParseQuery.getQuery(Constants.INVITATION_CLASS);
            inviteSearchQuery.whereContains(Constants.EMAIL, user.getEmail());
            inviteSearchQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        parseObject.put(Constants.INVITE_STATUS, INVITE_ACCEPTED);
                        parseObject.saveEventually();
                    } else {
                        ErrorHandler.handleError(null, e);
                    }
                }
            });
        }
    }
}
