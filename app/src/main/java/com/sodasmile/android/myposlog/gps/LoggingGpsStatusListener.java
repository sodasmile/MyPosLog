package com.sodasmile.android.myposlog.gps;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;

public class LoggingGpsStatusListener implements GpsStatus.Listener {

    private static final String TAG = "GPS_MONITOR";
    private LocationManager locationManager;
    private GpsStatus gpsStatus;

    public LoggingGpsStatusListener(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Log.i(TAG, "GPS status changed to " + event + ": " + gpsStatusToString(event));
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                logStatus();
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                logStatus();
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                logStatus();
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                logStatus();
            default:
                break;
        }
    }

    private void logStatus() {
        gpsStatus = locationManager.getGpsStatus(gpsStatus);
        Log.i(TAG, "Found " + gpsStatus.getMaxSatellites() + " satellites, first fix: " + gpsStatus.getTimeToFirstFix());
        Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
        for (GpsSatellite satellite : satellites) {
            Log.i(TAG, String.format("Gps Status: Azimut: %f, elevation: %f, prn: %d, snr: %f, usedInFix: %s, hasAlmanac: %s, hasEphemeris: %s",
                    satellite.getAzimuth(),
                    satellite.getElevation(),
                    satellite.getPrn(),
                    satellite.getSnr(),
                    satellite.usedInFix(),
                    satellite.hasAlmanac(),
                    satellite.hasEphemeris()));
        }
    }

    private String gpsStatusToString(int gpsStatus) {
        switch (gpsStatus) {
            case GpsStatus.GPS_EVENT_STARTED:
                return "GPS_EVENT_STARTED";
            case GpsStatus.GPS_EVENT_STOPPED:
                return "GPS_EVENT_STOPPED";
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                return "GPS_EVENT_FIRST_FIX";
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                return "GPS_EVENT_SATELLITE_STATUS";
            default:
                return "UNKNOWN";

        }
    }
}
