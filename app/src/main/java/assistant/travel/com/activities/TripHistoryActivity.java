/*
 * Copyright 2018 David Tainton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package assistant.travel.com.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import assistant.travel.com.R;
import assistant.travel.com.adapters.RecyclerAdapterTrips;
import assistant.travel.com.adapters.SwipeToDeleteCallback;
import assistant.travel.com.database.Trip;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripHistoryActivity extends AppCompatActivity {


    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.trip_history_layout)
    CoordinatorLayout coordinatorLayout;


    RecyclerAdapterTrips mAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    ArrayList<Trip> trips = new ArrayList<>();

    private static final String COLLECTION_USER = "users";
    private static final String SUBCOLLECTION_TRIPS = "trips";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        populateRecyclerView();
        enableSwipeToDeleteAndUndo();

    }

    private void populateRecyclerView() {

        mProgressBar.setVisibility(View.VISIBLE);

        db.collection(COLLECTION_USER).document(mAuth.getCurrentUser().getUid()).collection(SUBCOLLECTION_TRIPS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        mProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Trip trip = new Trip();
                                trip.setDistance(document.getDouble("distance"));
                                trip.setTimeTaken(document.getDouble("time-taken"));
                                trip.setStartDateTime(document.getString("start-date"));
                                trip.setEndDateTime(document.getString("end-date"));
                                trip.setTransportMode(document.getString("transport-mode"));

                                trips.add(trip);
                            }

                            mAdapter = new RecyclerAdapterTrips(trips);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });

    }

    private void enableSwipeToDeleteAndUndo() {

        // Allows user to delete trip by swiping the list item to the left
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final Trip item = mAdapter.getData().get(position);

                mAdapter.removeItem(position);

                // Gives user a chance to undo delete if it was done accidentally
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Trip was removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mAdapter.restoreItem(item, position);
                        mRecyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
