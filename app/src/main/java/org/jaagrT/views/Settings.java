package org.jaagrT.views;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.Database;
import org.jaagrT.services.ObjectService;
import org.jaagrT.widgets.rangebar.RangeBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Settings extends Fragment {

    private static final String SET = "Set";
    private static final String KMS = " Kms";
    private static final String ALERT_MESSAGE = "Alert Message";
    private static final String RESPOND_WITH_IN = "Respond with in";
    private static final String ALERT_WITH_IN = "Alert with in";

    private static final int IN_ALERT_RANGE = 1;
    private static final int OUT_ALERT_RANGE = 2;
    private static final String[] INCOMING_ALERT_LIST = {Constants.RECEIVE_SMS, Constants.RECEIVE_PUSH, Constants.RECEIVE_EMAIL};
    private static final String[] OUTGOING_ALERT_LIST = {Constants.SEND_SMS, Constants.SEND_PUSH, Constants.SEND_EMAIL};
    private Activity activity;
    private BasicController basicController;
    private ParseObject userPreferenceObject;
    private SharedPreferences prefs;
    private TextView inAlertRangeView, outAlertRangeView;
    private BitmapHolder bitmapHolder;

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

        basicController = BasicController.getInstance(activity);
        bitmapHolder = BitmapHolder.getInstance(activity);
        userPreferenceObject = ObjectService.getUserPreferenceObject();
        prefs = basicController.getPrefs();

        Button inAlertsBtn = (Button) rootView.findViewById(R.id.inAlertsBtn);
        inAlertsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInAlertsDialog();
            }
        });

        Button inAlertRangeBtn = (Button) rootView.findViewById(R.id.inAlertRange);
        inAlertRangeView = (TextView) rootView.findViewById(R.id.inAlertRangeView);
        inAlertRangeView.setText(prefs.getInt(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE) + KMS);
        inAlertRangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRangeSelector(IN_ALERT_RANGE);
            }
        });

        Button outAlertsBtn = (Button) rootView.findViewById(R.id.outAlertsBtn);
        outAlertsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOutAlertsDialog();
            }
        });

        Button outAlertRangeBtn = (Button) rootView.findViewById(R.id.outAlertRange);
        outAlertRangeView = (TextView) rootView.findViewById(R.id.outAlertRangeView);
        outAlertRangeView.setText(prefs.getInt(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE) + KMS);
        outAlertRangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRangeSelector(OUT_ALERT_RANGE);
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
                    new ClearUserDetails().execute();
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
        activity.overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(activity, ObjectService.class);
        activity.stopService(serviceIntent);
    }

    private void showCustomMessageDialog() {
        View alertMessageView = activity.getLayoutInflater().inflate(R.layout.alert_message, null);
        final MaterialEditText messageBox = (MaterialEditText) alertMessageView.findViewById(R.id.messageBox);
        messageBox.setText(prefs.getString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE));
        messageBox.addValidator(new FormValidators.EmptyFieldValidator());

        final List<MaterialEditText> editTexts = new ArrayList<>();
        editTexts.add(messageBox);

        new MaterialDialog.Builder(activity)
                .customView(alertMessageView, false)
                .title(ALERT_MESSAGE)
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
                .itemsCallbackMultiChoice(getUserAlertChoices(R.string.incoming_alerts), new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog dialog, Integer[] integers, CharSequence[] charSequences) {
                        dialog.dismiss();
                        setUserAlertChoices(R.string.incoming_alerts, integers);
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
                .itemsCallbackMultiChoice(getUserAlertChoices(R.string.outgoing_alerts), new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog dialog, Integer[] integers, CharSequence[] charSequences) {
                        dialog.dismiss();
                        setUserAlertChoices(R.string.outgoing_alerts, integers);
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
                .itemsCallbackSingleChoice(getUserNotificationChoice(), new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int choice, CharSequence charSequence) {
                        dialog.dismiss();
                        if (choice == 0) {
                            prefs.edit().putBoolean(Constants.SHOW_POP_UPS, true).apply();
                            if (userPreferenceObject != null) {
                                userPreferenceObject.put(Constants.SHOW_POP_UPS, true);
                            }
                        } else {
                            prefs.edit().putBoolean(Constants.SHOW_POP_UPS, false).apply();
                            if (userPreferenceObject != null) {
                                userPreferenceObject.put(Constants.SHOW_POP_UPS, false);
                            }
                        }

                        if (userPreferenceObject != null) {
                            userPreferenceObject.saveEventually();
                        }
                    }
                })
                .build()
                .show();
    }

    private Integer[] getUserAlertChoices(int whichAlert) {
        if (whichAlert == R.string.incoming_alerts || whichAlert == R.string.outgoing_alerts) {
            ArrayList<Integer> choices = new ArrayList<>();
            String[] list;
            if (whichAlert == R.string.incoming_alerts) {
                list = INCOMING_ALERT_LIST;
            } else {
                list = OUTGOING_ALERT_LIST;
            }
            for (int i = 0; i < list.length; i++) {
                if (prefs.getBoolean(list[i], true)) {
                    choices.add(i);
                }
            }
            return arrayToInteger(choices);
        }

        return null;
    }

    private int getUserNotificationChoice() {
        if (prefs.getBoolean(Constants.SHOW_POP_UPS, true)) {
            return 0;
        }
        return 1;
    }

    private void setUserAlertChoices(int whichAlert, Integer[] integers) {
        String[] list;
        ArrayList<Integer> userChoices = new ArrayList<>(Arrays.asList(integers));
        if (whichAlert == R.string.incoming_alerts) {
            list = INCOMING_ALERT_LIST;
        } else {
            list = OUTGOING_ALERT_LIST;
        }
        for (int i = 0; i < list.length; i++) {
            if (userChoices.contains(i)) {
                prefs.edit().putBoolean(list[i], true).apply();
                if (userPreferenceObject != null) {
                    userPreferenceObject.put(list[i], true);
                }
            } else {
                prefs.edit().putBoolean(list[i], false).apply();
                if (userPreferenceObject != null) {
                    userPreferenceObject.put(list[i], false);
                }
            }
        }

        if (userPreferenceObject != null) {
            userPreferenceObject.saveEventually();
        }
    }

    private Integer[] arrayToInteger(ArrayList<Integer> choices) {
        if (choices.size() > 0) {
            Integer[] integerChoices = new Integer[choices.size()];
            for (int i = 0; i < choices.size(); i++) {
                integerChoices[i] = choices.get(i);
            }

            return integerChoices;
        }
        return null;
    }

    private void showRangeSelector(final int which) {
        View rangeSelectorView = activity.getLayoutInflater().inflate(R.layout.alert_distance, null);
        final RangeBar seekBar = (RangeBar) rangeSelectorView.findViewById(R.id.seekBar);
        if (which == IN_ALERT_RANGE) {
            seekBar.setSeekPinByValue(prefs.getInt(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE));
        } else {
            seekBar.setSeekPinByValue(prefs.getInt(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE));
        }

        MaterialDialog.Builder dialog = new MaterialDialog.Builder(activity)
                .customView(rangeSelectorView, false)
                .titleColor(getResources().getColor(R.color.teal_400))
                .positiveText(SET)
                .positiveColor(getResources().getColor(R.color.teal_400))
                .cancelable(true)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        int choice = Integer.parseInt(seekBar.getPinValue(seekBar.getRightIndex()));
                        if (which == IN_ALERT_RANGE) {
                            inAlertRangeView.setText(String.valueOf(choice) + KMS);
                            prefs.edit().putInt(Constants.RESPOND_ALERT_WITH_IN, choice).apply();
                            if (userPreferenceObject != null) {
                                userPreferenceObject.put(Constants.RESPOND_ALERT_WITH_IN, choice);
                                userPreferenceObject.saveEventually();
                            }
                        } else {
                            outAlertRangeView.setText(String.valueOf(choice) + KMS);
                            prefs.edit().putInt(Constants.NOTIFY_WITH_IN, choice).apply();
                            if (userPreferenceObject != null) {
                                userPreferenceObject.put(Constants.NOTIFY_WITH_IN, choice);
                                userPreferenceObject.saveEventually();
                            }
                        }
                    }
                });

        if (which == IN_ALERT_RANGE) {
            dialog.title(RESPOND_WITH_IN);
        } else {
            dialog.title(ALERT_WITH_IN);
        }

        dialog.build().show();
    }

    private class ClearUserDetails extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            basicController.dropTable(Database.CIRCLES_TABLE);
            basicController.dropTable(Database.USER_TABLE);
            bitmapHolder.deleteAllImages();
            prefs.edit().clear().apply();
            stopService();
            return null;
        }
    }
}
