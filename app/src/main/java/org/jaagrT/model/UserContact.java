package org.jaagrT.model;

import android.graphics.Bitmap;

import org.jaagrT.helpers.Utilities;

/**
 * Authored by vedhavyas.singareddi on 23-12-2014.
 */
public class UserContact {

    private int ID;
    private String contactID;
    private String name;
    private String emails;
    private byte[] thumbnailPicture;

    public UserContact() {
        //empty constructor
    }

    public UserContact(String contactID, String title) {
        this.contactID = contactID;
        this.name = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getThumbnailPicture() {
        return Utilities.getBitmapFromBlob(thumbnailPicture);
    }

    public void setProfilePic(byte[] data) {
        this.thumbnailPicture = data;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
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

    public byte[] getThumbnailPictureRaw() {
        return thumbnailPicture;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
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
