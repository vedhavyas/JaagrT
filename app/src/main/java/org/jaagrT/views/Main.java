package org.jaagrT.views;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
    private Handler handler;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        handler = new Handler();
        basicController = BasicController.getInstance(activity);
        account = new MaterialAccount("", "", new ColorDrawable(Color.parseColor(TRANSPARENT_COLOR)), getResources().getDrawable(R.drawable.ic_nav_background));
        this.addAccount(account);
        new Thread(new GetUserAsync()).start();

        panicSection = this.newSection(PANIC, this.getResources().getDrawable(R.drawable.ic_panic_small), new Panic())
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
    }


    private class GetUserAsync implements Runnable {

        @Override
        public void run() {
            User localUser = basicController.getLocalUser();
            account.setTitle(localUser.getFirstName());
            account.setSubTitle(localUser.getEmail());
            if (localUser.getThumbnailPicture() != null) {
                account.setPhoto(localUser.getThumbnailPicture());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyAccountDataChanged();
                }
            });
        }
    }
}
