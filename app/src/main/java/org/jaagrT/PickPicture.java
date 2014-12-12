package org.jaagrT;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.edmodo.cropper.CropImageView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.ParseFacebookUtils;

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
    private Bitmap originalImage;
    private PopupWindow choosePicPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        activity = this;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        ParseFacebookUtils.initialize(Constants.APPLICATION_ID);

        setUIElements();
        getLocalUser();
        setChoosePickPopUp();
        getFBProfilePicture();
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
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        Button acceptBtn = (Button) findViewById(R.id.acceptBtn);
        Button rotateBtn = (Button) findViewById(R.id.rotateBtn);
        final Button choosePicBtn = (Button) findViewById(R.id.choosePicBtn);

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
                choosePicPopUp.showAsDropDown(choosePicBtn, -75, 50);
            }
        });

        cropImageView.setFixedAspectRatio(true);
    }

    private void setChoosePickPopUp() {
        LayoutInflater inflater = getLayoutInflater();
        View popView = inflater.inflate(R.layout.choose_pic_popup, null);
        choosePicPopUp = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        choosePicPopUp.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));
        choosePicPopUp.setOutsideTouchable(true);

        ImageButton cameraBtn = (ImageButton) popView.findViewById(R.id.cameraBtn);
        ImageButton galleryBtn = (ImageButton) popView.findViewById(R.id.galleryBtn);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicPopUp.dismiss();
                getImageFromCamera();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicPopUp.dismiss();
                getImageFromGallery();
            }
        });
    }

    private void getLocalUser() {
        Database db = Database.getInstance(activity);
        db.setTableName(Database.USER_TABLE);
        user = db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    private void getFBProfilePicture() {
        Session session = ParseFacebookUtils.getSession();
        if (session != null) {
            final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(this);
            pDialog.setTitleText("Downloading Picture... ");
            pDialog.show();
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
        }
    }

    private void getImageFromCamera() {
        Utilities.logIt("Choose pic from Camera!!");
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.SELECT_PICTURE);
    }


    private void getImageFromGallery() {
        Utilities.logIt("Choose pic from Gallery!!");
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
}
