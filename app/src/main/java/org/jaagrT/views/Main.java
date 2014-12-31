package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.jaagrT.R;
import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.model.User;

import it.neokree.materialnavigationdrawer.MaterialAccount;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;


public class Main extends MaterialNavigationDrawer<Fragment> {

    private static final String PANIC = "Panic";
    private static final String CIRCLE = "Circle";
    private static final String SETTINGS = "Settings";

    private MaterialAccount account;
    private ObjectRetriever retriever;
    private User user;
    private Activity activity;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        account = new MaterialAccount("", "", new ColorDrawable(Color.parseColor("#9e9e9e")), getResources().getDrawable(R.drawable.ic_nav_background));
        this.addAccount(account);
        MaterialSection panicSection = newSection(PANIC, this.getResources().getDrawable(R.drawable.panic_btn_small), new Panic())
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));
        MaterialSection circleSection = newSection(CIRCLE, this.getResources().getDrawable(R.drawable.ic_circle), new Circle())
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));

        MaterialSection settingsSection = newSection(SETTINGS, this.getResources().getDrawable(R.drawable.ic_settings), new Intent(getBaseContext(), Settings.class))
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));


        this.addSection(panicSection);
        this.addSection(circleSection);
        this.addBottomSection(settingsSection);
        new GetUserAsync().execute();

        allowArrowAnimation();
        addMultiPaneSupport();
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
