package org.jaagrT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.edmodo.cropper.CropImageView;

import org.jaagrT.utils.Constants;
import org.jaagrT.utils.Utilities;


public class ImageCrop extends Activity {

    private static final int ROTATE_NINETY_DEGREES = 90;
    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        setUIElements();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Bitmap originalImage = Utilities.getBitmapFromBlob(extras.getByteArray(Constants.ORIGINAL_IMAGE_ARRAY));
            if (originalImage != null) {
                cropImageView.setImageBitmap(originalImage);
            }
        }
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

                Intent data = new Intent();
                data.putExtra(Constants.CROPPED_IMAGE_ARRAY, Utilities.getBlob(cropImageView.getCroppedImage()));
                setResult(RESULT_OK, data);
                finish();
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }
}
