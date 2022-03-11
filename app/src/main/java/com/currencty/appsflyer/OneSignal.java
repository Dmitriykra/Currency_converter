package com.currencty.appsflyer;

import android.util.Log;

import com.currencty.WebViewActivity;
import com.onesignal.OSDeviceState;

import java.util.Objects;

public class OneSignal extends WebViewActivity {

    public static String getOSPlayerID() {
        String check = null;
        while (check == null) {
            OSDeviceState device = com.onesignal.OneSignal.getDeviceState();

            check = Objects.requireNonNull(device).getUserId();//push player_id
            Log.d("mainOS", check);
        }

        return check;
    }
}
