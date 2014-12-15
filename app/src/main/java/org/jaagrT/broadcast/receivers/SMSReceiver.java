package org.jaagrT.broadcast.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import org.jaagrT.VerifyPhone;
import org.jaagrT.utilities.Utilities;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        VerifyPhone phoneVerifyActivity = VerifyPhone.verifyPhoneActivity;

        try {

            if (bundle != null) {
                if (phoneVerifyActivity != null) {

                    final Object[] pDusObj = (Object[]) bundle.get("pdus");

                    for (Object aPDusObj : pDusObj) {

                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPDusObj);
                        String senderNumber = currentMessage.getDisplayOriginatingAddress();
                        String messageBody = currentMessage.getDisplayMessageBody();
                        phoneVerifyActivity.setVerificationCode(senderNumber, messageBody);

                    }
                }
            }
        } catch (Exception e) {
            Utilities.logIt(e.getMessage());

        }
    }
}
