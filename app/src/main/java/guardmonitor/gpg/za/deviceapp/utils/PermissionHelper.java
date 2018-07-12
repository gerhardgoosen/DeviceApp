package guardmonitor.gpg.za.deviceapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.Arrays;

import guardmonitor.gpg.za.deviceapp.MainActivity;


/**
 * Created by Gerhard on 2016/09/21.
 */
public class PermissionHelper {
    private final static String TAG = "PermissionHelper";

    public static final String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.ACCESS_NETWORK_STATE
    };


    /**
     * Requests "dangerous" permissions for the application at runtime.
     *
     * @param activity    The Activity
     * @param requestCode A requestCode to be passed to the plugin's onRequestPermissionResult()
     *                    along with the result of the permissions request
     * @param permissions The permissions to be requested
     */
    public static void requestPermissions(MainActivity activity, int requestCode, String[] permissions) {
        try {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Exception when requesting permissions " + Arrays.toString(permissions), e);
        }
    }


    /**
     * Checks at runtime to see if the application has been granted a permission.
     *
     * @param activity   The activity
     * @param permission The permission to be checked
     * @return True if the permission has already been granted and false otherwise
     */
    public static boolean hasPermission(MainActivity activity, String permission) {
        try {
            return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "Exception when checking permissions " + permission, e);
        }
        return false;
    }


    /**
     * Checks at runtime to see if the application has been granted a permission.
     *
     * @param activity   The activity
     * @param permissions The permissions to be checked
     * @return True if the permission has already been granted and false otherwise
     */
    public static boolean hasPermissions(MainActivity activity, String[] permissions) {
        try {

            for(String p : permissions)
            {
                if(!PermissionHelper.hasPermission(activity, p))
                {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Exception when checking permissions " + permissions, e);
        }
        return false;
    }

    /**
     * Delivers Permission Results to the application.
     *
     * @param activity    The activity
     * @param requestCode the code for results
     * @param permissions The permission to be checked
     * @return True if the permission has already been granted and false otherwise

    private static void deliverPermissionResult(MapsActivity activity, int requestCode, String[] permissions) {
        // Generate the request results
        int[] requestResults = new int[permissions.length];
        Arrays.fill(requestResults, PackageManager.PERMISSION_GRANTED);
        try {
            activity.onRequestPermissionsResult(requestCode, permissions, requestResults);
        } catch (Exception e) {
            Log.e(TAG, "Exception when delivering permissions results", e);
        }
    }*/


    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    public static void showSettingsAlert(final Context mContext) {
        Log.v(TAG, "showSettingsAlert");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Locations Services Settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

}
