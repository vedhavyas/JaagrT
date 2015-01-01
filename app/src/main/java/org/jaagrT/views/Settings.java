package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.listeners.ParseListener;
import org.jaagrT.model.Database;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.FormValidators;
import org.jaagrT.utilities.Utilities;

public class Settings extends ActionBarActivity {

    private static final String SETTINGS = "Settings";
    private Activity activity;
    private ObjectRetriever objectRetriever;
    private ParseObject userPreferenceObject;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        activity = this;
        setUpActivity();

    }

    private void setUpActivity() {
        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(SETTINGS);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        objectRetriever = ObjectRetriever.getInstance(activity);
        userPreferenceObject = objectRetriever.getUserPreferenceObject(new ParseListener() {
            @Override
            public void onComplete(ParseObject parseObject) {
                userPreferenceObject = parseObject;
            }
        });
        prefs = objectRetriever.getPrefs();

        Button logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    ParseUser.logOut();
                    clearUserData();
                }

                startStartScreenActivity();
            }
        });

        Button messageBtn = (Button) findViewById(R.id.alertMessageBtn);
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomMessageDialog();
            }
        });
    }

    private void startStartScreenActivity() {
        Intent startScreenIntent = new Intent(this, StartScreen.class);
        startScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startScreenIntent);
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void clearUserData() {
        Database db = Database.getInstance(activity);
        db.dropAllTables();
        prefs.edit().clear().apply();
    }

    private void showCustomMessageDialog() {
        View alertMessageView = getLayoutInflater().inflate(R.layout.alert_message, null);
        final MaterialEditText messageBox = (MaterialEditText) alertMessageView.findViewById(R.id.messageBox);
        messageBox.setText(prefs.getString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE));
        messageBox.addValidator(new FormValidators.EmptyFieldValidator());

        final MaterialEditText[] editTexts = {messageBox};

        new MaterialDialog.Builder(activity)
                .customView(alertMessageView, false)
                .title("Your Alert Message")
                .titleColor(getResources().getColor(R.color.teal_400))
                .positiveText("Set")
                .positiveColor(getResources().getColor(R.color.teal_400))
                .cancelable(true)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (Utilities.isEditBoxesValid(editTexts)) {
                            dialog.dismiss();
                            if (userPreferenceObject != null) {
                                userPreferenceObject.put(Constants.ALERT_MESSAGE, messageBox.getText().toString());
                                userPreferenceObject.saveEventually();
                            }
                            prefs.edit().putString(Constants.ALERT_MESSAGE, messageBox.getText().toString()).apply();
                        }
                    }
                })
                .build()
                .show();
    }

}
