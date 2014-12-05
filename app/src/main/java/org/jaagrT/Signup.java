package org.jaagrT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

import org.jaagrT.utils.AlertDialogs;
import org.jaagrT.utils.Constants;
import org.jaagrT.utils.FormValidators;
import org.jaagrT.utils.Utilities;


public class Signup extends Activity {

    private FormEditText nameBox, emailBox, passwordBox, phoneBox;
    private Activity activity;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private GoogleApiClient apiClient;
    private AlertDialog dialog;

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
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);

        if (requestCode == Constants.GOOGLE_CONNECTION && resultCode == RESULT_OK) {
            Utilities.logIt("Google connection resolved");
            apiClient.connect();

        }
    }

    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialog_error", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errorDialog");
    }

    private void registerUser() {
        final AlertDialog dialog = AlertDialogs.showProgressDialog(activity, "Signing up...");
        ParseUser newUser = new ParseUser();
        newUser.setUsername(emailBox.getText().toString());
        newUser.setPassword(passwordBox.getText().toString());
        newUser.setEmail(emailBox.getText().toString());
        newUser.put("name", nameBox.getText().toString());
        newUser.put("phone", phoneBox.getText().toString());

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dialog.cancel();
                if (e == null) {
                    Utilities.snackIt(activity, "Registration Successful", "Okay");
                } else {
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.errorDialog(activity, "Error", e.getMessage(), "okay").show();
                }
            }
        });
    }

    private void setUpActivity() {
        nameBox = (FormEditText) findViewById(R.id.nameBox);
        emailBox = (FormEditText) findViewById(R.id.emailBox);
        passwordBox = (FormEditText) findViewById(R.id.passwordBox);
        FormEditText verifyPasswordBox = (FormEditText) findViewById(R.id.verifyPasswordBox);
        phoneBox = (FormEditText) findViewById(R.id.phoneBox);


        phoneBox.addValidator(new FormValidators.PhoneNumberValidator());
        verifyPasswordBox.addValidator(new FormValidators.PasswordVerifyValidator(passwordBox));

        final FormEditText[] editTexts = {nameBox, emailBox, passwordBox, verifyPasswordBox, phoneBox};

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        Button fbBtn = (Button) findViewById(R.id.fbBtn);
        Button googleBtn = (Button) findViewById(R.id.googleBtn);


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
                simpleFacebook.login(fbLoginListener);
            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = AlertDialogs.showProgressDialog(activity, "Connecting to Google...");
                apiClient.connect();
            }
        });

        fbLoginListener = new OnLoginListener() {
            @Override
            public void onLogin() {
                Utilities.logIt("Connected with facebook");
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                Utilities.logIt("User didn't accept permissions");
            }

            @Override
            public void onThinking() {
                Utilities.logIt("On thinking");
            }

            @Override
            public void onException(Throwable throwable) {
                Utilities.logIt("Exception while logging into facebook");
            }

            @Override
            public void onFail(String s) {
                Utilities.logIt("Failed to login to facebook");
            }
        };

        GoogleApiClient.ConnectionCallbacks googleConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
                Utilities.logIt("Connected to Google");
            }

            @Override
            public void onConnectionSuspended(int i) {
                Utilities.logIt("Connection is suspended");
            }
        };

        GoogleApiClient.OnConnectionFailedListener googleOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Utilities.logIt("Google connection failed");
                if (result.hasResolution()) {
                    try {
                        result.startResolutionForResult(activity, Constants.GOOGLE_CONNECTION);
                    } catch (IntentSender.SendIntentException e) {
                        Utilities.logIt("Google connection - Exception caught!! Reconnecting...");
                        apiClient.connect();
                    }
                } else {
                    Utilities.logIt("Google connection - Issue has no resolution!!");
                    showErrorDialog(result.getErrorCode());
                }
            }
        };

        simpleFacebook = SimpleFacebook.getInstance(activity);
        apiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(googleConnectionCallbacks)
                .addOnConnectionFailedListener(googleOnConnectionFailedListener)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Plus.API)
                .build();
    }

}
