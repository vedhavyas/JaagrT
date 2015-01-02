package org.jaagrT.views;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class Settings extends Fragment {

    private static final String SET = "Set";
    private Activity activity;
    private ObjectRetriever objectRetriever;
    private ParseObject userPreferenceObject;
    private SharedPreferences prefs;

    public Settings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        activity = getActivity();
        setUpActivity(rootView);
        return rootView;
    }

    private void setUpActivity(View rootView) {

        objectRetriever = ObjectRetriever.getInstance(activity);
        userPreferenceObject = objectRetriever.getUserPreferenceObject(new ParseListener() {
            @Override
            public void onComplete(ParseObject parseObject) {
                userPreferenceObject = parseObject;
            }
        });
        prefs = objectRetriever.getPrefs();

        Button inAlertsBtn = (Button) rootView.findViewById(R.id.inAlertsBtn);
        inAlertsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInAlertsDialog();
            }
        });

        Button outAlertsBtn = (Button) rootView.findViewById(R.id.outAlertsBtn);
        outAlertsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOutAlertsDialog();
            }
        });

        Button messageBtn = (Button) rootView.findViewById(R.id.alertMessageBtn);
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomMessageDialog();
            }
        });

        Button notificationPopUpBtn = (Button) rootView.findViewById(R.id.notificationPopUpBtn);
        notificationPopUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotificationPopUpDialog();
            }
        });

        Button logoutBtn = (Button) rootView.findViewById(R.id.logoutBtn);
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
    }

    private void startStartScreenActivity() {
        Intent startScreenIntent = new Intent(activity, StartScreen.class);
        startScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startScreenIntent);
        activity.overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void clearUserData() {
        Database db = Database.getInstance(activity);
        db.dropAllTables();
        prefs.edit().clear().apply();
        objectRetriever.clearAllObjects();
    }

    private void showCustomMessageDialog() {
        View alertMessageView = activity.getLayoutInflater().inflate(R.layout.alert_message, null);
        final MaterialEditText messageBox = (MaterialEditText) alertMessageView.findViewById(R.id.messageBox);
        messageBox.setText(prefs.getString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE));
        messageBox.addValidator(new FormValidators.EmptyFieldValidator());

        final MaterialEditText[] editTexts = {messageBox};

        new MaterialDialog.Builder(activity)
                .customView(alertMessageView, false)
                .title("Your Alert Message")
                .titleColor(getResources().getColor(R.color.teal_400))
                .positiveText(SET)
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

    private void showInAlertsDialog() {
        new MaterialDialog.Builder(activity)
                .title(R.string.incoming_alerts)
                .titleColor(getResources().getColor(R.color.teal_400))
                .autoDismiss(false)
                .cancelable(true)
                .positiveText(SET)
                .positiveColor(getResources().getColor(R.color.teal_400))
                .items(R.array.incomingAlerts)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, Integer[] integers, CharSequence[] charSequences) {

                    }
                })
                .build()
                .show();
    }

    private void showOutAlertsDialog() {
        new MaterialDialog.Builder(activity)
                .title(R.string.outgoing_alerts)
                .titleColor(getResources().getColor(R.color.teal_400))
                .autoDismiss(false)
                .cancelable(true)
                .positiveText(SET)
                .positiveColor(getResources().getColor(R.color.teal_400))
                .items(R.array.outgoingAlerts)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, Integer[] integers, CharSequence[] charSequences) {

                    }
                })
                .build()
                .show();
    }

    private void showNotificationPopUpDialog() {
        new MaterialDialog.Builder(activity)
                .title(R.string.pop_up_notifications)
                .titleColor(getResources().getColor(R.color.teal_400))
                .autoDismiss(false)
                .cancelable(true)
                .positiveText(SET)
                .positiveColor(getResources().getColor(R.color.teal_400))
                .items(R.array.notificationPopUP)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

                    }
                })
                .build()
                .show();
    }


}
