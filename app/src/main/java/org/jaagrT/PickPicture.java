package org.jaagrT;

import android.app.Activity;
import android.content.Intent;
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
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.listeners.ParseListener;
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
    private Bitmap originalImage, croppedImage;
    private MaterialDialog pictureDialog;
    private ParseObject userDetailsObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        activity = this;
        ParseFacebookUtils.initialize(Constants.APPLICATION_ID);
        setUpActivity();
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

    private void setUpActivity() {
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
        ObjectRetriever retriever = ObjectRetriever.getInstance(activity);
        setChoosePickPopUp();

        userDetailsObject = retriever.getUserDetailsObject(new ParseListener() {
            @Override
            public void onComplete(ParseObject parseObject) {
                userDetailsObject = parseObject;
            }
        });

        user = retriever.getLocalUser();
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

    private void startUserDetailsActivity() {
        Intent userDetailsIntent = new Intent(activity, GetUserDetails.class);
        userDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        userDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(userDetailsIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
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

            Database db = Database.getInstance(activity, Database.USER_TABLE);
            return db.updateUserData(user);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                Utilities.snackIt(activity, "Picture Saved", "Okay");
            } else {
                Utilities.snackIt(activity, "Save failed", "Okay");
            }

            startUserDetailsActivity();
        }

        SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);


    }
}
