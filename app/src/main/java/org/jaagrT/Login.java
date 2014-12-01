package org.jaagrT;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;


public class Login extends Activity {

    private EditText emailBox, passwordBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        new SetupUI().execute(this);
    }


    private class SetupUI extends AsyncTask<Activity, Void, Void>{

        @Override
        protected Void doInBackground(Activity... activities) {

            // Instantiate UI Elements
            Button fbBtn = (Button) findViewById(R.id.fbBtn);
            Button googleBtn = (Button) findViewById(R.id.googleBtn);
            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            Button loginBtn = (Button) findViewById(R.id.loginBtn);
            Button forgotPassBtn = (Button) findViewById(R.id.forgotPasswordBtn);
            emailBox = (EditText)findViewById(R.id.emailBox);
            passwordBox = (EditText)findViewById(R.id.passwordBox);
            final Activity activity = activities[0];

            SimpleFacebook mSimpleFacebook = SimpleFacebook.getInstance(activity);

            //setup listeners
            fbBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(activity, "Facebook Login", Toast.LENGTH_SHORT).show();
                }
            });

            googleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            forgotPassBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            OnLoginListener fbLoginListener = new OnLoginListener() {
                @Override
                public void onLogin() {

                }

                @Override
                public void onNotAcceptingPermissions(Permission.Type type) {

                }

                @Override
                public void onThinking() {

                }

                @Override
                public void onException(Throwable throwable) {

                }

                @Override
                public void onFail(String s) {

                }
            };

            return null;
        }
    }
}
