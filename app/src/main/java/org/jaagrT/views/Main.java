package org.jaagrT.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.model.User;

import it.neokree.materialnavigationdrawer.MaterialAccount;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;


public class Main extends MaterialNavigationDrawer<Fragment> {

    private static final String PANIC = "Panic";
    private static final String CIRCLES = "Circles";
    private static final String SETTINGS = "Settings";
    private static final String TRANSPARENT_COLOR = "#9e9e9e";

    private MaterialAccount account;
    private Activity activity;
    private MaterialSection panicSection, circleSection;
    private BasicController basicController;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        basicController = BasicController.getInstance(activity);
        account = new MaterialAccount("", "", new ColorDrawable(Color.parseColor(TRANSPARENT_COLOR)), getResources().getDrawable(R.drawable.ic_nav_background));
        this.addAccount(account);
        panicSection = this.newSection(PANIC, this.getResources().getDrawable(R.drawable.panic_btn_small), new Panic())
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));
        circleSection = this.newSection(CIRCLES, this.getResources().getDrawable(R.drawable.ic_circles), new Circles())
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));

        MaterialSection settingsSection = this.newSection(SETTINGS, this.getResources().getDrawable(R.drawable.ic_settings), new Settings())
                .setSectionColor(this.getResources().getColor(R.color.teal_500), this.getResources().getColor(R.color.teal_700));


        this.addSection(panicSection);
        this.addSection(circleSection);
        this.addBottomSection(settingsSection);
        allowArrowAnimation();
        addMultiPaneSupport();

        new GetUserAsync().execute();
    }

    @Override
    protected MaterialSection backToSection(MaterialSection currentSection) {
        return super.backToSection(panicSection);
    }

    private class GetUserAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            BasicController basicController = BasicController.getInstance(activity);
            User localUser = basicController.getLocalUser();
            account.setTitle(localUser.getFirstName());
            account.setSubTitle(localUser.getEmail());
            if (localUser.getThumbnailPicture() != null) {
                account.setPhoto(localUser.getThumbnailPicture());
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
