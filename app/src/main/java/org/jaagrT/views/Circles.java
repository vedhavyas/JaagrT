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

public class Circles extends Fragment {

    private Activity activity;

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
        startActivity(pickContactIntent);
        activity.overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }
}
