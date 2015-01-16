package org.jaagrT.views;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import org.jaagrT.R;
import org.jaagrT.adapters.CirclesAdapter;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.listeners.SwipeDismissListener;
import org.jaagrT.model.User;
import org.jaagrT.model.UserContact;
import org.jaagrT.services.ObjectService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Circles extends Fragment {

    private static final int CIRCLE_ADDED = 1;
    private static final int CIRCLE_UPDATED = 2;
    private static final String CIRCLE_ADD_SINGULAR = " circle added";
    private static final String CIRCLE_ADD_PLURAL = " circles added";
    private static final String CIRCLE_REMOVE_SINGULAR = " circle removed";
    private static final String CIRCLE_REMOVED_PLURAL = " circles removed";
    private static final String UPDATED = "Updated";
    private static final String OKAY = "Okay";
    private static final String UNDO = "Undo";
    private Activity activity;
    private BasicController basicController;
    private ParseObject userDetailsObject;
    private RecyclerView recList;
    private CirclesAdapter adapter;
    private FloatingActionButton addBtn;

    public Circles() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_circles, container, false);
        setUpActivity(rootView);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            int contactID = data.getIntExtra(Constants.CONTACT_ID, -1);
            UserContact contact = basicController.getContact(contactID);
            if (contact != null) {
                tryAndAddTheContact(contact);
            }
        }
    }

    private void setUpActivity(View rootView) {
        activity = getActivity();
        basicController = BasicController.getInstance(activity);
        userDetailsObject = ObjectService.getUserDetailsObject();
        recList = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        adapter = new CirclesAdapter(activity, new ArrayList<User>());
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //TODO show circle details
            }
        });

        SwipeDismissListener dismissListener = new SwipeDismissListener(recList, new SwipeDismissListener.SwipeListener() {
            @Override
            public boolean canSwipe(int position) {
                return true;
            }

            @Override
            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                deleteCircle(reverseSortedPositions);
            }

            @Override
            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                deleteCircle(reverseSortedPositions);
            }
        });
        recList.addOnItemTouchListener(dismissListener);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(adapter);

        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.teal_300,
                R.color.teal_400,
                R.color.teal_500,
                R.color.teal_700,
                R.color.teal_900);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetCirclesAndList(swipeRefresh, true).execute();
            }
        });

        addBtn = (FloatingActionButton) rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContactActivity();
            }
        });
        //TODO check if the fab is reacting as expected
        addBtn.attachToRecyclerView(recList);
        new GetCirclesAndList(swipeRefresh, false).execute();
    }

    private void startPickContactActivity() {
        Intent pickContactIntent = new Intent(activity, PickContact.class);
        startActivityForResult(pickContactIntent, Constants.PICK_CONTACT);
        activity.overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void tryAndAddTheContact(final UserContact contact) {
        final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
        pDialog.setTitleText(Constants.PLEASE_WAIT).show();
        ParseQuery<ParseObject> userSearchQuery = ParseQuery.getQuery(Constants.USER_DETAILS_CLASS);
        userSearchQuery.whereContainedIn(Constants.USER_PRIMARY_EMAIL, Arrays.asList(contact.getEmailList()));
        userSearchQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                pDialog.cancel();
                if (e == null) {
                    if (parseObjects.size() > 0) {
                        new SaveCircle(parseObjects, contact).execute();
                        if (userDetailsObject != null) {
                            ParseRelation<ParseObject> relation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                            for (ParseObject parseObject : parseObjects) {
                                relation.add(parseObject);
                            }
                            userDetailsObject.saveInBackground();
                        }
                    } else {
                        //TODO take user to invite page
                    }
                } else {
                    ErrorHandler.handleError(activity, e);
                }

            }
        });
    }

    private void updateCircleList(List<User> circles) {
        if (circles != null) {
            Collections.sort(circles, new Comparator<User>() {
                @Override
                public int compare(User circle1, User circle2) {
                    return circle1.getFirstName().compareTo(circle2.getFirstName());
                }
            });
            adapter.setCircles(circles);
        }
    }

    private void deleteCircle(int[] reverseSortedPositions) {
        User circle;
        List<User> removedCircles = new ArrayList<>();

        for (int position : reverseSortedPositions) {
            circle = adapter.getCircle(position);
            adapter.removeCircle(position);
            removedCircles.add(circle);
        }

        showUndoSnack(removedCircles);
    }

    private void showUndoSnack(final List<User> removedCircles) {

        String text;
        if (removedCircles.size() > 1) {
            text = removedCircles.size() + CIRCLE_REMOVED_PLURAL;
        } else {
            text = removedCircles.size() + CIRCLE_REMOVE_SINGULAR;
        }

        SnackbarManager.show(
                Snackbar.with(activity)
                        .text(text)
                        .textColor(getResources().getColor(R.color.white))
                        .actionLabel(UNDO)
                        .actionColor(getResources().getColor(R.color.teal_400))
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                //TODO try and move the button instead
                                addBtn.setVisibility(View.GONE);
                            }

                            @Override
                            public void onShown(Snackbar snackbar) {
                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {
                                addBtn.setVisibility(View.VISIBLE);
                                basicController.deleteCircles(removedCircles);
                            }
                        })
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                for (User circle : removedCircles) {
                                    adapter.addCircle(circle);
                                }
                                removedCircles.clear();
                            }
                        }));
    }

    private void showSnack(int which, int number) {
        String text;
        if (which == CIRCLE_ADDED) {
            if (number > 1) {
                text = number + CIRCLE_ADD_PLURAL;
            } else {
                text = number + CIRCLE_ADD_SINGULAR;
            }
        } else {
            text = UPDATED;
        }


        SnackbarManager.show(
                Snackbar.with(activity)
                        .text(text)
                        .textColor(getResources().getColor(R.color.white))
                        .actionLabel(OKAY)
                        .actionColor(getResources().getColor(R.color.teal_400))
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                //TODO try and move the button instead
                                addBtn.setVisibility(View.GONE);
                            }

                            @Override
                            public void onShown(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {
                                addBtn.setVisibility(View.VISIBLE);
                            }
                        })
                        .attachToRecyclerView(recList));
    }

    private class SaveCircle extends AsyncTask<Void, Void, List<User>> {

        private List<ParseObject> circleObjects;
        private UserContact contact;

        private SaveCircle(List<ParseObject> circleObjects, UserContact contact) {
            this.circleObjects = circleObjects;
            this.contact = contact;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            basicController.saveCircles(circleObjects, contact);
            return basicController.getCircles();
        }

        @Override
        protected void onPostExecute(List<User> circles) {
            super.onPostExecute(circles);
            updateCircleList(circles);
            showSnack(CIRCLE_ADDED, circleObjects.size());
        }
    }

    private class GetCirclesAndList extends AsyncTask<Void, Void, List<User>> {

        SwipeRefreshLayout swipeRefresh;
        boolean updateMode;

        private GetCirclesAndList(SwipeRefreshLayout swipeRefresh, boolean updateMode) {
            this.swipeRefresh = swipeRefresh;
            this.updateMode = updateMode;
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            return basicController.getCircles();
        }

        @Override
        protected void onPostExecute(List<User> circles) {
            super.onPostExecute(circles);
            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
            updateCircleList(circles);
            if (updateMode && circles != null) {
                showSnack(CIRCLE_UPDATED, circles.size());
            }
        }
    }

}
