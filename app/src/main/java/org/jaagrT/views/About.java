package org.jaagrT.views;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.jaagrT.R;


public class About extends Fragment implements View.OnClickListener {

    private Activity activity;

    public About() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        activity = getActivity();
        setUpActivity(rootView);
        return rootView;
    }

    private void setUpActivity(View rootView) {
        Button aboutBtn = (Button) rootView.findViewById(R.id.aboutBtn);
        Button helpBtn = (Button) rootView.findViewById(R.id.helpBtn);
        Button feedbackBtn = (Button) rootView.findViewById(R.id.feedbackBtn);
        Button attributionBtn = (Button) rootView.findViewById(R.id.attributionsBtn);
        Button licenseBtn = (Button) rootView.findViewById(R.id.licensesBtn);


        aboutBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        feedbackBtn.setOnClickListener(this);
        attributionBtn.setOnClickListener(this);
        licenseBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        //TODO finish this up
        Toast.makeText(activity, "Not implemented Yet", Toast.LENGTH_SHORT).show();
    }
}
