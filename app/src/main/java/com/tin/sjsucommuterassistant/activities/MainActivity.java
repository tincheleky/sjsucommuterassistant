/*
    Tin Che
    SID: 0106 47 364
    CS 175 - 2 Final Project.
    Google sign in is taken from developers.google.com
*/


package com.tin.sjsucommuterassistant.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tin.sjsucommuterassistant.R;
import com.tin.sjsucommuterassistant.dialogs.AdsDialog;
import com.tin.sjsucommuterassistant.helpers.JSONParserHelper;
import com.tin.sjsucommuterassistant.listeners.OnSwipeListener;
import com.tin.sjsucommuterassistant.providers.DataContentProvider;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    //CONSTANTS:
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 111;
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final String FILE_NAME = "sjsu_commuter_assistant";
    public static final String USER_DISPLAY_NAME = "USER_DISPLAY_NAME";
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    public static final double SJSU_LAT = 37.335143;
    public static final double SJSU_LONG = -121.881276;

    //Google Vars:
    private boolean mLocationPermissionGranted;
    private boolean isInit = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LatLngBounds.Builder curLocationAndSJSUBoundBuilder;
    private static MarkerOptions SJSUMarker = new MarkerOptions();
    private CameraPosition mCameraPosition;
    private GoogleSignInOptions gso;
    private Geocoder geocoder;

    //Sensors vars:
    private SensorManager sensorManager;
    private float compassDegree;

    //UI vars:
    private TextView tvHelloUser;
    private TextView tvAddress;
    private TextView tvCompass;
    private TextView tvDelay;
    private ImageView ivTraffic;
    private TextView tvSignout;
    SharedPreferences sharedPreferences;

    DistanceMatrixTask distanceMatrixTask;

    //App var:
    boolean isLoggedIn = false;
    boolean hasExternalStorage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        geocoder = new Geocoder(this, Locale.getDefault());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        hasExternalStorage = isExternalAvailalble();

        tvHelloUser = (TextView) findViewById(R.id.display_hello_user);
        tvDelay = (TextView) findViewById(R.id.display_traffic_delay);
        tvAddress = (TextView) findViewById(R.id.display_address);
        tvCompass = (TextView) findViewById(R.id.display_compass);
        ivTraffic = (ImageView) findViewById(R.id.display_traffic_status);
        tvSignout = (TextView) findViewById(R.id.signout_button);
        if(tvHelloUser != null){
            tvHelloUser.setOnTouchListener(new OnSwipeListener(MainActivity.this){

                @Override
                public void onSwipeRight(){
                    System.out.println("TESTING SWIPE RIGHT");
                    if(tvSignout != null) {
                        tvSignout.setVisibility(ImageView.INVISIBLE);
                        tvSignout.setEnabled(false);
                    }

                }

                @Override
                public void onSwipeLeft(){
                    System.out.println("TESTING SWIPE LEFT");
                    if(tvSignout != null) {
                        tvSignout.setVisibility(ImageView.VISIBLE);
                        tvSignout.setEnabled(true);
                    }
                }

                @Override
                public void onClick() {
                    super.onClick();
                    signIn();
                }
            });
            String username = sharedPreferences.getString(USER_DISPLAY_NAME, "");
            if(username.length() > 1){
                tvHelloUser.setText("Hi, " + username);
                isLoggedIn = true;
            }
            else {
                tvHelloUser.setText("Please log in");
            }
        }

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        buildGoogleApiClient();
//        mGoogleApiClient.connect();
        new ConnectingTask().execute("Task Connection");
        SJSUMarker.title("SJSU");
        SJSUMarker.position(new LatLng(SJSU_LAT, SJSU_LONG));
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private synchronized void buildGoogleApiClient() {

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        createLocationRequest();
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // A step later in the tutorial adds the code to get the device
        // location.

        try {
            if (mLocationPermissionGranted) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                System.out.println("REGISTER LOCATION UPDATE");
            }
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    try {
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        updateMap();
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLocation = null;
            }
        }catch(SecurityException e)
        {
            e.printStackTrace();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

    /*
     * Sets the desired interval for active location updates. This interval is
     * inexact. You may not receive updates at all if no location sources are available, or
     * you may receive them slower than requested. You may also receive updates faster than
     * requested if other applications are requesting location at a faster interval.
     */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

    /*
     * Sets the fastest rate for active location updates. This interval is exact, and your
     * application will never receive updates faster than this value.
     */
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Do setup things when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(!mMap.isTrafficEnabled()){
            mMap.setTrafficEnabled(true);
        }
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
        else if (mCurrentLocation != null) {
            updateMap();
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                startNavigation(null);
            }
        });

        //_toast("Map is ready");
    }

    private void _toast(String whatToToast)
    {
        Toast.makeText(this, whatToToast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        // Create a LatLngBounds that includes the city of Current Location and SJSU
        updateMap();
        System.out.println("LocationUpdate: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());


        if(!isInit) {
            distanceMatrixTask = new DistanceMatrixTask();
            distanceMatrixTask.execute(String.valueOf(mCurrentLocation.getLatitude())
                    + ","
                    + String.valueOf(mCurrentLocation.getLongitude()));

            isInit = true;
        }
    }

    private void updateMap()
    {
        //getDeviceLocation();
        curLocationAndSJSUBoundBuilder = new LatLngBounds.Builder();

        if(mCurrentLocation != null) {
            //UPDATE MAP MARKERS
            curLocationAndSJSUBoundBuilder.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .include(new LatLng(37.335143, -121.881276));

            // Constrain the camera target.
            if (mMap != null) {
                LatLngBounds tempBounds = curLocationAndSJSUBoundBuilder.build();
                mMap.setLatLngBoundsForCameraTarget(tempBounds);
                mMap.clear();
                MarkerOptions homeMarker = new MarkerOptions();
                homeMarker.title("Current Location");
                if (mCurrentLocation != null) {
                    homeMarker.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    mMap.addMarker(homeMarker);
                }
                mMap.addMarker(SJSUMarker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(tempBounds, 250));
                //System.out.println("Set Map Bounds");
            }

            //UPDATE ADDRESS
            updateAddressTextView();

        }
    }

    private void updateAddressTextView()
    {
        List<Address> addresses = null;
        try {
            if(geocoder != null) {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            }
            if(tvAddress != null) {
                if(addresses != null) {
                    tvAddress.setText(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void updateCompassTextView()
    {
        int range = (int) (compassDegree / (360f / 16f));
        String degreesToDirection = "";

        if (range == 15 || range == 0)
            degreesToDirection = "N";
        if (range == 1 || range == 2)
            degreesToDirection = "NE";
        if (range == 3 || range == 4)
            degreesToDirection = "E";
        if (range == 5 || range == 6)
            degreesToDirection = "SE";
        if (range == 7 || range == 8)
            degreesToDirection = "S";
        if (range == 9 || range == 10)
            degreesToDirection = "SW";
        if (range == 11 || range == 12)
            degreesToDirection = "W";
        if (range == 13 || range == 14)
            degreesToDirection = "NW";

        if(tvCompass != null) {
            tvCompass.setText(degreesToDirection);
        }
//        System.out.println("COMPASS DATA: " + compassDegree);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getDeviceLocation();
        updateMap();
        _toast("onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    public void showLoginDialog(View v)
    {
        signIn();

    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess() + "," + result.getStatus());
        if (result.isSuccess()) {
            isLoggedIn = true;
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            tvHelloUser.setText("Hi, " + acct.getDisplayName());
            sharedPreferences.edit().putString("USER_DISPLAY_NAME", acct.getDisplayName()).commit();

//            updateUI(true);
            _toast("Successfully Signed in");
        } else {
            // Signed out, show unauthenticated UI.
//            updateUI(false);
            _toast("Failed to log in");

        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        //updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
        isLoggedIn = false;
    }


    public void startNavigation(View view) {
        AdsDialog adsDialog = new AdsDialog(this, null);
        adsDialog.show();

        if(mCurrentLocation != null) {
            if(isLoggedIn) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DataContentProvider.LATITUDE, mCurrentLocation.getLatitude());
                contentValues.put(DataContentProvider.LONGITUDE, mCurrentLocation.getLongitude());
                contentValues.put(DataContentProvider.TIME, String.valueOf(System.currentTimeMillis()));

                Uri uri = getContentResolver().insert(
                        DataContentProvider.URI, contentValues);

                //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();

                Uri data = Uri.parse("content://com.tin.sjsucommuterassistant.providers.DataContentProvider");
                Cursor c = managedQuery(data, null, null, null, "_id");
                int entries = 0;
                if(c != null) {
                    if (c.moveToFirst()) {
                        do {
                            entries++;
                        } while (c.moveToNext());
                    }
                    System.out.println("Total Data in DB: " + entries);
                }

            }

            if(hasExternalStorage){
                try {
                    FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String strDate = sdf.format(calendar.getTime());
                    fos.write(strDate.getBytes());
                    fos.close();
                    System.out.println("Write to File successfully");

                    FileInputStream fis = openFileInput(FILE_NAME);
                    System.out.println("Bytes available to read: " + fis.available());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }


        }
    }

    //METHODS FOR SensorEventListenner
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        compassDegree = sensorEvent.values[0];

        updateCompassTextView();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void signOutAccounts(View view) {
        signOut();
        if(sharedPreferences!= null){
            sharedPreferences.edit().putString(USER_DISPLAY_NAME, "").commit();
        }
        if(tvHelloUser != null){
            tvHelloUser.setText("Please sign in ");
        }
        if(tvSignout != null) {
            tvSignout.setVisibility(ImageView.INVISIBLE);
            tvSignout.setEnabled(false);
        }
    }

    private class ConnectingTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... args)
        {
            while(!mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.connect();
            }
            return "Connected";
        }

        @Override
        protected void onPostExecute(String result)
        {
            //_toast(result);
        }
    }


    //This class and downloadUrl function are partially taken from
    //http://wptrafficanalyzer.in/blog/gps-and-google-map-in-android-applications-series/
    private class DistanceMatrixTask extends AsyncTask<String, Void, String[]>{

        @Override
        protected String[] doInBackground(String... params) {
            // For storing data from web service
            String data[] = new String[2];

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyA7qp9aVHzsRTtNs4TT99bSYQYt0lUdPn4";

            //https://maps.googleapis.com/maps/api/distancematrix/json?
            // origins=37.359289,-121.966124
            // &destinations=37.335143,-121.881276
            // &mode=driving&departure_time=now
            // &traffic_mode=best_guess
            // &key=AIzaSyA7qp9aVHzsRTtNs4TT99bSYQYt0lUdPn4

            // Current Location
            String origins = "origins=" + params[0];

            // SJSU coordinate
            String destinations = "destinations=37.335143,-121.881276";

            // Mode
            String mode = "mode=driving";

            /// Departure_time
            String departure_time = "departure_time=now";

            //traffic_mode
            String traffic_mode = "traffic_mode=best_guess";



            // Building the parameters to the web service
            String parameters1 = origins + "&" + destinations + "&" + key;
            String parameters2 = origins + "&" + destinations + "&" + mode + "&" + departure_time + "&" + traffic_mode + "&" + key;


            // Output format
            String output = "json";

            // Building the url to the web service
            String url1 = "https://maps.googleapis.com/maps/api/distancematrix/" + output + "?" + parameters1;
            String url2 = "https://maps.googleapis.com/maps/api/distancematrix/" + output + "?" + parameters2;


            try{
                // Fetching the data from we service
                data[0] = downloadUrl(url1);
                data[1] = downloadUrl(url2);
//                System.out.println(url1);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

//        // Creating ParserTask
//        parserTask = new ParserTask();
//
//        // Starting Parsing the JSON string returned by Web Service
//        parserTask.execute(result);
            System.out.println("API CALL FOR DISTANCE MATRIX");
            System.out.println(result);
            int duration = JSONParserHelper.getDurationFromJSON(result[0]);
            int duration_in_traffic = JSONParserHelper.getDurationInTrafficFromJSON(result[1]);
            int delay = duration_in_traffic - duration;

            if(tvDelay != null){
                delay /= 60;
                if(delay < 0)
                    delay = 0;
                tvDelay.setText(String.valueOf(delay) + " minutes");

            }
            double percentage = duration_in_traffic / (double) duration;
            if(ivTraffic != null){
                System.out.println("PERCENTAGE: " + percentage);
                if(percentage > 1.70){
                    ivTraffic.setBackgroundColor(Color.RED);
                }else if(percentage > 1.25){
                    ivTraffic.setBackgroundColor(Color.YELLOW);
                }else{
                    ivTraffic.setBackgroundColor(Color.GREEN);
                }
            }

        }

        /** A method to download json data from url */
        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while( ( line = br.readLine()) != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                e.printStackTrace();
            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    //Check if external storage is available for Read and Write.
    private boolean isExternalAvailalble() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
