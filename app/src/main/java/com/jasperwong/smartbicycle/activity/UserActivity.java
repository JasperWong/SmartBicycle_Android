package com.jasperwong.smartbicycle.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
 import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.service.FrontService;
import com.jasperwong.smartbicycle.sqlite.MyDatabaseHelper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;


public class UserActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,OnDateSelectedListener,OnMonthChangedListener {

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    private TextView dayShow;
    private Intent serviceIntent;
    private EditText usernameET;
    private EditText heightET;
    private EditText weightET;
    @Bind(R.id.calendarView)
    MaterialCalendarView widget;
    private TextView dayKmTV;
    private TextView totalTimesTV;
    private TextView totalHoursTV;
    private MyDatabaseHelper dbHelper;
    private SharedPreferences.Editor saver;
    private SharedPreferences loader;
    private TextView nameShow;
    private TextView idShow;
    private TextView distanceTotalTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_user);
        setSupportActionBar(toolbar);
        totalTimesTV=(TextView)findViewById(R.id.timesTV);
        totalHoursTV=(TextView)findViewById(R.id.hourTV);
        dayShow=(TextView)findViewById(R.id.dayTV);
        dayKmTV=(TextView)findViewById(R.id.dayKmTV);
        nameShow=(TextView)findViewById(R.id.nameTV);
        idShow=(TextView)findViewById(R.id.idTV);
        distanceTotalTV=(TextView)findViewById(R.id.kmTV);
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
        //Setup initial text
        widget.setDynamicHeightEnabled(true);
        widget.setTileHeightDp(35);
        widget.setTopbarVisible(false);
        dbHelper = new MyDatabaseHelper(this, "test.db", null, 1);
        dbHelper.getWritableDatabase();
        serviceIntent=new Intent(this, FrontService.class);
        startService(serviceIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(FrontService.TAG));
        saver = getSharedPreferences("data", MODE_PRIVATE).edit();
        loader= getSharedPreferences("data",MODE_PRIVATE);
        nameShow.setText(loader.getString("username",""));
        idShow.setText("ID:"+loader.getInt("id",0));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor=db.query("USER",null,null,null,null,null,null,null);
        float distanceTotal =0;
        float hour = 0;
        float times=0;
        if(cursor.moveToFirst()){
            do {
                String username = cursor.getString(cursor.getColumnIndex("username"));
                distanceTotal = cursor.getFloat(cursor.getColumnIndex("distanceTotal"));
                hour = cursor.getFloat(cursor.getColumnIndex("hourTotal"));
                times= cursor.getInt(cursor.getColumnIndex("timesTotal"));
                if (nameShow.getText().equals(username)) {
                    totalTimesTV.setText(times+"");
                    totalHoursTV.setText(hour+"");
                    distanceTotalTV.setText(distanceTotal+"");
                    break;
                }
            }while(cursor.moveToNext());
        }
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
        float distanceDay_real=0;
        //If you change a decorate, you need to invalidate decorators
        String dateSelect=new String(FORMATTER.format(date.getDate()));
        dayShow.setText(dateSelect);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor=db.query("USER",null,null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                String username = cursor.getString(cursor.getColumnIndex("username"));
                String dateDB = cursor.getString(cursor.getColumnIndex("date"));
                float distanceDay = cursor.getFloat(cursor.getColumnIndex("distanceDay"));
                if (dateDB.equals(dateSelect)) {
                    distanceDay_real=distanceDay;
                    break;
                }
            }while(cursor.moveToNext());
        }
    //        float distanceDay=cursor.getFloat(cursor.getColumnIndex("date"));
//        float num =(float)(Math.random() * 3);
        BigDecimal  b  =   new BigDecimal(distanceDay_real);
        distanceDay_real=b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
        dayKmTV.setText(getSelectedDatesString().valueOf(distanceDay_real));
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
            final AlertDialog.Builder routeDialog=new AlertDialog.Builder(this);
            View edit= getLayoutInflater().inflate(R.layout.edit_user_info,null);
            routeDialog.setCancelable(true);
            routeDialog.setView(edit);
            usernameET=(EditText)edit.findViewById(R.id.UserNameInput);
            heightET=(EditText)edit.findViewById(R.id.heightInput) ;
            weightET=(EditText)edit.findViewById(R.id.weightInput);
            routeDialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface,int which){
                    int id=(int)(Math.random() * 100000000);
                    idShow.setText("ID:"+id);
                    saver.putString("username",usernameET.getText().toString());
//                    Float height=Float.valueOf(heightET.getText().toString())
                    saver.putFloat("height",Float.parseFloat(heightET.getText().toString()));
                    saver.putFloat("weight",Float.valueOf(weightET.getText().toString()));
                    saver.putInt("id",id);
                    saver.commit();
                    nameShow.setText(loader.getString("username",""));

                }
            });

            routeDialog.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface,int which){
                    Log.d("route","cancel");
                }
            });
            routeDialog.show();

        }   else if(id==R.id.action_update){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor=db.query("USER",null,null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    String username=cursor.getString(cursor.getColumnIndex("username"));
                    String date=cursor.getString(cursor.getColumnIndex("date"));
                    float  distanceDay=cursor.getFloat(cursor.getColumnIndex("distanceDay"));
                    float distanceTotal=cursor.getFloat(cursor.getColumnIndex("distanceTotal"));
                    float hourTotal=cursor.getFloat(cursor.getColumnIndex("hourTotal"));
                    int timesTotal=cursor.getInt(cursor.getColumnIndex("timesTotal"));
                    final String url=new String(
                            "http://jasperwong.cn:8082/SmartBicycle_Server/user/insert?"
                            +"username="+username
                            +"&date="+date
                            +"&distanceDay="+distanceDay
                            +"&distanceTotal="+distanceTotal
                            +"&hourTotal="+hourTotal
                            +"&timesTotal="+timesTotal
                    );
                    Log.d("test",url);
                    sendRequestWithHttpURLConnection(url);
                }while(cursor.moveToNext());
                cursor.close();
            }
//            ContentValues values = new ContentValues();
//            // 开始组装第一条数据
//            values.put("username", "JasperWong");
//            values.put("date", "2016年8月18日");
//            values.put("distanceDay", 12.11);
//            values.put("distanceTotal", 13.22);
//            values.put("hourTotal",55.1);
//            values.put("timesTotal",1123);
//            db.replace("USER", null, values); // 插入第一条数据
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
//        date.getMonth()
    }

    private void sendRequestWithHttpURLConnection(final String urlConfig) {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(new String(urlConfig));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
//                    Message message = new Message();
//                    message.what = SHOW_RESPONSE;
//                    // 将服务器返回的结果存放到Message中
//                    message.obj = response.toString();
//                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



}
