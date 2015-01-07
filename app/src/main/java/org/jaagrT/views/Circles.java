package org.jaagrT.views;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.model.UserContact;
import org.jaagrT.services.ObjectService;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Circles extends Fragment {

    private Activity activity;
    private BasicController basicController;
    private ParseObject userDetailsObject;

    public Circles() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_circles, container, false);
        setUpActivity(rootView);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            int contactID = data.getIntExtra(Constants.CONTACT_ID, -1);
            UserContact contact = basicController.getContact(contactID);
            if (contact != null) {
                tryAndAddTheContact(contact);
            }
        }
    }

    private void setUpActivity(View rootView) {
        activity = getActivity();
        basicController = BasicController.getInstance(activity);
        userDetailsObject = ObjectService.getUserDetailsObject();
        FloatingActionButton addBtn = (FloatingActionButton) rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContactActivity();
            }
        });
    }

    private void startPickContactActivity() {
        Intent pickContactIntent = new Intent(activity, PickContact.class);
        startActivityForResult(pickContactIntent, Constants.PICK_CONTACT);
        activity.overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void tryAndAddTheContact(final UserContact contact) {
        final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
        pDialog.setTitleText(Constants.PLEASE_WAIT).show();

        ParseQuery<ParseObject> userSearchQuery = ParseQuery.getQuery(Constants.USER_DETAILS_CLASS);
        for (String email : contact.getEmailList()) {
            Utilities.logData(email, Log.INFO);
        }
        userSearchQuery.whereContainedIn(Constants.USER_PRIMARY_EMAIL, Arrays.asList(contact.getEmailList()));
        userSearchQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.size() > 0) {
                        if (userDetailsObject != null) {
                            for (ParseObject parseObject : parseObjects) {
                                Utilities.logData(parseObject.getString(Constants.USER_PRIMARY_EMAIL), Log.DEBUG);
                                ParseRelation<ParseObject> relation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                                relation.add(parseObject);
                            }
                            userDetailsObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    pDialog.cancel();
                                    if (e == null) {
                                        Utilities.logData("User saved", Log.DEBUG);
                                    } else {
                                        Utilities.logData("Failed to save", Log.DEBUG);
                                    }
                                }
                            });
                        }
                    } else {
                        pDialog.cancel();
                        Utilities.logData("No users found", Log.DEBUG);
                    }
                } else {
                    pDialog.cancel();
                    AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.CHECK_INTERNET, Constants.OKAY);
                }
            }
        });
    }

}
