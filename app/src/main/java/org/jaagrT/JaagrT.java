package org.jaagrT;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

import org.jaagrT.utils.Utilities;

/**
 * Authored by vedhavyas on 1/12/14.
 * Project JaagrT
 */


public class JaagrT extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        {
            Utilities.logIt("Initiating parse");
            Parse.initialize(this, "XRlRRJWTJL3czveTq3WLf5BCqyvo0gSwee4SKLFO", "gF7bTRj305xcbT1PdwQeZZ1rOFnCaZASWA4VkEkn");
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

    }
}
