package org.jaagrT.views;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.jaagrT.R;
import org.jaagrT.adapters.RecyclerViewAdapter;
import org.jaagrT.controller.BasicController;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.model.Database;
import org.jaagrT.model.UserContact;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PickContact extends ActionBarActivity {

    private static final String TITLE = "Pick a contact";
    private static final String PLEASE_WAIT = "Please wait...";
    private static final String FETCHING_CONTACTS = "Fetching contacts...";
    private static final String ERROR = "Error";
    private static final String ERROR_UNKNOWN = "Unknown error!!";
    private static final String OKAY = "Okay";
    private RecyclerView recList;
    private List<UserContact> fullContactList;
    private BasicController basicController;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_contact);
        activity = this;
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        basicController = BasicController.getInstance(activity);
        recList = (RecyclerView) findViewById(R.id.cardList);
        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.teal_300,
                R.color.teal_400,
                R.color.teal_500,
                R.color.teal_700,
                R.color.teal_900);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateContactList(swipeRefresh).execute();
            }
        });
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                toolbar.setTitle(TITLE);
                showContacts(fullContactList);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle("");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String data) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String data) {
                if (!data.isEmpty()) {
                    showMatchingContacts(data);
                } else {
                    showContacts(fullContactList);
                }
                return false;
            }
        });

        new GetContacts().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            returnResult(Activity.RESULT_CANCELED, -1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    @Override
    public void onBackPressed() {
        returnResult(Activity.RESULT_CANCELED, -1);
    }

    private void returnResult(int result, int contactID) {
        Intent intent = new Intent();
        if (contactID > 0) {
            intent.putExtra(Constants.CONTACT_ID, contactID);
        }
        setResult(result, intent);
        finish();
    }

    private void showContacts(final List<UserContact> contacts) {
        if (contacts != null) {
            Collections.sort(contacts, new Comparator<UserContact>() {
                @Override
                public int compare(UserContact contactInfo1, UserContact contactInfo2) {
                    return contactInfo1.getName().compareTo(contactInfo2.getName());
                }
            });
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(contacts);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    returnResult(Activity.RESULT_OK, contacts.get(position).getID());
                }
            });
            recList.setAdapter(adapter);
        }
    }

    private void showMatchingContacts(String data) {
        if (fullContactList != null) {
            List<UserContact> matchList = new ArrayList<>();
            for (UserContact contactInfo : fullContactList) {
                if (contactInfo.getName().toLowerCase().contains(data.toLowerCase())) {
                    matchList.add(contactInfo);
                }
            }
            showContacts(matchList);
        }
    }

    private List<UserContact> getContactsFromPhone() {
        List<UserContact> contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
                if (emailCursor.getCount() > 0) {
                    UserContact contact = new UserContact(id, name, getResources().getDrawable(R.drawable.ic_contact_default_pic));
                    while (emailCursor.moveToNext()) {
                        contact.addEmail(emailCursor.getString(emailCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                    }
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                    Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                    Cursor picCursor = getContentResolver().query(photoUri,
                            new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
                    if (picCursor.moveToFirst()) {
                        contact.setImage(picCursor.getBlob(0));
                    }
                    picCursor.close();

                    contacts.add(contact);

                }
                emailCursor.close();
            }
        }
        cursor.close();
        return contacts;
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {
        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText(PLEASE_WAIT);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            fullContactList = basicController.getContacts();
            if (fullContactList == null) {
                pDialog.setTitleText(FETCHING_CONTACTS);
                fullContactList = getContactsFromPhone();
                if (fullContactList != null) {
                    basicController.dropTable(Database.CONTACTS_TABLE);
                    basicController.saveContacts(fullContactList);
                    fullContactList = basicController.getContacts();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            if (fullContactList != null) {
                showContacts(fullContactList);
            } else {
                AlertDialogs.showErrorDialog(activity, ERROR, ERROR_UNKNOWN, OKAY);
            }
        }
    }

    private class UpdateContactList extends AsyncTask<Void, Void, Void> {

        private SwipeRefreshLayout swipeRefresh;

        private UpdateContactList(SwipeRefreshLayout swipeRefresh) {
            this.swipeRefresh = swipeRefresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<UserContact> updatedContacts = getContactsFromPhone();
            if (updatedContacts != null) {
                basicController.dropTable(Database.CONTACTS_TABLE);
                basicController.saveContacts(updatedContacts);
                fullContactList = basicController.getContacts();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
                if (fullContactList != null) {
                    showContacts(fullContactList);
                } else {
                    AlertDialogs.showErrorDialog(activity, ERROR, ERROR_UNKNOWN, OKAY);
                }
            }
        }
    }

}
