package guardmonitor.gpg.za.deviceapp.async;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import guardmonitor.gpg.za.deviceapp.utils.HttpRequest;
import guardmonitor.gpg.za.deviceapp.utils.NotificationUtils;

/**
 * Created by Gerhard on 2016/09/21.
 */
public class TrackLocationTask extends AsyncTask<URL, Integer, Long> {
    private final static String TAG = "TrackLocationTask";
    private Context mContext;

    private BufferedReader reader;
    private Location trackedLocation;

    public TrackLocationTask(Context context, Location locationData) {
        this.mContext = context;
        this.trackedLocation = locationData;
    }

    protected Long doInBackground(URL... urls) {
        int count = urls.length;
        long totalSize = 0;
        for (int i = 0; i < count; i++) {


            //totalSize += Downloader.downloadFile(urls[i]);
            try {
                String deviceUUID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                JSONObject locationJSON = new JSONObject();
                locationJSON.put("device", deviceUUID);
                locationJSON.put("longitude", String.valueOf(trackedLocation.getLongitude()));
                locationJSON.put("latitude", String.valueOf(trackedLocation.getLatitude()));
                locationJSON.put("altitude", String.valueOf(trackedLocation.getAltitude()));
                locationJSON.put("accuracy", String.valueOf(trackedLocation.getAccuracy()));

                final String response = this.trackPosition(urls[i], locationJSON, deviceUUID);

                //NotificationUtils.makeToast(mContext,"Tracked Position : ["+response+"]",1);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        return totalSize;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.v(TAG, "onProgressUpdate");
    }

    @Override
    protected void onPostExecute(Long result) {
        //showDialog("Position Tracked " + result + " bytes");
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute");
    }


    public void showDialog(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Alert");

        // Setting Dialog Message
        alertDialog.setMessage(message);


        // on pressing cancel button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    private String trackPosition(URL TRACKING_URL, JSONObject location, String deviceId) {
        StringBuilder sb = new StringBuilder();



        try {
            Log.v(TAG, "URL :::: > " + TRACKING_URL + deviceId);
            HttpRequest req =new HttpRequest(TRACKING_URL + deviceId);
            req.withHeaders("Content-Type: application/json");//add request header: "Content-Type: application/json"
            req.prepare(HttpRequest.Method.POST);//Set HttpRequest method as PUT
            req.withData(location.toString());//Add json data to request body
            JSONObject res=req.sendAndReadJSON();
            Log.v(TAG,":::: >  response : " + res.toString());


        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "Exception  :::: >  e : " + e.getMessage());
        }

        return sb.toString();

    }
}