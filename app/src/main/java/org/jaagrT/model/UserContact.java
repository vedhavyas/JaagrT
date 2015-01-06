package org.jaagrT.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.jaagrT.utilities.Utilities;

/**
 * Authored by vedhavyas.singareddi on 23-12-2014.
 */
public class UserContact {

    private String ID;
    private String name;
    private String emails;
    private Bitmap image;
    private byte[] imageBlob;

    public UserContact() {
        //empty constructor
    }

    public UserContact(String ID, String title, Drawable drawable) {
        this.ID = ID;
        this.name = title;
        this.image = ((BitmapDrawable) drawable).getBitmap();
        this.imageBlob = Utilities.getBlob(this.image);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(byte[] data) {
        this.image = Utilities.getBitmapFromBlob(data);
        this.imageBlob = data;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String[] getEmailList() {
        return emails.split(":");
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public byte[] getImageBlob() {
        return imageBlob;
    }

    public void addEmail(String email) {
        if (this.emails == null) {
            this.emails = email;
        } else {
            if (!doesEmailExist(email)) {
                this.emails += ":" + email;
            }
        }
    }

    private boolean doesEmailExist(String data) {
        String[] emails = this.emails.split(":");
        for (String email : emails) {
            if (email.equalsIgnoreCase(data)) {
                return true;
            }
        }

        return false;
    }
}
