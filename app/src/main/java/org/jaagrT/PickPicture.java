package org.jaagrT;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edmodo.cropper.CropImageView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class PickPicture extends Activity {

    private static final int ROTATE_NINETY_DEGREES = 90;
    private CropImageView cropImageView;
    private Activity activity;
    private User user;
    private SharedPreferences prefs;
    private Bitmap originalImage, croppedImage;
    private MaterialDialog pictureDialog;
    private ParseObject userDetailsObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        activity = this;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        ParseFacebookUtils.initialize(Constants.APPLICATION_ID);

        setUIElements();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        cropImageView.setFixedAspectRatio(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                cropImageView.setImageBitmap(originalImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUIElements() {
        SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
        pDialog.setTitleText("Please wait...");
        pDialog.show();
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        Button acceptBtn = (Button) findViewById(R.id.acceptBtn);
        Button rotateBtn = (Button) findViewById(R.id.rotateBtn);
        final Button choosePicBtn = (Button) findViewById(R.id.choosePicBtn);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                new SavePicture().execute();
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
                pictureDialog.show();
            }
        });

        cropImageView.setFixedAspectRatio(true);
        setChoosePickPopUp();
        getLocalUser();
        getParseUser();
        if (user != null) {
            if (user.getPicture() != null) {
                cropImageView.setImageBitmap(user.getPicture());
                pDialog.cancel();
            } else {
                getFBProfilePicture(pDialog);
            }
        } else {
            pDialog.cancel();
        }
    }


    private void getParseUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                    .fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                userDetailsObject = parseObject;
                            } else {
                                Utilities.logIt("Failed to get object");
                                Utilities.logIt(e.getMessage());
                            }
                        }
                    });
        }
    }

    private void setChoosePickPopUp() {
        LayoutInflater inflater = getLayoutInflater();
        View popView = inflater.inflate(R.layout.choose_pic_popup, null);

        Button cameraBtn = (Button) popView.findViewById(R.id.cameraBtn);
        Button galleryBtn = (Button) popView.findViewById(R.id.galleryBtn);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureDialog.cancel();
                getImageFromCamera();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureDialog.cancel();
                getImageFromGallery();
            }
        });

        pictureDialog = new MaterialDialog.Builder(this)
                .title("Choose")
                .customView(popView)
                .build();
    }

    private void getLocalUser() {
        Database db = Database.getInstance(activity);
        db.setTableName(Database.USER_TABLE);
        user = db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    private void getFBProfilePicture(final SweetAlertDialog pDialog) {
        Session session = ParseFacebookUtils.getSession();
        if (session != null) {
            pDialog.setTitleText("Downloading Picture... ");
            Bundle params = new Bundle();
            params.putBoolean("redirect", false);
            params.putString("height", "800");
            params.putString("type", "large");
            params.putString("width", "800");
            new Request(
                    session,
                    "/me/picture",
                    params,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            try {
                                JSONObject mainObject = new JSONObject(response.getRawResponse());
                                JSONObject data = mainObject.getJSONObject("data");
                                new DownloadImage(pDialog).execute(data.getString("url"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                pDialog.cancel();
                            }
                        }
                    }
            ).executeAsync();
        } else {
            pDialog.cancel();
        }
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.SELECT_PICTURE);
    }


    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.SELECT_PICTURE);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        SweetAlertDialog pDialog;

        private DownloadImage(SweetAlertDialog pDialog) {
            this.pDialog = pDialog;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            InputStream in;
            try {
                in = new java.net.URL(url).openStream();
                originalImage = BitmapFactory.decodeStream(in);
                return originalImage;
            } catch (IOException e) {
                e.printStackTrace();
                pDialog.cancel();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            pDialog.cancel();
            if (bitmap != null) {
                cropImageView.setImageBitmap(bitmap);
            }
        }
    }

    private class SavePicture extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText("Saving...");
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Bitmap reSizedBitmap = Utilities.getReSizedBitmap(croppedImage);

            user.setPicture(croppedImage);
            user.setThumbnailPicture(Utilities.getReSizedBitmap(croppedImage));

            if (userDetailsObject != null) {
                ParseFile pictureFile = new ParseFile(Constants.USER_PICTURE_FILE_NAME, Utilities.getBlob(croppedImage));
                pictureFile.saveInBackground();
                ParseFile thumbFile = new ParseFile(Constants.USER_THUMBNAIL_PICTURE_FILE_NAME, Utilities.getBlob(reSizedBitmap));
                thumbFile.saveInBackground();
                userDetailsObject.put(Constants.USER_PROFILE_PICTURE, pictureFile);
                userDetailsObject.put(Constants.USER_THUMBNAIL_PICTURE, thumbFile);
                userDetailsObject.saveInBackground();
            }

            Database db = Database.getInstance(activity);
            db.setTableName(Database.USER_TABLE);
            return db.updateUserData(user);
        }

        SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                Utilities.snackIt(activity, "Saved to DB", "Okay");
            } else {
                Utilities.snackIt(activity, "Failed to save", "Okay");
            }
        }


    }
}
