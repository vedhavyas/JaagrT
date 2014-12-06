package org.jaagrT;

import android.app.Activity;
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
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

import org.jaagrT.utils.Constants;
import org.jaagrT.utils.Utilities;


public class Login extends Activity {

    private FormEditText emailBox, passwordBox;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private GoogleApiClient apiClient;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = this;
        setUpActivity();
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

        if (requestCode == Constants.GOOGLE_CONNECTION_CODE && resultCode == RESULT_OK) {
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

    private void setUpActivity() {
        // Instantiate UI Elements
        Button fbBtn = (Button) findViewById(R.id.fbBtn);
        Button googleBtn = (Button) findViewById(R.id.googleBtn);
        final Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button forgotPassBtn = (Button) findViewById(R.id.forgotPasswordBtn);
        emailBox = (FormEditText) findViewById(R.id.emailBox);
        passwordBox = (FormEditText) findViewById(R.id.passwordBox);
        final FormEditText[] editTexts = {emailBox, passwordBox};

        //setup listeners
        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleFacebook.login(fbLoginListener);
            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiClient.connect();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(activity, Signup.class);
                startActivity(signUpIntent);
                overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    Utilities.snackIt(activity, "Data Validated", "Okay");
                }
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cropIntent = new Intent(activity, UserProfileEdit.class);
                startActivity(cropIntent);
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
                        result.startResolutionForResult(activity, Constants.GOOGLE_CONNECTION_CODE);
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
