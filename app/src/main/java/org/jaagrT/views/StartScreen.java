package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.ParseUser;

import org.jaagrT.R;

public class StartScreen extends Activity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            startMainActivity();
        }
        setContentView(R.layout.activity_start_screen);
        activity = this;
        setUpActivity();

    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private void setUpActivity() {
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(activity, Login.class);
                startActivity(loginIntent);
                overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
            }
        });


        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(activity, SignUp.class);
                startActivity(signUpIntent);
                overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
            }
        });
    }

}
