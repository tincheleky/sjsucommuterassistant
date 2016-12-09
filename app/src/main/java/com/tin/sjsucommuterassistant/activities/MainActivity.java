package com.tin.sjsucommuterassistant.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tin.sjsucommuterassistant.R;
import com.tin.sjsucommuterassistant.dialogs.LoginDialog;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 111;
    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    public static final double SJSU_LAT = 37.335143;
    public static final double SJSU_LONG = -121.881276;

    private boolean mLocationPermissionGranted;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LatLngBounds.Builder curLocationAndSJSUBoundBuilder;
    private static MarkerOptions SJSUMarker = new MarkerOptions();
    private CameraPosition mCameraPosition;
    private TextView tvHelloUser;
    private GoogleSignInOptions gso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHelloUser = (TextView) findViewById(R.id.display_hello_user);
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        SJSUMarker.title("SJSU");
        SJSUMarker.position(new LatLng(SJSU_LAT, SJSU_LONG));
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
        _toast("Map is ready");

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

    }

    private void updateMap()
    {
        //getDeviceLocation();
        if(mCurrentLocation != null) {
            curLocationAndSJSUBoundBuilder = new LatLngBounds.Builder();
            curLocationAndSJSUBoundBuilder.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                                        .include(new LatLng(37.335143, -121.881276));
            System.out.println("LocationUpdate: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        }

        // Constrain the camera target.
        if(mMap != null) {
            LatLngBounds tempBounds = curLocationAndSJSUBoundBuilder.build();
            mMap.setLatLngBoundsForCameraTarget(tempBounds);
            mMap.clear();
            MarkerOptions homeMarker = new MarkerOptions();
            homeMarker.title("Current Location");
            if(mCurrentLocation != null) {
                homeMarker.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                mMap.addMarker(homeMarker);
            }
            mMap.addMarker(SJSUMarker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(tempBounds, 250));
            System.out.println("Set Map Bounds");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getDeviceLocation();
        updateMap();
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
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
//            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
//            updateUI(true);
            _toast("Successfully Signed in");
        } else {
            // Signed out, show unauthenticated UI.
//            updateUI(false);
            _toast("Failed to log in");

        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
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
    }


}
