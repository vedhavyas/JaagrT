package org.jaagrT;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.andreabaccega.widget.FormEditText;

import org.jaagrT.utilities.FormValidators;
import org.jaagrT.utilities.Utilities;


public class UserProfileEdit extends Activity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);
        activity = this;
        setUpActivity();
    }

    private void setUpActivity() {

        FormEditText nameBox = (FormEditText) findViewById(R.id.nameBox);
        FormEditText emailBox = (FormEditText) findViewById(R.id.emailBox);
        FormEditText phoneBox = (FormEditText) findViewById(R.id.phoneBox);
        ImageView profilePicView = (ImageView) findViewById(R.id.profilePicView);
        Button updateBtn = (Button) findViewById(R.id.updateBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

        phoneBox.addValidator(new FormValidators.PhoneNumberValidator());
        final FormEditText[] editTexts = {nameBox, emailBox, phoneBox};

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    Utilities.snackIt(activity, "Data Validated", "Okay");
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
