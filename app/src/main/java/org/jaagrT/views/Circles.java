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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;

import org.jaagrT.R;
import org.jaagrT.adapters.CirclesAdapter;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.OnItemClickListener;
import org.jaagrT.listeners.SwipeDismissListener;
import org.jaagrT.model.User;
import org.jaagrT.model.UserContact;
import org.jaagrT.services.ObjectService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Circles extends Fragment {

    private static final String UPDATING_CIRCLES = "Updating Circles...";
    private static final String ARE_YOU_SURE = "Are you sure?";
    private static final String DELETE = "Yes,delete Circle!";
    private static final String NO = "No";

    private Activity activity;
    private BasicController basicController;
    private ParseObject userDetailsObject;

    private RecyclerView recList;


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
        final SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.teal_300,
                R.color.teal_400,
                R.color.teal_500,
                R.color.teal_700,
                R.color.teal_900);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateCircles(swipeRefresh).execute();
            }
        });
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        FloatingActionButton addBtn = (FloatingActionButton) rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContactActivity();
            }
        });
        //TODO check if the fab is reacting as expected
        addBtn.attachToRecyclerView(recList);
        new GetCircles().execute();
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
                if (e == null) {
                    if (parseObjects.size() > 0) {
                        if (userDetailsObject != null) {
                            ParseRelation<ParseObject> relation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                            for (ParseObject parseObject : parseObjects) {
                                relation.add(parseObject);
                            }
                            List<ParseObject> userCircles = ObjectService.getUserCircles();
                            if (userCircles != null) {
                                userCircles.addAll(parseObjects);
                            }

                            userDetailsObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        new SaveCircle(pDialog, parseObjects, contact).execute();
                                    } else {
                                        pDialog.cancel();
                                        ErrorHandler.handleError(activity, e);
                                    }
                                }
                            });
                        } else {
                            ObjectService.startHandlerJob();
                        }
                    } else {
                        //TODO take user to invite page
                        pDialog.cancel();
                    }
                } else {
                    pDialog.cancel();
                    ErrorHandler.handleError(activity, e);
                }

            }
        });
    }

    private void showCircles(final List<User> circles) {
        if (circles != null) {
            Collections.sort(circles, new Comparator<User>() {
                @Override
                public int compare(User circle1, User circle2) {
                    return circle1.getFirstName().compareTo(circle2.getFirstName());
                }
            });
            final CirclesAdapter adapter = new CirclesAdapter(activity, circles);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    Utilities.snackIt(activity, circles.get(position).getFirstName(), "Okay");
                }
            });
            recList.setAdapter(adapter);

            SwipeDismissListener dismissListener = new SwipeDismissListener(recList, new SwipeDismissListener.SwipeListener() {
                @Override
                public boolean canSwipe(int position) {
                    return true;
                }

                @Override
                public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                    for (int position : reverseSortedPositions) {
                        circles.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                    for (int position : reverseSortedPositions) {
                        circles.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                    adapter.notifyDataSetChanged();
                }
            });

            recList.addOnItemTouchListener(dismissListener);
        }
    }


    private class SaveCircle extends AsyncTask<Void, Void, List<User>> {

        private SweetAlertDialog pDialog;
        private List<ParseObject> circles;
        private UserContact contact;

        private SaveCircle(SweetAlertDialog pDialog, List<ParseObject> circles, UserContact contact) {
            this.pDialog = pDialog;
            this.circles = circles;
            this.contact = contact;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setTitleText(UPDATING_CIRCLES);
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            basicController.saveCircles(circles, contact);
            return basicController.getCircles();
        }

        @Override
        protected void onPostExecute(List<User> circles) {
            super.onPostExecute(circles);
            pDialog.cancel();
            showCircles(circles);
        }
    }

    private class GetCircles extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected List<User> doInBackground(Void... params) {
            return basicController.getCircles();
        }

        @Override
        protected void onPostExecute(List<User> circles) {
            super.onPostExecute(circles);
            showCircles(circles);
        }
    }

    private class UpdateCircles extends AsyncTask<Void, Void, List<User>> {
        private SwipeRefreshLayout swipeRefresh;

        private UpdateCircles(SwipeRefreshLayout swipeRefresh) {
            this.swipeRefresh = swipeRefresh;
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            userDetailsObject = ObjectService.getUserDetailsObject();
            if (userDetailsObject != null) {
                ParseRelation<ParseObject> circleRelation = userDetailsObject.getRelation(Constants.USER_CIRCLE_RELATION);
                try {
                    List<ParseObject> circles = circleRelation.getQuery().find();
                    basicController.updateCircles(circles);
                    return basicController.getCircles();
                } catch (ParseException e) {
                    ErrorHandler.handleError(null, e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<User> circles) {
            super.onPostExecute(circles);
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
            showCircles(circles);
        }
    }

}
