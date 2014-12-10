package org.jaagrT;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.edmodo.cropper.CropImageView;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;

import java.io.IOException;
import java.io.InputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class PickPicture extends Activity {

    private static final int ROTATE_NINETY_DEGREES = 90;
    private CropImageView cropImageView;
    private Activity activity;
    private User user;
    private SharedPreferences prefs;
    private String pictureUrl;
    private Bitmap originalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        activity = this;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        setUIElements();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pictureUrl = extras.getString(Constants.PICTURE_URL_STRING);
        }

        new GetUserAndPicture().execute();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        cropImageView.setFixedAspectRatio(true);
    }

    private void setUIElements() {
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        Button acceptBtn = (Button) findViewById(R.id.acceptBtn);
        Button rotateBtn = (Button) findViewById(R.id.rotateBtn);
        Button choosePicBtn = (Button) findViewById(R.id.choosePicBtn);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cropImageView.rotateImage(ROTATE_NINETY_DEGREES);

            }
        });

        choosePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cropImageView.setFixedAspectRatio(true);
    }

    private class GetUserAndPicture extends AsyncTask<Void, Void, Void> {
        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText("Please Wait...");
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Database db = Database.getInstance(activity);
            db.setTableName(Database.USER_TABLE);
            user = db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
            if (pictureUrl != null) {
                InputStream in;
                try {
                    in = new java.net.URL(pictureUrl).openStream();
                    originalImage = BitmapFactory.decodeStream(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (originalImage != null) {
                cropImageView.setImageBitmap(originalImage);
            }

            pDialog.cancel();
        }
    }
}
