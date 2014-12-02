package org.jaagrT;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import org.jaagrT.utils.Utilities;


public class Signup extends Activity {

    private EditText nameBox, emailBox, passwordBox, verifyPasswordBox, phoneBox;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        activity = this;
        new SetUpUI().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private class SetUpUI extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Instantiate UI Elements
            nameBox = (EditText) findViewById(R.id.nameBox);
            emailBox = (EditText) findViewById(R.id.emailBox);
            passwordBox = (EditText) findViewById(R.id.passwordBox);
            verifyPasswordBox = (EditText) findViewById(R.id.verifyPasswordBox);
            phoneBox = (EditText) findViewById(R.id.phoneBox);

            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

            //Setup Listeners
            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar snackbar = Utilities.getSnackBar(activity);
                    snackbar.text("Signup")
                            .textColorResource(R.color.black)
                            .actionLabel("Okay")
                            .actionColorResource(R.color.black)
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked() {

                                }
                            })
                            .show(activity);
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            return null;
        }
    }
}
