package org.jaagrT.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Authored by vedhavyas on 6/12/14.
 * Project JaagrT
 */
public class User {

    private int ID;
    private String objectID;
    private String firstName;
    private String lastName;
    private int memberOfMasterCircle;
    private int phoneVerified;
    private String phoneNumber;
    private String email;
    private long currentLat;
    private long currentLong;
    private String secondaryEmails;
    private String secondaryPhones;

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

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public List<String> getSecondaryEmails() {
        List<String> emails = new ArrayList<>();
        if (secondaryEmails != null && !secondaryEmails.isEmpty()) {
            String[] dataSet = secondaryEmails.split(":");
            for (String email : dataSet) {
                if (!emails.contains(email)) {
                    emails.add(email);
                }
            }
        }

        return emails;
    }

    public void setSecondaryEmails(List<String> emails) {
        secondaryEmails = "";
        for (String email : emails) {
            if (secondaryEmails.isEmpty()) {
                secondaryEmails = email;
            } else {
                secondaryEmails = secondaryEmails + ":" + email;
            }
        }
    }

    public List<String> getSecondaryPhones() {
        List<String> phones = new ArrayList<>();
        if (secondaryPhones != null && !secondaryPhones.isEmpty()) {
            String[] dataSet = secondaryPhones.split(":");
            for (String phone : dataSet) {
                if (!phones.contains(phone)) {
                    phones.add(phone);
                }
            }
        }

        return phones;
    }

    public void setSecondaryPhones(List<String> phones) {
        secondaryPhones = "";
        for (String phone : phones) {
            if (secondaryPhones.isEmpty()) {
                secondaryPhones = phone;
            } else {
                secondaryPhones = secondaryPhones + ":" + phone;
            }
        }
    }

    public String getSecondaryEmailsRaw() {
        return secondaryEmails;
    }

    public void setSecondaryEmailsRaw(String emails) {
        this.secondaryEmails = emails;
    }

    public String getSecondaryPhonesRaw() {
        return secondaryPhones;
    }

    public void setSecondaryPhonesRaw(String phones) {
        this.secondaryPhones = phones;
    }
}
