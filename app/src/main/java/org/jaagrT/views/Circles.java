package org.jaagrT.views;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.model.UserContact;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

public class Circles extends Fragment {

    private Activity activity;
    private BasicController basicController;

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

    private void setUpActivity(View rootView) {
        activity = getActivity();
        basicController = BasicController.getInstance(activity);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            int contactID = data.getIntExtra(Constants.CONTACT_ID, -1);
            UserContact contact = basicController.getContact(contactID);
            if (contact != null) {
                Utilities.logIt(contact.getName());
            }
        }
    }
}
