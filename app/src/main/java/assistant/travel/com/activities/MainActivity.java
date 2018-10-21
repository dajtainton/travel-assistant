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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import assistant.travel.com.R;
import assistant.travel.com.adapters.RecyclerAdapterDirections;
import assistant.travel.com.database.DbConnector;
import assistant.travel.com.database.Trip;
import assistant.travel.com.helpers.DateTimeParser;
import assistant.travel.com.helpers.DirectionParser;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private TextView mDisplayName;
    private TextView mUsername;

    @BindView(R.id.place_bottom_sheet)
    LinearLayout mPlaceBottomSheet;
    @BindView(R.id.btn_close_places)
    ImageButton mClosePlacesBottomSheet;
    @BindView(R.id.place_name)
    TextView mPlaceName;
    @BindView(R.id.place_address)
    TextView mPlaceAddress;
    @BindView(R.id.btn_get_directions)
    AppCompatButton mGetDirectionsButton;

    @BindView(R.id.bottom_sheet)
    LinearLayout mDirectionsBottomSheet;
    @BindView(R.id.btn_close_directions)
    ImageButton mCloseDirectionsBottomSheet;
    @BindView(R.id.transport_mode)
    ImageView mTransportMode;
    @BindView(R.id.trip_distance)
    TextView mTripDistance;
    @BindView(R.id.trip_eta)
    TextView mTripEta;
    @BindView(R.id.directions_recycleview)
    RecyclerView mDirectionsRecyclerView;


    private BottomSheetBehavior mDirectionsSheetBehavior;
    private BottomSheetBehavior mPlaceSheetBehavior;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    private Location mLocation;
    private Trip mCurrentTrip;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private FirebaseAuth mAuth;


    private long UPDATE_INTERVAL = 15000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean mFirstRun = true;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        initBottomSheets();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        FirebaseUser user = mAuth.getCurrentUser();
        mDisplayName = header.findViewById(R.id.display_name);
        mUsername = header.findViewById(R.id.email_address);
        mDisplayName.setText(user.getDisplayName());
        mUsername.setText(user.getEmail());


        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {

                mMapFragment.getMapAsync(new OnMapReadyCallback() {

                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        setMapPreferences(googleMap);
                        googleMap.clear();

                        googleMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName().toString())
                                .snippet(place.getAddress().toString()));

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));

                        mCurrentTrip = new Trip();
                        Location endLocation = new Location("");
                        endLocation.setLongitude(place.getLatLng().longitude);
                        endLocation.setLatitude(place.getLatLng().latitude);
                        mCurrentTrip.setEndLocation(endLocation);

                        mPlaceName.setText(place.getName().toString());
                        mPlaceAddress.setText(place.getAddress().toString());

                        mDirectionsSheetBehavior.setHideable(true);
                        mDirectionsSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                        mPlaceSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        mPlaceSheetBehavior.setHideable(false);

                        mGetDirectionsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mPlaceSheetBehavior.setHideable(true);
                                mPlaceSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                                mCurrentTrip.setStartLocation(mLocation);
                                mCurrentTrip.setStartDateTime(DateTimeParser.getCurrentDateTime());

                                // Getting URL to the Google Directions API
                                String url = getDirectionsUrl(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), place.getLatLng());

                                DownloadTask downloadTask = new DownloadTask();

                                // Start downloading json data from Google Directions API
                                downloadTask.execute(url);
                            }
                        });


                    }
                });
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void initBottomSheets() {

        mPlaceSheetBehavior = BottomSheetBehavior.from(mPlaceBottomSheet);
        mPlaceSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */

        mPlaceSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mMap.setPadding(0, 0, 0, 0);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mMap.setPadding(0, 0, 0, 550);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mMap.setPadding(0, 0, 0, 350);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mClosePlacesBottomSheet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlaceSheetBehavior.setHideable(true);
                mPlaceSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        mDirectionsSheetBehavior = BottomSheetBehavior.from(mDirectionsBottomSheet);
        mDirectionsSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        mDirectionsSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mMap.setPadding(0, 0, 0, 0);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mMap.setPadding(0, 0, 0, 1050);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mMap.setPadding(0, 0, 0, 300);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mCloseDirectionsBottomSheet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mDirectionsSheetBehavior.setHideable(true);
                mDirectionsSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                saveTrip();
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 10));
            }
        });

    }

    private void initDirectionsRecycler(ArrayList<String> directions, ArrayList<String> distances) {
        // set up the RecyclerView
        mDirectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerAdapterDirections adapter = new RecyclerAdapterDirections(this, directions, distances);
        mDirectionsRecyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mDirectionsRecyclerView.getContext(),
                new LinearLayoutManager(this).getOrientation());
        mDirectionsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void saveTrip() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Save Trip");
        builder.setMessage("Do you want to save this trip?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                mCurrentTrip.setEndDateTime(DateTimeParser.getCurrentDateTime());
                DbConnector db = new DbConnector();
                db.addTrip(mCurrentTrip);

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // I do not need any action here you might
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);


                // Display attributions if required.
                CharSequence attributions = place.getAttributions();
                if (!TextUtils.isEmpty(attributions)) {
                    //mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
                } else {
                    //mPlaceAttribution.setText("");
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocation != null) {
            mMapFragment.getMapAsync(this);
        }

        if (!checkPlayServices()) {
            Toast.makeText(MainActivity.this, "Please install Google Play services.", Toast.LENGTH_LONG).show();
        }
    }


    @SuppressLint("MissingPermission")
    private void setMapPreferences(GoogleMap googleMap) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean traffic = preferences.getBoolean("show_traffic", false);
        String mapType = preferences.getString("map_types", "normal");

        switch (mapType) {
            case "normal":
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "hybrid":
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "satellite":
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }

        mMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.setTrafficEnabled(traffic);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setMapPreferences(googleMap);

        if (mLocation != null && mFirstRun) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 10));
            mFirstRun = false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mMapFragment.getMapAsync(this);

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                finish();

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }


    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent;

        if (id == R.id.nav_profile) {
            intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_history) {
            intent = new Intent(MainActivity.this, TripHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            logoutUser();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private class DownloadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        String tripDistance;
        String tripEta;

        ArrayList<String> inst = new ArrayList<>();
        ArrayList<String> dist = new ArrayList<>();
        ArrayList<LatLng> locs = new ArrayList<>();

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionParser parser = new DirectionParser();

                routes = parser.parse(jObject);
                tripDistance = parser.getTotalDistance();
                tripEta = parser.getTotalTime();
                mCurrentTrip.setDistance(parser.getTotalDistanceInMeters());
                mCurrentTrip.setTimeTaken(parser.getTotalTimeInSeconds());
                inst = (ArrayList<String>) parser.getInstructions();
                dist = (ArrayList<String>) parser.getDistances();
                locs = (ArrayList<LatLng>) parser.getLocations();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 10));
            mDirectionsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mDirectionsSheetBehavior.setHideable(false);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String transportMode = preferences.getString("transport_mode", "driving");
            mCurrentTrip.setTransportMode(transportMode);

            switch (transportMode) {
                case "driving":
                    mTransportMode.setImageResource(R.drawable.ic_drive_text_primary_48dp);
                    break;
                case "walking":
                    mTransportMode.setImageResource(R.drawable.ic_walk_text_primary_48dp);
                    break;
                case "bicycling":
                    mTransportMode.setImageResource(R.drawable.ic_bike_text_primary_48dp);
                    break;
            }

            mTripDistance.setText(String.format("Distance: %s", tripDistance));
            mTripEta.setText(String.format("ETA: %s", tripEta));
            initDirectionsRecycler(inst, dist);

            for (int i = 0; i < result.size(); i++) {

                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap point = path.get(j);
                    double lat = Double.parseDouble(String.valueOf(point.get("lat")));
                    double lng = Double.parseDouble(String.valueOf(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                lineOptions.addAll(points).width(12).color(Color.BLUE).geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String transportMode = preferences.getString("transport_mode", "driving");
        String unitsSettings = preferences.getString("units", "metric");
        // Sensor enabled
        String sensor = "sensor=true";
        String mode = "mode=" + transportMode;
        String units = "units=" + unitsSettings;
        String apiKey = "key=AIzaSyCDrg5jluYDnwiO6FbfhMVDBkFgxm6k66s";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + units + "&" + str_dest + "&" + sensor + "&" + mode + "&" + apiKey;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

}
