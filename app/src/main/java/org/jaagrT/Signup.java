package org.jaagrT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Signup extends Activity {

    private FormEditText emailBox, passwordBox;
    private Activity activity;
    private User localUser;
    private String profileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        activity = this;
        setUpActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void setUpActivity() {
        emailBox = (FormEditText) findViewById(R.id.emailBox);
        passwordBox = (FormEditText) findViewById(R.id.passwordBox);

        final FormEditText[] editTexts = {emailBox, passwordBox};

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        final Button fbBtn = (Button) findViewById(R.id.fbBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    registerUser();
                }

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                pDialog.setTitleText("Connecting to Facebook...");
                pDialog.show();
                ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL), activity, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            if (parseUser == null) {
                                pDialog.cancel();
                                Utilities.snackIt(activity, "Failed to login with Facebook", "Okay");
                            } else if (parseUser.isNew()) {
                                pDialog.setTitleText("Connected to Facebook...");
                                getFbProfileData(activity, pDialog);
                            } else {
                                Utilities.logIt(parseUser.getEmail());
                            }
                        } else {
                            pDialog.cancel();
                            AlertDialogs.showErrorDialog(activity, "Login Error", e.getMessage(), "Oops!");
                        }
                    }
                });
            }
        });

    }

    private void registerUser() {
        final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
        pDialog.setTitleText("Registering you now...");
        pDialog.show();
        final ParseUser newUser = new ParseUser();
        newUser.setUsername(emailBox.getText().toString());
        newUser.setPassword(passwordBox.getText().toString());
        newUser.setEmail(emailBox.getText().toString());

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    pDialog.setTitleText("Registration Successful..");
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    if (parseUser != null) {
                        localUser = new User();
                        localUser.setEmail(parseUser.getEmail());
                        saveUserDetails(parseUser, pDialog);
                    }
                } else {
                    pDialog.cancel();
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
    }

    private void saveUserDetails(final ParseUser parseUser, final SweetAlertDialog pDialog) {
        pDialog.setTitleText("Finalizing Registration...");
        ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    localUser.setMemberOfMasterCircle(false);
                    saveDefaultPreferences(activity, parseUser, pDialog);
                } else {
                    pDialog.cancel();
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });

        parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
        parseUser.saveInBackground();

    }

    private void getFbProfileData(final Activity activity, final SweetAlertDialog pDialog) {
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
                        localUser.setMemberOfMasterCircle(false);
                        profileUrl = "https://graph.facebook.com/" + fbUser.getId() + "/picture?type=large";
                        saveFbUserDetails(activity, parseUser, fbUser, pDialog);
                    } catch (Exception e) {
                        pDialog.cancel();
                        AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                    }

                }
            }
        }).executeAsync();
    }

    private void saveFbUserDetails(final Activity activity, final ParseUser parseUser, GraphUser fbUser, final SweetAlertDialog pDialog) {
        pDialog.setTitleText("Completing Registration...");
        final ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_FIRST_NAME, fbUser.getFirstName());
        userDetailsObject.put(Constants.USER_LAST_NAME, fbUser.getLastName());
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveDefaultPreferences(activity, parseUser, pDialog);
                } else {
                    pDialog.cancel();
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
        parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
        parseUser.saveInBackground();

    }

    private void saveDefaultPreferences(final Activity activity, final ParseUser parseUser, final SweetAlertDialog pDialog) {
        final ParseObject userPreferenceObject = new ParseObject(Constants.USER_COMMUNICATION_PREFERENCE_CLASS);
        userPreferenceObject.put(Constants.SEND_SMS, true);
        userPreferenceObject.put(Constants.SEND_EMAIL, true);
        userPreferenceObject.put(Constants.SEND_PUSH, true);
        userPreferenceObject.put(Constants.IS_RESPONDER, false);
        userPreferenceObject.put(Constants.RECEIVE_SMS, false);
        userPreferenceObject.put(Constants.RECEIVE_PUSH, false);
        userPreferenceObject.put(Constants.RECEIVE_EMAIL, false);
        userPreferenceObject.put(Constants.NOTIFY_WITH_IN, 400);
        userPreferenceObject.put(Constants.RESPOND_ALERT_WITH_IN, 400);
        userPreferenceObject.saveInBackground();

        parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                .fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject userDetailsObject, ParseException e) {
                        if (e == null) {
                            userDetailsObject.put(Constants.USER_COMMUNICATION_PREFERENCE_ROW, userPreferenceObject);
                            userDetailsObject.saveInBackground();
                            new SaveUserLocally(activity, pDialog).execute();
                        } else {
                            pDialog.cancel();
                            AlertDialogs.showErrorDialog(activity, "Error Fetching", e.getMessage(), "Oops!");
                        }
                    }
                });
    }

    private void startPickPictureActivity(Activity activity) {
        Intent pickPictureIntent = new Intent(activity, PickPicture.class);
        pickPictureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pickPictureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (profileUrl != null) {
            pickPictureIntent.putExtra(Constants.PICTURE_URL_STRING, profileUrl);
        }
        startActivity(pickPictureIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private class SaveUserLocally extends AsyncTask<Void, Void, Void> {

        SweetAlertDialog pDialog;
        Activity activity;

        private SaveUserLocally(Activity activity, SweetAlertDialog pDialog) {
            this.activity = activity;
            this.pDialog = pDialog;
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(Constants.SEND_SMS, true).apply();
            prefs.edit().putBoolean(Constants.SEND_EMAIL, true).apply();
            prefs.edit().putBoolean(Constants.SEND_PUSH, true).apply();
            prefs.edit().putBoolean(Constants.IS_RESPONDER, false).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_SMS, false).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_PUSH, false).apply();
            prefs.edit().putBoolean(Constants.RECEIVE_EMAIL, false).apply();
            prefs.edit().putInt(Constants.NOTIFY_WITH_IN, 400).apply();
            prefs.edit().putInt(Constants.RESPOND_ALERT_WITH_IN, 400).apply();

            Database db = Database.getInstance(activity);
            db.setTableName(Database.USER_TABLE);
            int result = (int) db.saveUser(localUser);
            if (result > 0) {
                Utilities.logIt("Data saved locally");
                prefs.edit().putInt(Constants.LOCAL_USER_ID, result).apply();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            startPickPictureActivity(activity);
        }
    }


}
