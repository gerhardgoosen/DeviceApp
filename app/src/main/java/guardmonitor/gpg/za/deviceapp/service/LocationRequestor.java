package guardmonitor.gpg.za.deviceapp.service;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import guardmonitor.gpg.za.deviceapp.MainActivity;
//import guardmonitor.gpg.za.deviceapp.MapsActivity;
import guardmonitor.gpg.za.deviceapp.async.TrackLocationTask;
import guardmonitor.gpg.za.deviceapp.utils.NotificationUtils;
import guardmonitor.gpg.za.deviceapp.utils.PermissionHelper;


/**
 * Created by Gerhard on 2016/09/21.
 */
public class LocationRequestor implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private final static String TAG = "LocationRequestor";
    private static final String TRACKING_URL = "http://197.85.186.13/oopswhatnow/restapi/geo/track/";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private Location mCurrentLocation;


    private Context mContext;
    private MainActivity mainActivity;

    public LocationRequestor(Context context, MainActivity mainActivity) {
        Log.v(TAG, "Constructed LocationRequestor : ");
        this.mainActivity = mainActivity;
        this.mContext = context;
        mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .enableAutoManage(mainActivity, 34992, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }


    public void requestCurrentLocation() {
        Log.v(TAG, "requestCurrentLocation");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }


    public GoogleApiClient getGoogleApiClient(){
        return this.mGoogleApiClient;
    }


//implemented overrides

    @Override
    public void onLocationChanged(final Location location) {
        Log.v(TAG, "onLocationChanged");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        this.mainActivity.updateLocation(mCurrentLocation);

        if (mainActivity.isTrackPosition()) {
            try {
                TrackLocationTask trackLocationTask = new TrackLocationTask(mContext, location);
                String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

                URL url = new URL(TRACKING_URL + deviceId);
                trackLocationTask.execute(url);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "onConnected");

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);//(2 * 1000); // Update location every 2 seconds
        mLocationRequest.setFastestInterval(50);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.v(TAG, "LocationSettingsStatusCodes.SUCCESS");
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        if(!PermissionHelper.hasPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)){
                            PermissionHelper.requestPermissions( mainActivity,0,new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.v(TAG, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(mainActivity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.v(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

        this.requestCurrentLocation();
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.v(TAG,"onRequestPermissionsResult");
        if (requestCode == 0) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.requestCurrentLocation();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int code) {
        Log.v(TAG, "onConnectionSuspended");
        NotificationUtils.makeToast(mContext,"onConnectionSuspended" + code, 1);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(TAG, "onConnectionFailed " + result.getErrorMessage() );
        NotificationUtils.makeToast(mContext,"onConnectionFailed ",1);
    }

}
