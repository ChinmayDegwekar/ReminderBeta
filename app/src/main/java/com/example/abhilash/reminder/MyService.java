
//this is working : the best


// Usally :time required for service to complete work: 1 SEC

package com.example.abhilash.reminder;

/**
 * Created by CHINMAY on 12-10-2015.
 */
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener//, com.google.android.gms.location.LocationListener
{
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    //NotificationManager nm;
    String toast = "not connected yet";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location myCurrentLoc;
    Location destLoc;
    double distance = 0;
    double min_distance = Double.MAX_VALUE;
    long next_alarm = 500;
    String notify_subject;
    LocationManager locationManager;
    //------------repeat--------------------

    private AlarmManager mAlarmManager;
    private Intent mNotificationReceiverIntent, mLoggerReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;
    private static final long INITIAL_ALARM_DELAY = 1 * 10 * 1000L;
    protected static final long JITTER = 5000L;

    //LocationListener ll;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;


    private final BroadcastReceiver mybroadcast = new AlarmNotificationReceiver();


    SharedPreferences someData;
    SharedPreferences.Editor editor;
    HashMap<String, String> map = new HashMap<String, String>();


    //--------------------------------------
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        //version 1.0----------------

        someData = getSharedPreferences("SubLatLng", 0);
        editor = someData.edit();
        map = (HashMap<String, String>) someData.getAll();

        Set<Map.Entry<String, String>> se = map.entrySet();
        //min_distance=0;
        Log.e("In MyService", "Map size:" + map.size());
        for (Map.Entry<String, String> me : se) {

            if (me.getValue().contains("-")) {
                editor.remove(me.getKey());
                editor.apply();
                editor.commit();
            }

            Log.e("In MyService :", me.getKey() + " || " + me.getValue());

        }


        //====================================================
        // setup google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //createLocationRequest();
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //tvCurrentPos.setText("changedLoc :" + location.getLatitude() + " " + location.getLongitude());
                locationManager.removeUpdates(this);
                myCurrentLoc = location;

                toast = myCurrentLoc.getLatitude() + " " + myCurrentLoc.getLongitude();
                Toast.makeText(MyService.this, toast, Toast.LENGTH_SHORT).show();

                //compare with every entry in Map
                Set<Map.Entry<String, String>> se = map.entrySet();

                for (Map.Entry<String, String> me : se) {
                    String[] str = me.getValue().split(" ");
                    double latdest = Double.parseDouble(str[0]);
                    double longdest = Double.parseDouble(str[1]);
                    destLoc = new Location("Destination");
                    destLoc.setLatitude(latdest);
                    destLoc.setLongitude(longdest);
                    distance = myCurrentLoc.distanceTo(destLoc);
                    //Log.e("tagrugby","Subject:"+me.getKey()+" || lat: "+str[0]+"   lod: "+str[1]+"  || distance:"+distance);

                    if (min_distance > distance) {
                        notify_subject = me.getKey();
                        min_distance = distance;
                    }

                    //System.out.println(me.getKey()+"  --  "+me.getValue());
                }
                Log.e("tag", "Map size :" + map.size() + " || min_dist:" + min_distance);
                if (min_distance < 500) {
                    Toast.makeText(MyService.this, "You are within 500 m of " + notify_subject, Toast.LENGTH_LONG).show();
                    stopSelf();
                    Vibrator v = (Vibrator) MyService.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    v.vibrate(500);

                } else {
                    //--------------------- Repeat-------------


                    //--------------------- Repeat-------------

                    mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    // Create an Intent to broadcast to the AlarmNotificationReceiver
                    mNotificationReceiverIntent = new Intent(MyService.this,
                            AlarmNotificationReceiver.class);

                    // Create an PendingIntent that holds the NotificationReceiverIntent
                    mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                            MyService.this, 0, mNotificationReceiverIntent, 0);


                    // Set single alarm

                    next_alarm = distaceToTime(min_distance);
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + next_alarm,
                            mNotificationReceiverPendingIntent);//UPGRADEABLE
                    // Show Toast message
                    Toast.makeText(getApplicationContext(), " Alarm Set nearest :" + notify_subject + " || " + min_distance,
                            Toast.LENGTH_SHORT).show();

                    //-------------------------------------

                }
                stopSelf();//calls destroy method
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        //waits for onConnected CallBack
        //----------------------------

        // Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        // Toast.makeText(this, "after connectn: "+toast, Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public void onCreate() {

        super.onCreate();


        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Alarm.SERVICE_CREATED");
        //  registerReceiver(mybroadcast, filter);
        Log.e("tag", "BRciver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Toast.makeText(this, "Service Destroyed: " + toast, Toast.LENGTH_LONG).show();
        Vibrator v = (Vibrator) MyService.this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);


    }

    @Override
    public void onConnected(Bundle bundle) {
        //  myCurrentLoc= LocationServices.FusedLocationApi.getLastLocation(
        //         mGoogleApiClient);

        Log.d("tag-->", "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        //startLocationUpdates();

        //  PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
        //        mGoogleApiClient, mLocationRequest,this);
        Log.e("tag--", "Location update started ..............: ");


        /*

        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();

        //------------------------------
        mLocationRequest =  LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        myCurrentLoc= LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
       // mLocationManager =(LocationManager)this.getSystemService(LOCATION_SERVICE);
        // mLocationManager.requestLocationUpdates();
        //mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        toast = myCurrentLoc.getLatitude() + " " + myCurrentLoc.getLongitude();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

        //compare with every entry in Map
        Set<Map.Entry<String,String>> se=map.entrySet();

        for(Map.Entry<String,String> me : se)
        {
            String[] str = me.getValue().split(" ");
            double latdest = Double.parseDouble(str[0]);
            double longdest = Double.parseDouble(str[1]);
            destLoc = new Location("Destination");
            destLoc.setLatitude(latdest);
            destLoc.setLongitude(longdest);
            distance=myCurrentLoc.distanceTo(destLoc);
            //Log.e("tagrugby","Subject:"+me.getKey()+" || lat: "+str[0]+"   lod: "+str[1]+"  || distance:"+distance);

            if(min_distance > distance)
            {
                notify_subject=me.getKey();
                min_distance=distance;
            }

            //System.out.println(me.getKey()+"  --  "+me.getValue());
        }
        Log.e("tag","Map size :"+map.size()+" || min_dist:"+min_distance);
        if(min_distance < 500 )
        {
            Toast.makeText(MyService.this,"You are within 1 km of "+notify_subject,Toast.LENGTH_LONG).show();
            stopSelf();
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);

        }
        else {
            //--------------------- Repeat-------------


            //--------------------- Repeat-------------

            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Create an Intent to broadcast to the AlarmNotificationReceiver
            mNotificationReceiverIntent = new Intent(MyService.this,
                    AlarmNotificationReceiver.class);

            // Create an PendingIntent that holds the NotificationReceiverIntent
            mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                    MyService.this, 0, mNotificationReceiverIntent, 0);


            // Set single alarm

            next_alarm=distaceToTime(min_distance);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + next_alarm,
                    mNotificationReceiverPendingIntent);//UPGRADEABLE
            // Show Toast message
            Toast.makeText(getApplicationContext(), " Alarm Set nearest :"+notify_subject+" || "+min_distance,
                    Toast.LENGTH_SHORT).show();

            //-------------------------------------

        }
        stopSelf();//calls destroy method

        */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /* @Override
    public void onLocationChanged(Location location) {
        Log.e("tag  ", "Firing onLocationChanged..............................................");
        // first of all stop location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        myCurrentLoc = location;



        toast = myCurrentLoc.getLatitude() + " " + myCurrentLoc.getLongitude();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

        //compare with every entry in Map
        Set<Map.Entry<String,String>> se=map.entrySet();

        for(Map.Entry<String,String> me : se)
        {
            String[] str = me.getValue().split(" ");
            double latdest = Double.parseDouble(str[0]);
            double longdest = Double.parseDouble(str[1]);
            destLoc = new Location("Destination");
            destLoc.setLatitude(latdest);
            destLoc.setLongitude(longdest);
            distance=myCurrentLoc.distanceTo(destLoc);
            //Log.e("tagrugby","Subject:"+me.getKey()+" || lat: "+str[0]+"   lod: "+str[1]+"  || distance:"+distance);

            if(min_distance > distance)
            {
                notify_subject=me.getKey();
                min_distance=distance;
            }

            //System.out.println(me.getKey()+"  --  "+me.getValue());
        }
        Log.e("tag", "Map size :" + map.size() + " || min_dist:" + min_distance);
        if(min_distance < 500 )
        {
            Toast.makeText(MyService.this,"You are within 1 km of "+notify_subject,Toast.LENGTH_LONG).show();
            stopSelf();
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);

        }
        else {
            //--------------------- Repeat-------------


            //--------------------- Repeat-------------

            mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Create an Intent to broadcast to the AlarmNotificationReceiver
            mNotificationReceiverIntent = new Intent(MyService.this,
                    AlarmNotificationReceiver.class);

            // Create an PendingIntent that holds the NotificationReceiverIntent
            mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                    MyService.this, 0, mNotificationReceiverIntent, 0);


            // Set single alarm

            next_alarm=distaceToTime(min_distance);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + next_alarm,
                    mNotificationReceiverPendingIntent);//UPGRADEABLE
            // Show Toast message
            Toast.makeText(getApplicationContext(), " Alarm Set nearest :"+notify_subject+" || "+min_distance,
                    Toast.LENGTH_SHORT).show();

            //-------------------------------------

        }
        stopSelf();//calls destroy method
    }
    */


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public long distaceToTime(double distance) {
        return 30000;


    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*public void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.e("tag--", "Location update started ..............: ");
    }*/


    public Location getLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationGPS != null) {
                //return lastKnownLocationGPS;
                return lastKnownLocationGPS;
            } else {
                Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                System.out.println("1::" + loc);//----getting null over here
                System.out.println("2::" + loc.getLatitude());
                return loc;
            }
        } else {
            return null;
        }

    }
}