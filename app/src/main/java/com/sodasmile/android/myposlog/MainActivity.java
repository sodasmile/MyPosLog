package com.sodasmile.android.myposlog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GPS_MONITOR";
    private static final int MIN_TIME_MS = 2000;
    private static final int MIN_DISTANCE_M = 5;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.i(TAG, "Creating main activity");

        disableTrackButtons();

        String locationMode = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.i(TAG, "found location mode " + locationMode);
        if (locationMode == null || !locationMode.contains("gps")) {
            Utils.displayPromptForEnablingGPS(this);
        }

        locationListener = new MyLocationListener();

        verifyPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        verifyPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (false) { /* To satisfy following code...*/
            int hasPermission = getPackageManager().checkPermission(null, null);
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationUpdated(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        locationUpdated(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

        //locationManager.addGpsStatusListener(new LoggingGpsStatusListener(locationManager));
        locationManager.addGpsStatusListener(new ColorChangerGpsStatusListener());
    }

    private void disableTrackButtons() {
        enableTrackButtons(false);
    }

    private void enableTrackButtons() {
        enableTrackButtons(true);
    }

    private void enableTrackButtons(boolean enabled) {
        findViewById(R.id.track_category_1).setEnabled(enabled);
        findViewById(R.id.track_category_2).setEnabled(enabled);
        findViewById(R.id.track_category_3).setEnabled(enabled);
        findViewById(R.id.track_category_4).setEnabled(enabled);
        findViewById(R.id.track_category_5).setEnabled(enabled);
    }

    private void locationUpdated(Location location) {
        Log.i(TAG, "Received location update: " + location);
        if (location == null) {
            return;
        }
        TextView valueLatitude = (TextView) findViewById(R.id.value_latitude);
        TextView valueLongitude = (TextView) findViewById(R.id.value_longitude);
        TextView valueAltitude = (TextView) findViewById(R.id.value_altitude);
        TextView valueAge = (TextView) findViewById(R.id.value_age);
        valueLatitude.setText(String.format(getString(R.string.location_format), location.getLatitude()));
        valueLongitude.setText(String.format(getString(R.string.location_format), location.getLongitude()));
        valueAltitude.setText(String.format(getString(R.string.location_format), location.getAltitude()));
        long locationTimestamp = location.getTime();
        double age = System.currentTimeMillis() - locationTimestamp;
        double ageSeconds = age / 1000.0;
        valueAge.setText(String.format(getString(R.string.age_format), ageSeconds));

        Intent intent = new Intent(this, PosterService.class);
        intent.putExtra(PosterService.LOCATION_EXTRA, location);
        intent.putExtra(PosterService.CHOSEN_EXTRA, ((TextView) findViewById(R.id.track_chosen)).getText());
        startService(intent);
    }

    private boolean verifyPermission(String permissionName) {
        PackageManager pm = this.getPackageManager();
        int hasPermission = pm.checkPermission(permissionName, this.getPackageName());
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, permissionName + " no granted. Cannot continue");
            return false;
        }
        Log.i(TAG, permissionName + " granted.");
        return true;
    }

    public void startRecording(View view) {
        enableDisable(R.id.button_stop, R.id.button_start);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (false) { /* To satisfy following code...*/
            int hasPermission = getPackageManager().checkPermission(null, null);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, locationListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            locationUpdated(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, locationListener);
        }

        enableTrackButtons();
    }

    public void stopRecording(View view) {
        enableDisable(R.id.button_start, R.id.button_stop);
        disableTrackButtons();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (false) { /* To satisfy following code...*/
            int hasPermission = getPackageManager().checkPermission(null, null);
        }
        locationManager.removeUpdates(locationListener);
    }

    private void enableDisable(int enableRid, int disableRid) {
        View toEnable = findViewById(enableRid);
        View toDisable = findViewById(disableRid);
        toEnable.setEnabled(true);
        toDisable.setEnabled(false);
    }

    public void trackCategory1(View view) {
        registerTrackButtonClicked(view.getId());
    }

    public void trackCategory2(View view) {
        registerTrackButtonClicked(view.getId());
    }

    public void trackCategory3(View view) {
        registerTrackButtonClicked(view.getId());
    }

    public void trackCategory4(View view) {
        registerTrackButtonClicked(view.getId());
    }

    public void trackCategory5(View view) {
        registerTrackButtonClicked(view.getId());
    }

    private void registerTrackButtonClicked(int track_category_id) {
        enableTrackButtons();
        Button chosenButton = (Button) findViewById(track_category_id);
        chosenButton.setEnabled(false);
        TextView chosenText = (TextView) findViewById(R.id.track_chosen);
        chosenText.setText(chosenButton.getText());
    }

    private class ColorChangerGpsStatusListener implements GpsStatus.Listener {

        @Override
        public void onGpsStatusChanged(int event) {
            RadioButton gps = (RadioButton) findViewById(R.id.indicator_gps);
            int color = -1;
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    color = Color.GREEN;
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    color = Color.GRAY;
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    color = Color.YELLOW;
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    color = Color.BLUE;
                default:
                    break;
            }
            if (color != -1
                    ) {
                gps.setTextColor(color);
            }
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Location changed " + location);
            locationUpdated(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "Status changed " + provider + " " + status + " " + extras);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "Provider enabled " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "Provider disabled " + provider);
        }
    }
}
