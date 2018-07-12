package guardmonitor.gpg.za.deviceapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by Gerhard on 2016/09/21.
 */
public class NotificationUtils {

    public static void makeToast(final Context app, final String toastMsg, final int length) {
        Handler h = new Handler(Looper.getMainLooper());

        h.post(new Runnable() {
            public void run() {
                Toast.makeText(
                        app,
                        toastMsg,
                        length).show();
            }
        });
    }

}
