package com.android.camera;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class LocationManager {
    public static final int LOC_MNGR_ERR_PERM_DENY = 1;
    private static final String TAG = "LocationManager";
    private Context mContext;
    private Listener mListener;
    LocationListener[] mLocationListeners = {new LocationListener("gps"), new LocationListener("network")};
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;
    private boolean mWaitingLocPermResult = false;

    public interface Listener {
        void onErrorListener(int i);
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        String mProvider;
        boolean mValid = false;

        public void onProviderEnabled(String str) {
        }

        public LocationListener(String str) {
            this.mProvider = str;
            this.mLastLocation = new Location(this.mProvider);
        }

        public void onLocationChanged(Location location) {
            if (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d) {
                if (!this.mValid) {
                    Log.d(LocationManager.TAG, "Got first location.");
                }
                this.mLastLocation.set(location);
                this.mValid = true;
            }
        }

        public void onProviderDisabled(String str) {
            this.mValid = false;
        }

        public void onStatusChanged(String str, int i, Bundle bundle) {
            if (i == 0 || i == 1) {
                this.mValid = false;
            }
        }

        public Location current() {
            if (this.mValid) {
                return this.mLastLocation;
            }
            return null;
        }
    }

    public LocationManager(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public Location getCurrentLocation() {
        if (!this.mRecordLocation) {
            return null;
        }
        int i = 0;
        while (true) {
            LocationListener[] locationListenerArr = this.mLocationListeners;
            if (i < locationListenerArr.length) {
                Location current = locationListenerArr[i].current();
                if (current != null) {
                    return current;
                }
                i++;
            } else {
                Log.d(TAG, "No location received yet.");
                return null;
            }
        }
    }

    public void recordLocation(boolean z) {
        if (this.mRecordLocation != z && !this.mWaitingLocPermResult && hasLoationPermission()) {
            this.mRecordLocation = z;
            if (z) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    private boolean hasLoationPermission() {
        return this.mContext.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;
    }

    public void waitingLocationPermissionResult(boolean z) {
        this.mWaitingLocPermResult = z;
    }

    private void startReceivingLocationUpdates() {
        String str = "provider does not exist ";
        String str2 = "fail to request location update, ignore";
        String str3 = TAG;
        if (this.mLocationManager == null) {
            this.mLocationManager = (android.location.LocationManager) this.mContext.getSystemService("location");
        }
        android.location.LocationManager locationManager = this.mLocationManager;
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates("network", 1000, 0.0f, this.mLocationListeners[1]);
            } catch (SecurityException e) {
                Log.i(str3, str2, e);
                Listener listener = this.mListener;
                if (listener != null) {
                    listener.onErrorListener(1);
                }
                recordLocation(false);
            } catch (IllegalArgumentException e2) {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(e2.getMessage());
                Log.d(str3, sb.toString());
            }
            try {
                this.mLocationManager.requestLocationUpdates("gps", 1000, 0.0f, this.mLocationListeners[0]);
            } catch (SecurityException e3) {
                Log.i(str3, str2, e3);
                Listener listener2 = this.mListener;
                if (listener2 != null) {
                    listener2.onErrorListener(1);
                }
                recordLocation(false);
                return;
            } catch (IllegalArgumentException e4) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str);
                sb2.append(e4.getMessage());
                Log.d(str3, sb2.toString());
            }
            Log.d(str3, "startReceivingLocationUpdates");
        }
    }

    private void stopReceivingLocationUpdates() {
        if (this.mLocationManager != null) {
            int i = 0;
            while (true) {
                LocationListener[] locationListenerArr = this.mLocationListeners;
                int length = locationListenerArr.length;
                String str = TAG;
                if (i < length) {
                    try {
                        this.mLocationManager.removeUpdates(locationListenerArr[i]);
                    } catch (Exception e) {
                        Log.i(str, "fail to remove location listners, ignore", e);
                    }
                    i++;
                } else {
                    Log.d(str, "stopReceivingLocationUpdates");
                    return;
                }
            }
        }
    }
}
