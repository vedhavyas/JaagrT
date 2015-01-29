package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;


public class Main extends MaterialNavigationDrawer<Fragment> {

    private static final String PANIC = "Panic";
    private static final String CIRCLES = "My Circles";
    private static final String SETTINGS = "Settings";

    private MaterialAccount account;
    private Activity activity;
    private MaterialSection panicSection, circleSection;
    private BasicController basicController;
    private Handler handler;
    private BitmapHolder bitmapHolder;

    @Override
    public void init(Bundle bundle) {
        activity = this;
        handler = new Handler();
        basicController = BasicController.getInstance(activity);
        bitmapHolder = BitmapHolder.getInstance(activity);
        account = new MaterialAccount(this.getResources(), "", "", R.drawable.ic_user, R.drawable.ic_nav_background);
        this.addAccount(account);
        this.setAccountListener(new MaterialAccountListener() {
            @Override
            public void onAccountOpening(MaterialAccount materialAccount) {
                Intent profileIntent = new Intent(activity, Profile.class);
                startActivity(profileIntent);
                overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
            }

            @Override
            public void onChangeAccount(MaterialAccount materialAccount) {

            }
        });
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
            Bitmap bitmap = bitmapHolder.getBitmapThumb(localUser.getEmail());
            if (bitmap != null) {
                account.setPhoto(bitmap);
            }
            final int circleNotifications = basicController.getEntryCount(Database.CIRCLES_TABLE);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyAccountDataChanged();
                    circleSection.setNotifications(circleNotifications);
                }
            });
        }
    }

}
