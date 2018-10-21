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

package assistant.travel.com.database;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import assistant.travel.com.adapters.RecyclerAdapterTrips;

/**
 *
 */

public class DbConnector {

    private static final String COLLECTION_USER = "users";

    private static final String SUBCOLLECTION_TRIPS = "trips";

    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_TIME_TAKEN = "time-taken";
    private static final String KEY_TRANSPORT_MODE = "transport-mode";
    private static final String KEY_START_LOCATION = "start-location";
    private static final String KEY_END_LOCATION = "end-location";
    private static final String KEY_START_DATETIME = "start-date";
    private static final String KEY_END_DATETIME = "end-date";

    private static final String SUBCOLLECTION_SETTINGS = "settings";

    private static final String KEY_PREFERRED_TRANSPORT = "preferred-transport";
    private static final String KEY_MEASUREMENT_SYSTEM = "measurement-system";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public DbConnector() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public void addTrip(Trip trip) {
        Map<String, Object> newTrip = new HashMap<>();

        newTrip.put(KEY_DISTANCE, trip.getDistance());
        newTrip.put(KEY_TIME_TAKEN, trip.getTimeTaken());
        newTrip.put(KEY_TRANSPORT_MODE, trip.getTransportMode());
        newTrip.put(KEY_START_LOCATION, new GeoPoint(trip.getStartLocation().getLatitude(), trip.getStartLocation().getLongitude()));
        newTrip.put(KEY_END_LOCATION, new GeoPoint(trip.getEndLocation().getLatitude(), trip.getEndLocation().getLongitude()));
        newTrip.put(KEY_START_DATETIME, trip.getStartDateTime());
        newTrip.put(KEY_END_DATETIME, trip.getEndDateTime());

        db.collection(COLLECTION_USER).document(mAuth.getCurrentUser().getUid())
                .collection(SUBCOLLECTION_TRIPS).document().set(newTrip)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.d("Database", "Failed");
                    }
                });
    }

    public ArrayList<Trip> fetchTrips() {
        final ArrayList<Trip> trips = new ArrayList<>();

        db.collection(COLLECTION_USER).document(mAuth.getCurrentUser().getUid()).collection(SUBCOLLECTION_TRIPS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
                        }
                    }
                });

        return trips;
    }

    public void addSettings(Settings settings) {
        Map<String, Object> newSettings = new HashMap<>();

        //newSettings.put(KEY_MEASUREMENT_SYSTEM, settings.isMetricMeasurement());
        //newSettings.put(KEY_PREFERRED_TRANSPORT, settings.getPreferredTransportMode());

        db.collection(COLLECTION_USER).document(mAuth.getCurrentUser().getUid())
                .collection(SUBCOLLECTION_SETTINGS).document().set(newSettings)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.d("Database", "Failed");
                    }
                });
    }

}
