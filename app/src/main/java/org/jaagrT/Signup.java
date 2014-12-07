package org.jaagrT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

import org.jaagrT.utils.AlertDialogs;
import org.jaagrT.utils.Constants;
import org.jaagrT.utils.FormValidators;
import org.jaagrT.utils.Utilities;

import java.io.InputStream;


public class Signup extends Activity {

    private static final int FB_PROFILE_PIC_HEIGHT = 800;
    private static final int FB_PROFILE_PIC_WIDTH = 800;
    private static final int GOOGLE_PLUS_PIC_DIMENSION = 800;
    private FormEditText nameBox, emailBox, passwordBox, phoneBox;
    private Activity activity;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private GoogleApiClient apiClient;
    private AlertDialog dialog;
    private String profileUrl = null;

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

        if (requestCode == Constants.GOOGLE_CONNECTION_CODE && resultCode == RESULT_OK) {
            Utilities.logIt("Google connection resolved");
            apiClient.connect();

        } else if (requestCode == Constants.CROP_IMAGE_CODE) {
            if (resultCode == RESULT_OK) {
                Utilities.logIt("Crop - Result OK");
                byte[] fileByte = data.getByteArrayExtra(Constants.CROPPED_IMAGE_ARRAY);
                if (fileByte != null) {
                    final ParseFile profilePic = new ParseFile(Constants.USER_PICTURE_FILE_NAME, fileByte);
                    attachProfileToUser(profilePic);
                }
            } else {
                Utilities.logIt("Crop - Result Failed");
            }
        }
    }

    private void attachProfileToUser(ParseFile file) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.put(Constants.USER_PROFILE_PICTURE, file);
            file.saveInBackground();
            currentUser.saveInBackground();
            Utilities.snackIt(activity, "Profile Attached", "Okay");
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
        final AlertDialog registrationDialog = AlertDialogs.showProgressDialog(activity, "Registering you...");
        final ParseUser newUser = new ParseUser();
        newUser.setUsername(emailBox.getText().toString());
        newUser.setPassword(passwordBox.getText().toString());
        newUser.setEmail(emailBox.getText().toString());
        newUser.put("name", nameBox.getText().toString());
        newUser.put("phone", phoneBox.getText().toString());

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    registrationDialog.cancel();
                    Utilities.snackIt(activity, "Registration Successful", "Okay");
                    addDefaultSettingsToUser();
                    pickProfilePicture();

                } else {
                    registrationDialog.cancel();
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.showErrorDialog(activity, "Error", e.getMessage(), "Oops!");
                }
            }
        });
    }

    private void addDefaultSettingsToUser() {

        ParseObject settings = new ParseObject(Constants.USER_SETTINGS_CLASS);

        settings.add(Constants.SEND_SMS, true);
        settings.add(Constants.SEND_EMAIL, true);
        settings.add(Constants.SEND_PUSH, true);
        settings.add(Constants.RESCUER, false);
        settings.add(Constants.RECEIVE_SMS, false);
        settings.add(Constants.RECEIVE_PUSH, false);
        settings.add(Constants.RECEIVE_EMAIL, false);
        settings.add(Constants.SEND_ALERT_RANGE, 400);
        settings.add(Constants.RECEIVE_ALERT_RANGE, 400);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.add(Constants.USER_SETTINGS, settings);
            currentUser.saveInBackground();
        }
    }

    private void pickProfilePicture() {
        if (profileUrl != null) {
            Utilities.logIt("Profile URL Found");
            new DownloadImageTask().execute(profileUrl);
        } else {
            startCropActivity(null, null);
        }
    }

    private void startCropActivity(Bitmap bitmap, AlertDialog cropDialog) {
        Intent cropIntent = new Intent(activity, ImageCrop.class);
        if (bitmap != null) {
            cropIntent.putExtra(Constants.ORIGINAL_IMAGE_ARRAY, Utilities.getBlob(bitmap));
        }
        startActivityForResult(cropIntent, Constants.CROP_IMAGE_CODE);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
        if (cropDialog != null && cropDialog.isShowing()) {
            cropDialog.cancel();
        }
    }

    private void getFacebookProfile() {
        OnProfileListener fbProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                nameBox.setText(profile.getName());
                emailBox.setText(profile.getEmail());
                profileUrl = profile.getPicture();
            }
        };

        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(FB_PROFILE_PIC_HEIGHT);
        pictureAttributes.setWidth(FB_PROFILE_PIC_WIDTH);
        pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);

        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.NAME)
                .add(Profile.Properties.EMAIL)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        Utilities.logIt("Downloading profile...");
        simpleFacebook.getProfile(properties, fbProfileListener);
    }

    private void getGooglePlusProfile() {
        try {
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(apiClient);
            if (currentPerson != null) {
                nameBox.setText(currentPerson.getDisplayName());
                emailBox.setText(Plus.AccountApi.getAccountName(apiClient));
                profileUrl = currentPerson.getImage().getUrl();
                profileUrl = profileUrl.substring(0,
                        profileUrl.length() - 2)
                        + GOOGLE_PLUS_PIC_DIMENSION;
            } else {
                Utilities.logIt("Google Profile info is null !!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        final Button fbBtn = (Button) findViewById(R.id.fbBtn);
        final Button googleBtn = (Button) findViewById(R.id.googleBtn);


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
                fbBtn.setEnabled(false);
                getFacebookProfile();
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
                googleBtn.setEnabled(false);
                getGooglePlusProfile();
                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }

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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        AlertDialog picDownloadDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utilities.logIt("Downloading your Profile Picture...");
            picDownloadDialog = AlertDialogs.showProgressDialog(activity, "Downloading your Profile Picture...");
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Utilities.logIt(e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bitmap = Utilities.compressBitmap(bitmap);
            if (bitmap != null) {
                startCropActivity(bitmap, picDownloadDialog);
            }
        }
    }


}
