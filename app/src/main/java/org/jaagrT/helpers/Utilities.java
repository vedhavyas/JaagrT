package org.jaagrT.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.widgets.roundDrawable.ColorGenerator;
import org.jaagrT.widgets.roundDrawable.RoundDrawable;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Authored by vedhavyas on 3/12/14.
 * Project JaagrT
 */

public class Utilities {

    private static final String OBJECT_LOG_FILE = "Object_log.txt";
    private static final String HEADERS = "TIME  ------------------  STATUS";

    public static void logData(String data, int logType) {
        if (data != null) {
            if (logType == Log.INFO) {
                Log.i(Constants.JAAGRT, data);
            } else if (logType == Log.DEBUG) {
                Log.d(Constants.JAAGRT, data);
            } else {
                Log.e(Constants.JAAGRT, data);
            }
        }
    }

    public static boolean isEditBoxesValid(MaterialEditText[] editTexts) {
        boolean allValid = true;

        for (MaterialEditText editText : editTexts) {
            allValid = editText.validate() && allValid;
        }

        return allValid;
    }

    public static void snackIt(Activity activity, String text, String actionLabel) {

        Snackbar snackbar = Snackbar.with(activity);
        snackbar.text(text)
                .textColorResource(R.color.white)
                .actionLabel(actionLabel)
                .actionColorResource(R.color.teal_400)
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .show(activity);
    }

    public static void toastIt(Context context, String data) {
        if (data != null) {
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
        }
    }

    public static byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] blob = null;
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blob = stream.toByteArray();
        }

        return blob;
    }

    public static Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

    public static Bitmap compressBitmap(Bitmap original) {
        Bitmap compressed = null;
        if (original != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 40, out);
            compressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        }

        return compressed;
    }


    public static Bitmap getReSizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxSize = 100;
        Bitmap reSizedBitmap = null;

        float bitmapRatio = (float) width / (float) height;
        try {
            if (bitmapRatio > 0) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }
            reSizedBitmap = Bitmap.createScaledBitmap(image, width, height, true);
        } catch (Exception e) {
            ErrorHandler.handleError(null, e);
        }

        return reSizedBitmap;
    }

    public static Drawable getRoundedDrawable(Context context, String data) {
        ColorGenerator colorGenerator = ColorGenerator.create(context.getResources().getIntArray(R.array.colorsList));
        int fontSize = 50;

        String finalData = "";
        String[] dataSet = data.split(" ");
        if (dataSet.length > 1) {
            for (int i = 0; i < 2; i++) {
                finalData += dataSet[i].substring(0, 1);
            }
        } else {
            finalData = data.substring(0, 1);
        }

        return RoundDrawable.builder()
                .beginConfig()
                .fontSize(fontSize)
                .toUpperCase()
                .endConfig()
                .buildRound(finalData, colorGenerator.getRandomColor());
    }

    //TODO remove all the logging methods
    public static void writeToLog(String message) {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, OBJECT_LOG_FILE);
        String data = getLogData(message);
        if (!file.exists()) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                bw.write(HEADERS);
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (IOException e) {
                ErrorHandler.handleError(null, e);
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            ErrorHandler.handleError(null, e);
        }
    }

    private static String getLogData(String message) {
        String data;
        Calendar calendar = Calendar.getInstance();
        String date = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minutes = String.valueOf(calendar.get(Calendar.MINUTE));
        String seconds = String.valueOf(calendar.get(Calendar.SECOND));
        data = date + "/" + month + "/" + year + "-" + hour + ":" + minutes + ":" + seconds + " ------ " + message;
        return data;
    }


}
