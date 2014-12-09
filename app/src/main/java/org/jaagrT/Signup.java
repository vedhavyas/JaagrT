package org.jaagrT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

import java.util.Arrays;


public class Signup extends Activity {

    private FormEditText emailBox, passwordBox;
    private Activity activity;

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
                final AlertDialog fbLoginDialog = AlertDialogs.showProgressDialog(activity, "Connecting to Facebook...");
                ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL), activity, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        fbLoginDialog.cancel();
                        if (e == null) {
                            if (user == null) {
                                Utilities.snackIt(activity, "Failed to login with Facebook", "Okay");
                            } else if (user.isNew()) {
                                getFbProfileData();
                            } else {
                                Utilities.logIt(user.getEmail());
                            }
                        } else {
                            AlertDialogs.showErrorDialog(activity, "Login Error", e.getMessage(), "Oops!");
                        }
                    }
                });
            }
        });

    }

    private void registerUser() {
        final AlertDialog registrationDialog = AlertDialogs.showProgressDialog(activity, "Registering you...");
        final ParseUser newUser = new ParseUser();
        newUser.setUsername(emailBox.getText().toString());
        newUser.setPassword(passwordBox.getText().toString());
        newUser.setEmail(emailBox.getText().toString());

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                registrationDialog.cancel();
                if (e == null) {
                    Utilities.snackIt(activity, "Registration Successful", "Okay");
                    ParseUser parseUser = ParseUser.getCurrentUser();
                    if (parseUser != null) {
                        saveUserDetails(parseUser);
                    }
                } else {
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
    }

    private void saveUserDetails(final ParseUser parseUser) {
        ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveDefaultPreferences(parseUser);
                }
            }
        });

        parseUser.put(Constants.USER_DETAILS_ROW, userDetailsObject);
        parseUser.saveInBackground();

    }

    private void getFbProfileData() {
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
                        saveFbUserDetails(parseUser, fbUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.logIt(e.toString());
                    }

                }
            }
        }).executeAsync();
    }

    private void saveFbUserDetails(final ParseUser parseUser, GraphUser fbUser) {
        final ParseObject userDetailsObject = new ParseObject(Constants.USER_DETAILS_CLASS);
        userDetailsObject.put(Constants.USER_FIRST_NAME, fbUser.getFirstName());
        userDetailsObject.put(Constants.USER_LAST_NAME, fbUser.getLastName());
        userDetailsObject.put(Constants.USER_MEMBER_OF_MASTER_CIRCLE, false);
        userDetailsObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                saveDefaultPreferences(parseUser);
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
                        } else {
                            AlertDialogs.showErrorDialog(activity, "Error Fetching", e.getMessage(), "Oops!");
                        }
                    }
                });
    }


}
