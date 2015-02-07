package org.jaagrT.helpers;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.jaagrT.controller.BasicController;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored by vedhavyas on 7/2/15.
 * Project JaagrT
 */
public class ObjectFetcher {

    private static final String INVITE_SENT = "Invitation Sent";

    public static void getCirclesFirstTime() {
        Utilities.writeToFile("getting circles for the first time...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
                BasicController basicController = ObjectService.getBasicController();
                if (userDetailsObject != null) {
                    ParseRelation<ParseObject> circleRelation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                    try {
                        List<ParseObject> userCircles = circleRelation.getQuery().find();
                        basicController.updateCirclesThroughObjects(userCircles);
                        ObjectService.setUserCircles(userCircles);
                    } catch (ParseException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
            }
        }).start();

    }

    public static void fetchAndUpdateUserImages() {
        try {
            BasicController basicController = ObjectService.getBasicController();
            User localUser = basicController.getUser();
            BitmapHolder bitmapHolder = ObjectService.getBitmapHolder();
            ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
            Bitmap image = bitmapHolder.getBitmapImage(localUser.getEmail());
            if (image != null) {
                ParseFile pictureFile = new ParseFile(Constants.USER_PICTURE_FILE_NAME, Utilities.getBlob(image));
                pictureFile.save();
                userDetailsObject.put(Constants.USER_PROFILE_PICTURE, pictureFile);
            }

            image = bitmapHolder.getBitmapThumb(localUser.getEmail());
            if (image != null) {
                ParseFile thumbFile = new ParseFile(Constants.USER_THUMBNAIL_PICTURE_FILE_NAME, Utilities.getBlob(image));
                thumbFile.save();
                userDetailsObject.put(Constants.USER_THUMBNAIL_PICTURE, thumbFile);
            }
        } catch (ParseException e) {
            ErrorHandler.handleError(null, e);
        } catch (Exception e) {
            ErrorHandler.handleError(null, e);
        }
    }

