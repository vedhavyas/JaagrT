package org.jaagrT;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.model.User;

import it.neokree.materialnavigationdrawer.MaterialAccount;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;


public class Main extends MaterialNavigationDrawer {

    private MaterialAccount account;
    private ObjectRetriever retriever;
    private User user;
    private Activity activity;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        account = new MaterialAccount("", "", new ColorDrawable(Color.parseColor("#9e9e9e")), getResources().getDrawable(R.drawable.ic_nav_background));
        this.addAccount(account);
        MaterialSection section = newSection("Panic", new Panic());
        this.addSection(section);
        new GetUserAsync().execute();
    }

    private class GetUserAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            retriever = ObjectRetriever.getInstance(activity);
            user = retriever.getLocalUser();
            account.setTitle(user.getFirstName());
            account.setSubTitle(user.getEmail());
            if (user.getThumbnailPicture() != null) {
                account.setPhoto(user.getThumbnailPicture());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyAccountDataChanged();
        }
    }
}
