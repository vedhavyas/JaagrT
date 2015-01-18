package org.jaagrT.model;

/**
 * Authored by vedhavyas.singareddi on 23-12-2014.
 */
public class Contact {

    private int ID;
    private String contactID;
    private String name;
    private String emails;

    public Contact() {
        //empty constructor
    }

    public Contact(String contactID, String title) {
        this.contactID = contactID;
        this.name = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