    public static void getUserMiscDetails() {
        Utilities.writeToFile("Fetching misc details...");
        new Thread(new Runnable() {
            @Override
            public void run() {

                BitmapHolder bitmapHolder = ObjectService.getBitmapHolder();
                ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
                BasicController basicController = ObjectService.getBasicController();
                if (userDetailsObject != null) {
                    byte[] picture = null, thumbnail = null;
                    User user = basicController.getUser();

                    List<String> secondaryEmails = userDetailsObject.getList(Constants.USER_SECONDARY_EMAILS);
                    List<String> secondaryPhones = userDetailsObject.getList(Constants.USER_SECONDARY_PHONES);


                    if (secondaryEmails != null) {
                        user.setSecondaryEmails(secondaryEmails);
                    }
                    if (secondaryPhones != null) {
                        user.setSecondaryPhones(secondaryPhones);
                    }
                    basicController.updateUser(user);

                    if (userDetailsObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                        try {
                            thumbnail = userDetailsObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE).getData();
                        } catch (ParseException e) {
                            ErrorHandler.handleError(null, e);
                        }
                    }
                    bitmapHolder.saveBitmapThumbAsync(user.getEmail(), Utilities.getBitmapFromBlob(thumbnail));

                    if (userDetailsObject.getParseFile(Constants.USER_PROFILE_PICTURE) != null) {
                        try {
                            picture = userDetailsObject.getParseFile(Constants.USER_PROFILE_PICTURE).getData();
                        } catch (ParseException e) {
                            ErrorHandler.handleError(null, e);
                        }
                    }

                    bitmapHolder.saveBitmapImageAsync(user.getEmail(), Utilities.getBitmapFromBlob(picture));
                    Utilities.writeToFile("Updated misc details ...");
                }
            }
        }).start();
    }

    public static void fetchAndUpdateAllCircleImages() {
        try {
            BasicController basicController = ObjectService.getBasicController();
            List<String> objectIds = basicController.getCircleObjectIDs();
            for (String objectId : objectIds) {
                fetchAndUpdateCircleImage(objectId);
            }
        } catch (Exception e) {
            ErrorHandler.handleError(null, e);
        }
    }

    public static void fetchAndUpdateCircleImage(String objectID) {
        try {
            BitmapHolder bitmapHolder = ObjectService.getBitmapHolder();
            ParseQuery<ParseObject> circleQuery = ParseQuery.getQuery(Constants.USER_DETAILS_CLASS);
            ParseObject circle = circleQuery.get(objectID);
            if (circle.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                Bitmap thumb = Utilities.getBitmapFromBlob(circle.getParseFile(Constants.USER_THUMBNAIL_PICTURE).getData());
                bitmapHolder.saveBitmapThumb(circle.getString(Constants.USER_PRIMARY_EMAIL), thumb);
            }

            if (circle.getParseFile(Constants.USER_PROFILE_PICTURE) != null) {
                Bitmap picture = Utilities.getBitmapFromBlob(circle.getParseFile(Constants.USER_PROFILE_PICTURE).getData());
                bitmapHolder.saveBitmapImage(circle.getString(Constants.USER_PRIMARY_EMAIL), picture);
            }
        } catch (ParseException e) {
            ErrorHandler.handleError(null, e);
        } catch (Exception e) {
            ErrorHandler.handleError(null, e);
        }
    }

    public static void fetchAndUpdateUserCircles() {
        ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
        BasicController basicController = ObjectService.getBasicController();
        if (userDetailsObject != null) {
            ParseRelation<ParseObject> circleRelation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
            try {
                List<ParseObject> userCircles = circleRelation.getQuery().find();
                List<String> objectIDs = basicController.getCircleObjectIDs();
                List<User> updatedCircles = new ArrayList<>();
                if (objectIDs != null) {
                    for (ParseObject parseObject : userCircles) {
                        if (objectIDs.contains(parseObject.getObjectId())) {
                            User circle = new User();
                            circle.setObjectID(parseObject.getObjectId());
                            if (parseObject.getString(Constants.USER_FIRST_NAME) == null) {
                                String[] emailSet = parseObject.getString(Constants.USER_PRIMARY_EMAIL).split("@");
                                circle.setFirstName(emailSet[0]);
                            } else {
                                circle.setFirstName(parseObject.getString(Constants.USER_FIRST_NAME));
                            }
                            circle.setLastName(parseObject.getString(Constants.USER_LAST_NAME));
                            circle.setPhoneNumber(parseObject.getString(Constants.USER_PRIMARY_PHONE));
                            circle.setPhoneVerified(parseObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                            circle.setMemberOfMasterCircle(parseObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                            circle.setEmail(parseObject.getString(Constants.USER_PRIMARY_EMAIL));
                            updatedCircles.add(circle);
                        } else {
                            circleRelation.remove(parseObject);
                        }
                    }

                    basicController.updateCircles(updatedCircles);
                } else {
                    for (ParseObject object : userCircles) {
                        circleRelation.remove(object);
                    }
                }
                userDetailsObject.saveEventually();
                ObjectService.setUserCircles(userCircles);
                Utilities.writeToFile("Updated circles...");

            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            } catch (Exception e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    public static void fetchAndUpdateUserPreferenceObject() {
        ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
        ParseObject userPreferenceObject;
        BasicController basicController = ObjectService.getBasicController();
        if (userDetailsObject != null) {
            try {
                userPreferenceObject = userDetailsObject.getParseObject(Constants.USER_COMMUNICATION_PREFERENCE_ROW).fetch();
                SharedPreferences prefs = basicController.getPrefs();
                userPreferenceObject.put(Constants.SEND_SMS, prefs.getBoolean(Constants.SEND_SMS, true));
                userPreferenceObject.put(Constants.SEND_EMAIL, prefs.getBoolean(Constants.SEND_EMAIL, true));
                userPreferenceObject.put(Constants.SEND_PUSH, prefs.getBoolean(Constants.SEND_PUSH, true));
                userPreferenceObject.put(Constants.SHOW_POP_UPS, prefs.getBoolean(Constants.SHOW_POP_UPS, true));
                userPreferenceObject.put(Constants.RECEIVE_SMS, prefs.getBoolean(Constants.RECEIVE_SMS, true));
                userPreferenceObject.put(Constants.RECEIVE_PUSH, prefs.getBoolean(Constants.RECEIVE_PUSH, true));
                userPreferenceObject.put(Constants.RECEIVE_EMAIL, prefs.getBoolean(Constants.RECEIVE_EMAIL, true));
                userPreferenceObject.put(Constants.NOTIFY_WITH_IN, prefs.getInt(Constants.NOTIFY_WITH_IN, Constants.DEFAULT_DISTANCE));
                userPreferenceObject.put(Constants.RESPOND_ALERT_WITH_IN, prefs.getInt(Constants.RESPOND_ALERT_WITH_IN, Constants.DEFAULT_DISTANCE));
                userPreferenceObject.put(Constants.ALERT_MESSAGE, prefs.getString(Constants.ALERT_MESSAGE, Constants.DEFAULT_ALERT_MESSAGE));
                userPreferenceObject.saveEventually();
                ObjectService.setUserPreferenceObject(userPreferenceObject);
                Utilities.writeToFile("Updated preferences...");
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            } catch (Exception e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    public static void fetchAndUpdateUserDetailsObject() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseObject userDetailsObject;
        BasicController basicController = ObjectService.getBasicController();
        if (parseUser != null) {
            try {
                userDetailsObject = parseUser.getParseObject(Constants.USER_DETAILS_ROW).fetch();
                User localUser = basicController.getUser();
                if (localUser.getFirstName() != null) {
                    userDetailsObject.put(Constants.USER_FIRST_NAME, localUser.getFirstName());
                }
                if (localUser.getLastName() != null) {
                    userDetailsObject.put(Constants.USER_LAST_NAME, localUser.getLastName());
                }
                if (localUser.getPhoneNumber() != null) {
                    userDetailsObject.put(Constants.USER_PRIMARY_PHONE, localUser.getPhoneNumber());
                }

                if (localUser.getSecondaryEmailsRaw() != null) {
                    userDetailsObject.remove(Constants.USER_SECONDARY_EMAILS);
                    userDetailsObject.addAll(Constants.USER_SECONDARY_EMAILS, localUser.getSecondaryEmails());
                }

                if (localUser.getSecondaryPhonesRaw() != null) {
                    userDetailsObject.remove(Constants.USER_SECONDARY_PHONES);
                    userDetailsObject.addAll(Constants.USER_SECONDARY_PHONES, localUser.getSecondaryPhones());
                }
                localUser.setMemberOfMasterCircle(userDetailsObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE_VERIFIED, localUser.isPhoneVerified());
                userDetailsObject.saveEventually();
                basicController.updateUser(localUser);
                ObjectService.setUserDetailsObject(userDetailsObject);
                Utilities.writeToFile("Updated user details...");
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
            } catch (Exception e) {
                ErrorHandler.handleError(null, e);
            }
        }
    }

    public static void updateInvitations(String[] invitations) {
        BasicController basicController = ObjectService.getBasicController();
        final User user = basicController.getUser();
        ParseObject parseObject;
        for (final String invitation : invitations) {
            ParseQuery<ParseObject> emailSearchQuery = ParseQuery.getQuery(Constants.INVITATION_CLASS);
            emailSearchQuery.whereEqualTo(Constants.EMAIL, invitation);
            try {
                parseObject = emailSearchQuery.getFirst();
                parseObject.addUnique(Constants.INVITE_SENT_BY, user.getEmail());
                parseObject.save();
            } catch (ParseException e) {
                ErrorHandler.handleError(null, e);
                ParseObject newInviteObject = new ParseObject(Constants.INVITATION_CLASS);
                ParseACL inviteAcl = new ParseACL();
                inviteAcl.setPublicReadAccess(true);
                inviteAcl.setPublicWriteAccess(true);
                newInviteObject.setACL(inviteAcl);
                newInviteObject.put(Constants.EMAIL, invitation);
                newInviteObject.addUnique(Constants.INVITE_SENT_BY, user.getEmail());
                newInviteObject.put(Constants.INVITE_STATUS, INVITE_SENT);
                try {
                    newInviteObject.save();
                } catch (ParseException e1) {
                    ErrorHandler.handleError(null, e1);
                }
            }
        }
    }
}
