package com.example.abhilash.reminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static String sub;
    public static String des;
    public static String loc;
    public static String sdate;
    public static String edate;
    public static String time;

    public static double Lat = 12.09;
    public static double Lon= 99.76;

    SharedPreferences someData;
    SharedPreferences.Editor editor;
    HashMap<String, String> map = new HashMap<String, String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        someData = getSharedPreferences("SubLatLng",0);
        editor = someData.edit();
        map = (HashMap<String, String>) someData.getAll();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void addReminder(View view)
    {
        //TextView str= (TextView) findViewById(R.id.hello_text_view);
        setContentView(R.layout.reminder_ui);
    }

    public void setLocation(View view){
        EditText text1 = (EditText) findViewById(R.id.subject);
        sub = text1.getText().toString();
        EditText text2 = (EditText) findViewById(R.id.description);
        des = text2.getText().toString();


        DatePicker dp1 =(DatePicker) findViewById(R.id.datePicker1);
        sdate = String.valueOf(dp1.getYear())+"-"+String.valueOf(dp1.getMonth()+1)+"-"+String.valueOf(dp1.getDayOfMonth());

        DatePicker dp2 =(DatePicker) findViewById(R.id.datePicker2);
        edate = String.valueOf(dp2.getYear())+"-"+String.valueOf(dp2.getMonth()+1)+"-"+String.valueOf(dp2.getDayOfMonth());

        TimePicker tp =(TimePicker) findViewById(R.id.timePicker);
        time = tp.getCurrentHour()+":"+tp.getCurrentMinute();
        startActivity(new Intent(MainActivity.this, AddLocation.class));
    }

    public void viewReminders(View view)
    {

        startActivity(new Intent(MainActivity.this, Existing.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("App killed", "App destroyed start service :"+map.size());
        if(map.size()!=0)
        startService(new Intent(this, MyService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
