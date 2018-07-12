package guardmonitor.gpg.za.deviceapp.geoFence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.internal.ParcelableGeofence;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

import guardmonitor.gpg.za.deviceapp.MainActivity;
import guardmonitor.gpg.za.deviceapp.R;

/**
 * Created by Gerhard on 2016/09/22.
 */
public class GeoFenceService implements ResultCallback<Status> {

    protected static final String TAG = "GeoFenceService";

    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;
    protected ArrayList<Marker> mGeofenceMarkersList;
    protected ArrayList<Circle> mGeofenceCircleList;

    protected ArrayList<Marker> mTempGeofenceMarkersList;
    protected ArrayList<Circle> mTempGeofenceCircleList;
    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;


    private MainActivity pMainActivity;
    private Context pContext;


    public GeoFenceService(Context context, MainActivity mainActivity) {

        super();
        this.pContext = context;
        this.pMainActivity = mainActivity;


        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();
        mGeofenceCircleList = new ArrayList<Circle>();
        mGeofenceMarkersList = new ArrayList<Marker>();

        mTempGeofenceCircleList = new ArrayList<Circle>();
        mTempGeofenceMarkersList = new ArrayList<Marker>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = this.pMainActivity.getSharedPrefs().getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

    }


    public void drawUserGeoFence(LatLng point) {

        String title = "Temporary Fences [" + point.latitude + "/" + point.longitude + "]";

        Marker m = pMainActivity.getMainMap().addMarker(new MarkerOptions()
                .position(new LatLng(point.latitude, point.longitude))
                .title(title)
                .snippet("Radius:  " + Constants.GEOFENCE_RADIUS_IN_METERS));

        mTempGeofenceMarkersList.add(m);

        m.showInfoWindow();

        //Instantiates a new CircleOptions object +  center/radius
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(point.latitude, point.longitude))
                .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(5);//(2);

        // Get back the mutable Circle
        Circle circle = pMainActivity.getMainMap().addCircle(circleOptions);
        mTempGeofenceCircleList.add(circle);


//        SharedPreferences.Editor editor = this.pMainActivity.getSharedPrefs().edit();
//        editor.put(Constants.GEOFENCES_ADDED_KEY, mTempGeofenceCircleList);
//        editor.apply();


        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(title)
                // Set the circular region of this geofence.
                .setCircularRegion(
                        point.latitude,
                        point.longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());

    }

    public void drawGeoFences() {
        for (Geofence fence : mGeofenceList) {

            if (fence == null) break;

            ParcelableGeofence pf = (ParcelableGeofence)fence;

            Marker m = pMainActivity.getMainMap().addMarker(new MarkerOptions()
                    .position(new LatLng( pf.getLatitude(), pf.getLongitude()))
                    .title("Fence " + pf.getRequestId())
                    .snippet("Radius:  " + Constants.GEOFENCE_RADIUS_IN_METERS));

            mGeofenceMarkersList.add(m);

            m.showInfoWindow();

            //Instantiates a new CircleOptions object +  center/radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(pf.getLatitude(), pf.getLongitude()))
                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(5);//(2);

            // Get back the mutable Circle
            Circle circle = pMainActivity.getMainMap().addCircle(circleOptions);
            mGeofenceCircleList.add(circle);

        }
//        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {
//
//            if (entry == null) break;
//
//            Marker m = pMainActivity.getMainMap().addMarker(new MarkerOptions()
//                    .position(new LatLng(entry.getValue().latitude, entry.getValue().longitude))
//                    .title("Fence " + entry.getKey())
//                    .snippet("Radius:  " + Constants.GEOFENCE_RADIUS_IN_METERS));
//
//            mGeofenceMarkersList.add(m);
//
//            m.showInfoWindow();
//
//            //Instantiates a new CircleOptions object +  center/radius
//            CircleOptions circleOptions = new CircleOptions()
//                    .center(new LatLng(entry.getValue().latitude, entry.getValue().longitude))
//                    .radius(Constants.GEOFENCE_RADIUS_IN_METERS)
//                    .fillColor(0x40ff0000)
//                    .strokeColor(Color.TRANSPARENT)
//                    .strokeWidth(5);//(2);
//
//            // Get back the mutable Circle
//            Circle circle = pMainActivity.getMainMap().addCircle(circleOptions);
//            mGeofenceCircleList.add(circle);
//        }



    }

    public void cleanGeoFences() {
        for (Circle circle : mGeofenceCircleList) {
            circle.remove();
        }
        for (Marker marker : mGeofenceMarkersList) {
            marker.remove();
        }
        for (Circle circle : mTempGeofenceCircleList) {
            circle.remove();
        }
        for (Marker marker : mTempGeofenceMarkersList) {
            marker.remove();
        }


    }

    public boolean isGeofencesAdded() {
        return mGeofencesAdded;
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this.pMainActivity, GeoFenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this.pMainActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {


        for (Marker marker : mTempGeofenceMarkersList) {

            Log.v(TAG, "mTempGeofenceMarkersList - " + marker.getTitle());

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(marker.getTitle())
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            marker.getPosition().latitude,
                            marker.getPosition().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }



        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {
            Log.v(TAG, "BAY_AREA_LANDMARKS - " + entry.getKey());
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    public GeofencingRequest getGeofencingRequest() {

        mGeofenceList = new ArrayList<Geofence>();//clean
        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     * <p>
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = this.pMainActivity.getSharedPrefs().edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            if (mGeofencesAdded) {
                //draw markers
                drawGeoFences();

            } else {
                //remove circiles
                cleanGeoFences();
            }

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.

            Toast.makeText(
                    this.pContext,
                    this.pContext.getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeoFenceErrorMessages.getErrorString(pContext,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

}
