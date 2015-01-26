package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class PickPicture extends Activity {

    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final String CAMERA = "From Camera";
    private static final String GALLERY = "From Gallery";
    private static final String SELECT_PICTURE = "Select Picture";
    private static final String IMAGE = "image/*";
    private static final String FETCHING_PICTURE = "Fetching Picture... ";
    private CropImageView cropImageView;
    private Activity activity;
    private User localUser;
    private Bitmap originalImage, croppedImage;
    private BasicController basicController;
    private ParseObject userDetailsObject;
    private Button acceptBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_picture);
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
        if (requestCode == Constants.GET_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                cropImageView.setImageBitmap(originalImage);
                acceptBtn.setEnabled(true);
            } catch (IOException e) {
                ErrorHandler.handleError(activity, e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        returnResult(Activity.RESULT_CANCELED);
    }

    private void setUpActivity() {
        SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
        pDialog.setTitleText(Constants.PLEASE_WAIT);
        pDialog.show();
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        acceptBtn = (Button) findViewById(R.id.acceptBtn);
        Button rotateBtn = (Button) findViewById(R.id.rotateBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        final Button moreBtn = (Button) findViewById(R.id.moreBtn);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                new SavePicture().execute();
            }
        });
        acceptBtn.setEnabled(false);

        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cropImageView.rotateImage(ROTATE_NINETY_DEGREES);

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult(Activity.RESULT_CANCELED);
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePictureDialog();
            }
        });

        cropImageView.setFixedAspectRatio(true);

        basicController = BasicController.getInstance(activity);
        userDetailsObject = ObjectService.getUserDetailsObject();

        localUser = basicController.getLocalUser();
        if (localUser != null) {
            if (localUser.getThumbnailPicture() != null) {
                cropImageView.setImageBitmap(basicController.getUserPicture());
                pDialog.cancel();
            } else {
                getFBProfilePicture(pDialog);
            }
        } else {
            pDialog.cancel();
        }
    }


    private void getFBProfilePicture(final SweetAlertDialog pDialog) {
        Session session = ParseFacebookUtils.getSession();
        if (session != null) {
            pDialog.setTitleText(FETCHING_PICTURE);
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
                                ErrorHandler.handleError(activity, e);
                                pDialog.cancel();
                            }
                        }
                    }
            ).executeAsync();
        } else {
            pDialog.cancel();
        }
    }

    private void showChoosePictureDialog() {
        new MaterialDialog.Builder(this)
                .items(new String[]{CAMERA, GALLERY})
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        if (charSequence.toString().equalsIgnoreCase(CAMERA)) {
                            getImageFromCamera();
                        } else {
                            getImageFromGallery();
                        }
                    }
                })
                .show();
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.GET_IMAGE);
    }


    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType(IMAGE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), Constants.GET_IMAGE);
    }

    private void returnResult(int result) {
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
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

        SweetAlertDialog pDialog;

        private SavePicture() {
            pDialog = AlertDialogs.showSweetProgress(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText(Constants.SAVING);
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Bitmap reSizedBitmap = Utilities.getReSizedBitmap(croppedImage);

            localUser.setPicture(croppedImage);
            localUser.setThumbnailPicture(Utilities.getReSizedBitmap(croppedImage));

            if (userDetailsObject != null) {
                ParseFile pictureFile = new ParseFile(Constants.USER_PICTURE_FILE_NAME, Utilities.getBlob(croppedImage));
                pictureFile.saveInBackground();
                ParseFile thumbFile = new ParseFile(Constants.USER_THUMBNAIL_PICTURE_FILE_NAME, Utilities.getBlob(reSizedBitmap));
                thumbFile.saveInBackground();
                userDetailsObject.put(Constants.USER_PROFILE_PICTURE, pictureFile);
                userDetailsObject.put(Constants.USER_THUMBNAIL_PICTURE, thumbFile);
                userDetailsObject.saveInBackground();
            }
            return basicController.updateUser(localUser);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                returnResult(RESULT_OK);
            } else {
                returnResult(RESULT_CANCELED);
            }
        }

    }
}
