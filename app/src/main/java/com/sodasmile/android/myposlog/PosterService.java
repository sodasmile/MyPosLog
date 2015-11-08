package com.sodasmile.android.myposlog;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class PosterService extends IntentService {

    private static final String TAG = "GPS_MONITOR";

    public static final String LOCATION_EXTRA = "com.sodasmile.android.myposlog.location";
    public static final String CHOSEN_EXTRA = "com.sodasmile.android.myposlog.chosen";

    public PosterService() {
        super("PosterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(LOCATION_EXTRA);
        String chosen = intent.getStringExtra(CHOSEN_EXTRA);
        if (location == null) {
            return;
        }
        try {
            String spec = String.format("http://sodasmile.com/poslog?id=%s&lat=%s&lon=%s&alt=%s&time=%s&accu=%s&bear=%s&speed=%s&track=%s",
                    "asm",
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getTime(),
                    location.getAccuracy(),
                    location.getBearing(),
                    location.getSpeed(),
                    chosen);

            URLConnection connection = new URL(spec).openConnection();
            InputStream stream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                Log.i(TAG, line);
            }
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }
}
