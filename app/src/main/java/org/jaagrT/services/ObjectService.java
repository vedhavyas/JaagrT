package org.jaagrT.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jaagrT.utilities.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class ObjectService extends Service {

    private static final String OBJECT_LOG_FILE = "Object_log.txt";
    private static final String HEADERS = "TIME  ------------------  STATUS";
    private static final String UPDATE_STARTED = "Update started...";
    private static final String UPDATING_OBJECTS = "Updating objects...";
    private static final String THREAD_INTERRUPTED = "Thread interrupted...";
    private static final int MILLIS = 60000;
    private static final int UPDATE_INTERVAL = 30;


    private static ParseObject userDetailsObject, userPreferenceObject;
    private boolean objectLogThreadStatus;

    public ObjectService() {
    }

    public static ParseObject getUserDetailsObject() {
        return userDetailsObject;
    }

    public static void setUserDetailsObject(ParseObject userDetailsObject) {
        ObjectService.userDetailsObject = userDetailsObject;
    }

    public static ParseObject getUserPreferenceObject() {
        return userPreferenceObject;
    }

    public static void setUserPreferenceObject(ParseObject userPreferenceObject) {
        ObjectService.userPreferenceObject = userPreferenceObject;
    }

    private static void fetchObjectsSequentially() {
        if (userDetailsObject == null) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                parseUser.getParseObject(Constants.USER_DETAILS_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject userDetailsObject, ParseException e) {
                                if (e == null) {
                                    setUserDetailsObject(userDetailsObject);
                                    fetchUserPreferenceObject();
                                }
                            }
                        });
            }
        } else {
            userDetailsObject.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject userDetailsObject, ParseException e) {
                    if (e == null) {
                        setUserDetailsObject(userDetailsObject);
                        fetchUserPreferenceObject();
                    }
                }
            });
        }
    }

    private static void fetchUserPreferenceObject() {
        if (userPreferenceObject == null) {
            if (userDetailsObject != null) {
                userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW)
                        .fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject userPreferenceObject, ParseException e) {
                                if (e == null) {
                                    setUserPreferenceObject(userPreferenceObject);
                                }
                            }
                        });
            } else {
                fetchObjectsSequentially();
            }
        } else {
            userPreferenceObject.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject userPreferenceObject, ParseException e) {
                    if (e == null) {
                        setUserPreferenceObject(userPreferenceObject);
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userDetailsObject = null;
        userPreferenceObject = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startThreadIfPossible();
        return START_STICKY;
    }

    private void writeToLog(String message) {
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
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(data);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLogData(String message) {
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

    private void startThreadIfPossible() {
        if (!objectLogThreadStatus) {
            objectLogThreadStatus = true;
            Thread objectThread = new Thread(new ObjectUpdateRunnable(UPDATE_INTERVAL));
            objectThread.start();
        }
    }

    private class ObjectUpdateRunnable implements Runnable {

        private long updateInterval;

        private ObjectUpdateRunnable(int updateInterval) {
            this.updateInterval = updateInterval * MILLIS;
        }

        @Override
        public void run() {
            //TODO need to create a prefs to control the update
            while (true) {
                synchronized (this) {
                    writeToLog(UPDATE_STARTED);
                    fetchObjectsSequentially();
                    try {
                        Thread.sleep(updateInterval);
                        writeToLog(UPDATING_OBJECTS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        writeToLog(THREAD_INTERRUPTED);
                    }
                }
            }
        }
    }
}
