package com.jasperwong.smartbicycle.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
 import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.service.FrontService;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;


public class UserActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,OnDateSelectedListener,OnMonthChangedListener {

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    private TextView dayShow;
    private Intent serviceIntent;
    @Bind(R.id.calendarView)
    MaterialCalendarView widget;
    private TextView dayKmTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_user);
        setSupportActionBar(toolbar);

        dayShow=(TextView)findViewById(R.id.dayTV);
        dayKmTV=(TextView)findViewById(R.id.dayKmTV);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ButterKnife.bind(this);
        widget.setOnDateChangedListener(this);
        widget.setOnMonthChangedListener(this);
//        widget.setTopbarVisible(false);
//        widget.set
        //Setup initial text
        widget.setDynamicHeightEnabled(true);
        widget.setTileHeightDp(35);
        widget.setTopbarVisible(false);
        serviceIntent=new Intent(this, FrontService.class);
        startService(serviceIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(FrontService.TAG));
//        textView.setText(getSelectedDatesString());
    }

    private String getSelectedDatesString() {
        CalendarDay date = widget.getSelectedDate();
        if (date == null) {
            return "No Selection";
        }
        return FORMATTER.format(date.getDate());
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(serviceIntent);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        dayShow.setText(FORMATTER.format(date.getDate()));
        float num =(float)(Math.random() * 3);
        BigDecimal   b  =   new BigDecimal(num);
        num=b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
        dayKmTV.setText(getSelectedDatesString().valueOf(num));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_login) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_guide) {
            Intent guideIntent = new Intent(this, RouteActivity.class);
            startActivity(guideIntent);
            // Handle the camera action
        } else if (id == R.id.nav_switch) {
            Intent switchIntent = new Intent(this, SwitchActivity.class);
            startActivity(switchIntent);
        } else if (id == R.id.nav_setting) {
            Intent settingIntent = new Intent(this, SettingActivity.class);
            startActivity(settingIntent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_connect) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(UserActivity.this, "关闭程序", Toast.LENGTH_LONG).show();
            BaseActivity.ActivityCollector.finishAll();
            stopService(serviceIntent);
        }
    };
    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
//        getSupportActionBar().setTitle(FORMATTER.format(date.getDate()));
    }
}
