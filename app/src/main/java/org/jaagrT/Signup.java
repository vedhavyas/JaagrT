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
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.jaagrT.utils.AlertDialogs;
import org.jaagrT.utils.Constants;
import org.jaagrT.utils.Utilities;

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
                                saveDataToCloud();
                            } else {
                                Utilities.logIt("User Logged in");
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
                    //addDefaultSettingsToUser();
                } else {
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
    }

    private void saveDataToCloud() {
        Session session = ParseFacebookUtils.getSession();
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (response != null) {
                    try {
                        String email = (String) user.getProperty("email");
                        ParseUser parseUser = ParseUser.getCurrentUser();
                        parseUser.setEmail(email);
                        parseUser.saveInBackground();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.logIt(e.toString());
                    }

                }
            }
        }).executeAsync();
    }


    private void addDefaultSettingsToUser() {

        ParseObject settings = new ParseObject(Constants.USER_SETTINGS_CLASS);

        settings.put(Constants.SEND_SMS, true);
        settings.put(Constants.SEND_EMAIL, true);
        settings.put(Constants.SEND_PUSH, true);
        settings.put(Constants.RESCUER, false);
        settings.put(Constants.RECEIVE_SMS, false);
        settings.put(Constants.RECEIVE_PUSH, false);
        settings.put(Constants.RECEIVE_EMAIL, false);
        settings.put(Constants.SEND_ALERT_RANGE, 400);
        settings.put(Constants.RECEIVE_ALERT_RANGE, 400);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            settings.saveInBackground();
            currentUser.put(Constants.USER_SETTINGS, settings);
            currentUser.saveInBackground();
        }
    }


}
