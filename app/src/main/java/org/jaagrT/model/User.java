package org.jaagrT.model;


import android.graphics.Bitmap;

import org.jaagrT.utilities.Utilities;

/**
 * Authored by vedhavyas on 6/12/14.
 * Project JaagrT
 */
public class User {

    private int ID;
    private String firstName;
    private String lastName;
    private int memberOfMasterCircle;
    private int phoneVerified;
    private String phoneNumber;
    private String email;
    private long currentLat;
    private long currentLong;
    private byte[] picture;
    private byte[] thumbnailPicture;

    public User() {
        //Empty Constructor
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isMemberOfMasterCircle() {
        return memberOfMasterCircle == 1;
    }

    public void setMemberOfMasterCircle(boolean memberOfMasterCircle) {
        this.memberOfMasterCircle = (memberOfMasterCircle) ? 1 : 0;
    }

    public int isMemberOfMasterCircleRaw() {
        return memberOfMasterCircle;
    }

    public void setMemberOfMasterCircleRaw(int memberOfMasterCircle) {
        this.memberOfMasterCircle = memberOfMasterCircle;
    }

    public boolean isPhoneVerified() {
        return phoneVerified == 1;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = (phoneVerified) ? 1 : 0;
    }

    public void setPhoneVerifiedRaw(int phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public int isPhoneVerifiedRaw() {
        return phoneVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getPicture() {
        return Utilities.getBitmapFromBlob(picture);
    }

    public void setPicture(Bitmap pictureBitmap) {
        this.picture = Utilities.getBlob(pictureBitmap);
    }

    public Bitmap getThumbnailPicture() {
        return Utilities.getBitmapFromBlob(thumbnailPicture);
    }

    public void setThumbnailPicture(Bitmap thumbnailPictureBitmap) {
        this.thumbnailPicture = Utilities.getBlob(thumbnailPictureBitmap);
    }

    public byte[] getPictureRaw() {
        return picture;
    }

    public byte[] getThumbnailPictureRaw() {
        return thumbnailPicture;
    }


}
