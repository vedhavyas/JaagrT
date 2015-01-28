package org.jaagrT.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import org.jaagrT.listeners.BitmapGetListener;
import org.jaagrT.listeners.BitmapSaveListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Authored by vedhavyas.singareddi on 28-01-2015.
 */
public class BitmapHolder {
    private static final String THUMB = "_thumbnail.jpg";
    private static final String IMAGE = "_image.jpg";
    private static BitmapHolder bitmapHolder;
    private Context context;
    private Handler handler;

    private BitmapHolder(Context context) {
        this.context = context;
        this.handler = new Handler();
    }

    public static BitmapHolder getInstance(Context context) {
        if (bitmapHolder == null) {
            bitmapHolder = new BitmapHolder(context);
        }

        return bitmapHolder;
    }

    public void saveBitmapAsync(final String data, final Bitmap bitmap, final BitmapSaveListener listener) {
        if (listener != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean result = saveBitmap(data, bitmap);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSave(result);
                        }
                    });
                }
            }).start();
        }
    }

    public void getBitmapImageAsync(final String data, final BitmapGetListener listener) {
        if (listener != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = getBitmapImage(data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onGet(bitmap);
                        }
                    });
                }
            }).start();
        }
    }

    public void getBitmapThumbAsync(final String data, final BitmapGetListener listener) {
        if (listener != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = getBitmapThumb(data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onGet(bitmap);
                        }
                    });
                }
            }).start();
        }
    }

    public Bitmap getBitmapThumb(String data) {
        Bitmap bitmap = null;
        if (data != null && !data.isEmpty()) {
            FileInputStream fis = null;
            try {
                fis = context.openFileInput(data + THUMB);
                bitmap = BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                ErrorHandler.handleError(null, e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
            }
        }

        return bitmap;
    }

    public Bitmap getBitmapImage(String data) {
        Bitmap bitmap = null;
        if (data != null && !data.isEmpty()) {
            FileInputStream fis = null;
            try {
                fis = context.openFileInput(data + IMAGE);
                bitmap = BitmapFactory.decodeStream(fis);
            } catch (FileNotFoundException e) {
                ErrorHandler.handleError(null, e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
            }
        }

        return bitmap;
    }


    public boolean saveBitmap(String data, Bitmap bitmap) {
        boolean result = false;
        if (data != null && !data.isEmpty() && bitmap != null) {
            FileOutputStream fosImage = null;
            FileOutputStream fosThumb = null;
            checkAndDeleteIfExists(data);

            try {
                fosImage = context.openFileOutput(data + IMAGE, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);

                fosThumb = context.openFileOutput(data + THUMB, Context.MODE_PRIVATE);
                getReSizedBitmap(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fosThumb);

                result = true;
            } catch (FileNotFoundException e) {
                ErrorHandler.handleError(null, e);
            } finally {
                try {
                    if (fosImage != null) {
                        fosImage.close();
                    }

                    if (fosThumb != null) {
                        fosThumb.close();
                    }
                } catch (IOException e) {
                    ErrorHandler.handleError(null, e);
                }

            }
        }
        return result;
    }

    public boolean isBitmapExist(String data) {
        boolean result = false;
        if (data != null && !data.isEmpty()) {
            FileInputStream fis = null;
            try {
                fis = context.openFileInput(data + THUMB);
                result = true;
            } catch (FileNotFoundException e) {
                ErrorHandler.handleError(null, e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
            }
        }

        return result;
    }

    private boolean checkAndDeleteIfExists(String data) {
        boolean result = true;
        if (isBitmapExist(data)) {
            result = context.deleteFile(data + IMAGE);
            if (result) {
                result = context.deleteFile(data + THUMB);
            }
        }
        return result;
    }

    private Bitmap getReSizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxSize = 100;
        Bitmap reSizedBitmap = null;

        float bitmapRatio = (float) width / (float) height;
        try {
            if (bitmapRatio > 1) {
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

    public void deleteAllImages() {
        String[] files = context.fileList();
        for (String file : files) {
            checkAndDeleteIfExists(file);
        }
    }
}
