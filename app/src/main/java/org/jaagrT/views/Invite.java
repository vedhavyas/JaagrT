package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.Contact;
import org.jaagrT.model.User;

import java.util.ArrayList;
import java.util.List;

public class Invite extends ActionBarActivity {

    private static final String TITLE = "Invite";
    private static final String INVITE_SENT = "Invitation Sent";
    private Activity activity;
    private List<MaterialEditText> emailBoxes = new ArrayList<>();
    private BasicController basicController;
    private LinearLayout emailFieldLayout;
    private boolean sentInvite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        setUpActivity();
    }

    @Override
    public void onBackPressed() {
        startMainActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_CONTACT && resultCode == RESULT_OK) {
            int contactID = data.getIntExtra(Constants.CONTACT_ID, -1);
            addContact(contactID);
        } else if (requestCode == Constants.SEND_INVITE) {
            if (sentInvite) {
                new Thread(new SaveOnCloud()).start();
            }
            startMainActivity();
        }
    }

    private void setUpActivity() {
        activity = this;
        basicController = BasicController.getInstance(activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton addFieldBtn = (ImageButton) findViewById(R.id.addEmailFieldBtn);
        addFieldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewField(null);
            }
        });

        emailFieldLayout = (LinearLayout) findViewById(R.id.emailFieldLayout);
        Button pickContactBtn = (Button) findViewById(R.id.pickContactBtn);
        pickContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(activity, PickContact.class);
                startActivityForResult(pickContactIntent, Constants.PICK_CONTACT);
                activity.overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
            }
        });

        Button inviteBtn = (Button) findViewById(R.id.inviteBtn);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invite();
            }
        });

        int contactId = getIntent().getIntExtra(Constants.CONTACT_ID, -1);
        addContact(contactId);
    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(activity, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void addContact(int contactID) {
        Contact contact = basicController.getContact(contactID);
        if (contact != null) {
            String[] emails = contact.getEmailList();
            for (String email : emails) {
                addNewField(email);
            }
        }
    }

    private void addNewField(String data) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final ImageButton deleteField;
        final MaterialEditText editText;
        View view;

        view = inflater.inflate(R.layout.secondary_email_field, null);
        deleteField = (ImageButton) view.findViewById(R.id.deleteField);
        editText = (MaterialEditText) view.findViewById(R.id.emailBox);
        editText.addValidator(new FormValidators.EmptyFieldValidator())
                .addValidator(new FormValidators.EmailValidator());
        emailBoxes.add(editText);
        emailFieldLayout.addView(view, emailFieldLayout.getChildCount());


        if (data != null) {
            editText.setText(data);
        }

        deleteField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailBoxes.remove(editText);
                emailFieldLayout.removeView((View) v.getParent());
            }
        });
    }

    private String[] getEmailsFromBoxes() {
        String[] emails = new String[emailBoxes.size()];

        for (int i = 0; i < emailBoxes.size(); i++) {
            emails[i] = emailBoxes.get(i).getText().toString();
        }

        return emails;
    }

    private void invite() {
        if (emailBoxes.size() > 0) {
            if (Utilities.isEditBoxesValid(emailBoxes)) {
                Intent emailActivity = new Intent(Intent.ACTION_SEND);
                emailActivity.putExtra(Intent.EXTRA_EMAIL, getEmailsFromBoxes());
                emailActivity.putExtra(Intent.EXTRA_SUBJECT, Constants.JAAGRT);
                emailActivity.putExtra(Intent.EXTRA_TEXT, Constants.EMAIL_BODY);
                emailActivity.setType("message/rfc822");
                startActivityForResult(Intent.createChooser(emailActivity, "Select an Email provider"), Constants.SEND_INVITE);
                sentInvite = true;
            }
        } else {
            Utilities.snackIt(activity, "Add atleast One Email", "Okay");
        }
    }


    private class SaveOnCloud implements Runnable {

        @Override
        public void run() {
            //TODO do an offline save while segmenting the user images in service
            String[] emails = getEmailsFromBoxes();
            final User user = basicController.getUser();
            for (final String email : emails) {
                ParseQuery<ParseObject> emailSearchQuery = ParseQuery.getQuery(Constants.INVITATION_CLASS);
                emailSearchQuery.whereEqualTo(Constants.EMAIL, email);
                emailSearchQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            parseObject.addUnique(Constants.INVITE_SENT_BY, user.getEmail());
                            parseObject.saveEventually();
                        } else {
                            ErrorHandler.handleError(null, e);
                            ParseObject newInviteObject = new ParseObject(Constants.INVITATION_CLASS);
                            ParseACL inviteAcl = new ParseACL();
                            inviteAcl.setPublicReadAccess(true);
                            inviteAcl.setPublicWriteAccess(true);
                            newInviteObject.setACL(inviteAcl);
                            newInviteObject.put(Constants.EMAIL, email);
                            newInviteObject.addUnique(Constants.INVITE_SENT_BY, user.getEmail());
                            newInviteObject.put(Constants.INVITE_STATUS, INVITE_SENT);
                            newInviteObject.saveEventually();
                        }
                    }
                });
            }
        }
    }

}
