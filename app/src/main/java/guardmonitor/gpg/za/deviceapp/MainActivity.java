/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guardmonitor.gpg.za.deviceapp;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import guardmonitor.gpg.za.deviceapp.geoFence.Constants;
import guardmonitor.gpg.za.deviceapp.geoFence.GeoFenceService;
import guardmonitor.gpg.za.deviceapp.service.LocationRequestor;
import guardmonitor.gpg.za.deviceapp.utils.NotificationUtils;

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 * <p>
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 * <p>
 * Note that this Activity implements ResultCallback<Status>, requiring that
 * {@code onResult} must be defined. The {@code onResult} runs when the result of calling
 * {@link GeofencingApi#addGeofences(GoogleApiClient, GeofencingRequest, PendingIntent)}  addGeofences()} or
 * {@link com.google.android.gms.location.GeofencingApi#removeGeofences(GoogleApiClient, java.util.List)}  removeGeofences()}
 * becomes available.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapLongClickListener {

    protected static final String TAG = "MainActivity";

    private GoogleMap mMap;
    private LocationRequestor mLocationRequestor;
    private GeoFenceService mGeoFenceService;
    private boolean placeMarker = false;
    private boolean trackPosition = false;
    private boolean initTotalZoomed = false;



    private SharedPreferences mSharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);



        // Kick off the request to build GoogleApiClient.
        mLocationRequestor = new LocationRequestor(this, this);
        mGeoFenceService = new GeoFenceService(this, this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(TAG, "onMapReady :");
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        initMapSettings();


        if (mGeoFenceService.isGeofencesAdded()) {
            mGeoFenceService.drawGeoFences( );
        }
    }

    /**
     * Set initState of map
     */
    public void initMapSettings() throws SecurityException {
        Log.v(TAG, "initMapSettings");
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException se){
            se.printStackTrace();
        }

        //re-init Fences
        removeGeofences();
        addGeofences();

    }
    @Override
    public void onMapLongClick(LatLng point){
       NotificationUtils.makeToast(this,"Do onMapLongClick...",1);
    }

    public void updateLocation(Location loc) {

        if (loc == null) {
            this.mLocationRequestor.requestCurrentLocation();
        } else {

            Log.v(TAG, "updateLocation : provider : " + loc.getProvider());
            LatLng latlong = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (placeMarker) {
                mMap.addMarker(new MarkerOptions().position(latlong).title("gps[" + loc.getLatitude() + ":" + loc.getLongitude() + "]"));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));

            float zoom = mMap.getCameraPosition().zoom;

//            if (loc.hasSpeed()) {
//                NotificationUtils.makeToast(this, "Speed : " + loc.getSpeed(), 1);
//            }
            if (loc.hasBearing()) {

                if (!initTotalZoomed) {
                    initTotalZoomed = true;
                    zoom = mMap.getMaxZoomLevel() - 2;
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latlong)             // Sets the center of the map to current location
                        .zoom(zoom)                   // Sets the zoom
                        .bearing(loc.getBearing()) // Sets the orientation of the camera to east
                        .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                        .build();                   // Creates a CameraPosition from the builder

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }
        }
    }


    public boolean isTrackPosition() {
        return this.trackPosition;
    }


    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofences() {
        if (!mLocationRequestor.getGoogleApiClient().isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mLocationRequestor.getGoogleApiClient(),
                    // The GeofenceRequest object.
                    mGeoFenceService.getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    mGeoFenceService.getGeofencePendingIntent()
            ).setResultCallback(mGeoFenceService); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofences() {
        if (!mLocationRequestor.getGoogleApiClient().isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mLocationRequestor.getGoogleApiClient(),
                    // This is the same pending intent that was used in addGeofences().
                    mGeoFenceService.getGeofencePendingIntent()
            ).setResultCallback(mGeoFenceService); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }



    public GoogleMap getMainMap() {
        return mMap;
    }

    public SharedPreferences getSharedPrefs() {
        return this.mSharedPreferences;
    }
}