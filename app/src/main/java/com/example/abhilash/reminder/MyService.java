
//this is working : the best
package com.example.abhilash.reminder;

/**
 * Created by CHINMAY on 12-10-2015.
 */
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener
{
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    //NotificationManager nm;
    String toast="not connected yet";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location myCurrentLoc;
    LocationManager mLocationManager;

    //------------repeat--------------------

    private AlarmManager mAlarmManager;
    private Intent mNotificationReceiverIntent, mLoggerReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;
    private static final long INITIAL_ALARM_DELAY = 1 * 10 * 1000L;
    protected static final long JITTER = 5000L;

    private final BroadcastReceiver mybroadcast = new AlarmNotificationReceiver();


    SharedPreferences someData;
    SharedPreferences.Editor editor;
    HashMap<String,String> map = new HashMap<String,String>();


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

        someData = getSharedPreferences("SubLatLng",0);
        editor = someData.edit();
        map = (HashMap<String, String>)someData.getAll();

        Set<Map.Entry<String,String>> se=map.entrySet();
        //min_distance=0;
        Log.e("In MyService","Map size:"+map.size());
       for(Map.Entry<String,String> me : se)
        {

            if(me.getValue().contains("-"))
            {
                editor.remove(me.getKey());
                editor.apply();
                editor.commit();
            }

            Log.e("In MyService :",me.getKey()+" || "+me.getValue());

        }



        //====================================================
        // setup google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
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
          Log.e("tag","BRciver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Toast.makeText(this, "Service Destroyed: "+toast, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnected(Bundle bundle) {
        //  myCurrentLoc= LocationServices.FusedLocationApi.getLastLocation(
        //         mGoogleApiClient);
        Toast.makeText(this, "connected", Toast.LENGTH_LONG).show();

        //------------------------------
        mLocationRequest =  LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        myCurrentLoc= LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mLocationManager =(LocationManager)this.getSystemService(LOCATION_SERVICE);
        // mLocationManager.requestLocationUpdates();
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        toast = myCurrentLoc.getLatitude() + " " + myCurrentLoc.getLongitude();
        Toast.makeText(this, toast, Toast.LENGTH_LONG).show();

        //--------------------- Repeat-------------

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(MyService.this,
                AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                MyService.this, 0, mNotificationReceiverIntent, 0);


        // Set single alarm
        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + INITIAL_ALARM_DELAY,
                mNotificationReceiverPendingIntent);


        // Show Toast message
        Toast.makeText(getApplicationContext(), "Single Alarm Set",
                Toast.LENGTH_SHORT).show();

        //--------------------------------------


        stopSelf();//calls destroy method
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



}
