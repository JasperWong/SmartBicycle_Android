package com.jasperwong.smartbicycle.activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.sqlite.MyDatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SwitchActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener{
    private String TAG = this.getClass().getSimpleName();
    private MyDatabaseHelper dbHelper;
    private SharedPreferences.Editor saver;
    private SharedPreferences loader;
    private ImageView lockBTN=null;
    private ImageView alarmBTN=null;
    private ImageView statusIV=null;
//    private ImageView photoBTN=null;
    private int isLock=0;
    private int isAlarm=0;
    public static final int RESPONSE = 0;
    public static final int GETPHOTO=1;
    private boolean isUpdateDone=false;
    private boolean isUpdateStatus=false;
    Timer QueryTimer = new Timer();
    Timer PhotoTimer=null;
    SmsManager smsManager =null;
    private double bicycleLongtitude=0;
    private double bicycleLatitude=0;
    private int bicycleStatus=0;
    private int bicycleLock=0;
    private int bicycleAlarm=0;
    private TextView statusTV=null;
    private WebView webView=null;
    private boolean isGetPhoto=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_switch);
        setSupportActionBar(toolbar);
        smsManager=SmsManager.getDefault();
        lockBTN=(ImageView)findViewById(R.id.lockView);
        alarmBTN=(ImageView)findViewById(R.id.alarmView);
//        photoBTN=(ImageView)findViewById(R.id.photoView);
        statusTV=(TextView)findViewById(R.id.statusView);
        statusIV=(ImageView)findViewById(R.id.statusImageView);
//        webView=(WebView)findViewById(R.id.webView);
        QueryTimer.schedule(queryTask,0,100);

        lockBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLock==0){
                    isLock=1;
                    Toast.makeText(SwitchActivity.this,"开锁",Toast.LENGTH_SHORT).show();
                }else if(isLock==1){
                    isLock=0;
                    Toast.makeText(SwitchActivity.this,"关锁",Toast.LENGTH_SHORT).show();
                }
                String sendJson=new String("{\"locker\":"+isLock
                                            +",\"alarm\":"+isAlarm
                                            +",\"photo\":0}");
                smsManager.sendTextMessage("13128235741",null,sendJson,null,null);
//                sendUpdateRequest();
            }
        });

        alarmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlarm==0){
                    isAlarm=1;
                    Toast.makeText(SwitchActivity.this,"开启警报",Toast.LENGTH_SHORT).show();
                }else if(isAlarm==1){
                    isAlarm=0;
                    Toast.makeText(SwitchActivity.this,"关闭警报",Toast.LENGTH_SHORT).show();
                }
                String sendJson=new String("{\"locker\":"+isLock
                        +",\"alarm\":"+isAlarm
                        +",\"photo\":0}");
                smsManager.sendTextMessage("13128235741",null,sendJson,null,null);
//                sendUpdateRequest();
            }
        });

//        photoBTN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String sendJson=new String("{\"locker\":"+isLock
//                        +",\"alarm\":"+isAlarm
//                        +",\"photo\":1}");
//                Toast.makeText(SwitchActivity.this,"重拍照片",Toast.LENGTH_SHORT).show();
//                smsManager.sendTextMessage("13128235741",null,sendJson,null,null);
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

//    private Handler photoHandler=new Handler(){
//        public void handleMessage(Message msg){
//                webView.loadUrl("http://jasperwong.cn:8082/photo/bicycle.bmp");
//        }
//    };

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            Log.d("test","enter handler");
            switch (msg.what){
                case RESPONSE: {
                    String responseStr = (String) msg.obj;
                    if (isUpdateStatus) {         //若res为success则是更改数据成功
                        if (responseStr.equals(new String("success"))) {
                            isUpdateStatus = false;
                            Log.d("test", "update success");
                            return;
                        } else {
                            sendUpdateRequest();        //保证成功更新服务器状态
                            Log.d("test", "update fail");
                        }
                    }
                    else {
                        String remoteJson = responseStr;
                        try {
                            JSONObject jsonObject = new JSONObject(remoteJson);
                            bicycleStatus = Integer.parseInt(jsonObject.getString("status"));
                            bicycleLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                            bicycleLongtitude = Double.parseDouble(jsonObject.getString("longitude"));
                            bicycleLock=Integer.parseInt(jsonObject.getString("locker"));
                            bicycleAlarm=Integer.parseInt(jsonObject.getString("alarm"));
                            isAlarm=bicycleAlarm;
                            isLock=bicycleLock;
                            Log.d("test", "status:" + bicycleStatus);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    switch (bicycleStatus) {
                        case 1: {
                            statusTV.setText("自行车状态:正常");
                            statusIV.setImageResource(R.drawable.normal_status);
                            break;
                        }
                        case 2: {
                            statusTV.setText("自行车状态:多次小范围移动");
                            statusIV.setImageResource(R.drawable.normal_status);
                            break;
                        }
                        case 3: {
                            statusIV.setImageResource(R.drawable.normal_status);
                            statusTV.setText("自行车状态:持续多次震动");
                            break;
                        }
                        case 4: {
                            statusIV.setImageResource(R.drawable.fall_status);
                            statusTV.setText("自行车状态:倒下");
                            break;
                        }
                        case 5: {
                            statusIV.setImageResource(R.drawable.up_status);
                            statusTV.setText("自行车状态:被抬高超过50cm");
                            break;
                        }
                        case 6: {
                            statusTV.setText("自行车状态:长时间处于抬高状态");
                            statusIV.setImageResource(R.drawable.lift_status);
                            break;
                        }
                        case 7: {
                            statusTV.setText("自行车状态:长时间处于震动状态");
                            break;
                        }
                        case 8: {
                            statusTV.setText("自行车状态:被撬锁");
                            break;
                        }
                        default:
                            break;
                    }
                    if(isLock==1) lockBTN.setImageResource(R.drawable.switch_on);
                    else lockBTN.setImageResource(R.drawable.switch_off);
                    if(isAlarm==1) {
                        alarmBTN.setImageResource(R.drawable.switch_on);
//                        if(PhotoTimer==null)  PhotoTimeTask();
                    }
                    else {
                        alarmBTN.setImageResource(R.drawable.switch_off);
                        if(PhotoTimer!=null) PhotoTimer.cancel();
                    }
                    break;
                }
                case GETPHOTO:
                {
//                    webView.loadUrl("http://jasperwong.cn:8082/photo/bicycle.bmp");
                }
            }
        }
    };

    private void sendUpdateRequest(){
        StringBuilder stringBuilder=new StringBuilder("http://http://119.29.135.109:8082/SmartBicycle_Server/bicycle/update?id=1&select=2");
        stringBuilder.append("&locker="+isLock);
        stringBuilder.append("&alarm="+isAlarm);
        isUpdateStatus=true;
        sendRequestWithHttpURLConnection(stringBuilder.toString());
    }

//    private void PhotoTimeTask(){
//        PhotoTimer=new Timer();
//        PhotoTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.sendEmptyMessage(GETPHOTO);
//            }
//        },0,10000);
//    }

    private void sendRequestWithHttpURLConnection(final String UrlStr){
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(UrlStr);
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
                    Message message = new Message();
                    message.what = RESPONSE;
                    // 将服务器返回的结果存放到Message中
                    message.obj = response.toString();
                    handler.sendMessage(message);
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

    TimerTask queryTask=new TimerTask() {
        @Override
        public void run() {
            Log.d("test","start task");
            sendRequestWithHttpURLConnection("http://119.29.135.109:8082/SmartBicycle_Server/bicycle/query?id=1");
        }
    };

//    Runnable photoRunable = new Runnable(){
//        @Override
//        public void run(){
//            photoHandler.postDelayed(photoRunable, 10000);
//        }
//    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_switch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.menu_refresh){
            ;
        }
        return true;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_loca) {
            Intent guideIntent = new Intent(this, FoundActivity.class);
            startActivity(guideIntent);
        } else if (id == R.id.nav_switch) {
            Intent switchIntent = new Intent(this, SwitchActivity.class);
            startActivity(switchIntent);
        } else if (id == R.id.nav_plan) {
            Intent settingIntent = new Intent(this, SettingActivity.class);
            startActivity(settingIntent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onClick(View v) {
        int id=v.getId();
        switch (id) {
//            case R.id.button:
//
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            // 开始组装第一条数据
//                values.put("username", "JasperWong");
//                values.put("date", "2016年8月18日");
//                values.put("distanceDay", 3.11);
//                values.put("distanceTotal", 3.11);
//                values.put("hourTotal",0.4);
//                values.put("timesTotal",1);
//                db.replace("USER", null, values); // 插入第一条数据
//                values.clear();
//                values.put("username", "JasperWong");
//                values.put("date", "2016年8月19日");
//                values.put("distanceDay", 2.31);
//                values.put("distanceTotal", 5.42);
//                values.put("hourTotal",0.8);
//                values.put("timesTotal",2);
//                db.replace("USER", null, values); // 插入第一条数据
//                values.clear();
//                values.put("username", "JasperWong");
//                values.put("date", "2016年8月20日");
//                values.put("distanceDay", 2.11);
//                values.put("distanceTotal", 7.53);
//                values.put("hourTotal",1.1);
//                values.put("timesTotal",3);
//                db.replace("USER", null, values); // 插入第一条数据
//                values.clear();
//                values.put("username", "JasperWong");
//                values.put("date", "2016年8月21日");
//                values.put("distanceDay", 2.51);
//                values.put("distanceTotal", 10.04);
//                values.put("hourTotal",1.5);
//                values.put("timesTotal",4);
//                db.replace("USER", null, values); // 插入第一条数据
//                values.clear();
//                values.put("username", "JasperWong");
//                values.put("date", "2016年8月22日");
//                values.put("distanceDay", 12.11);
//                values.put("distanceTotal", 22.15);
//                values.put("hourTotal",2.4);
//                values.put("timesTotal",5);
//                db.replace("USER", null, values); // 插入第一条数据
//                db.delete("USER","date=?",new String[]{"2016年8月25日"});
//                saver.putFloat("distanceTotal",(float)22.15);
//                saver.putFloat("hourTotal",(float)2.4);
//                saver.putInt("timesTotal",5);
//                saver.commit();
        }
    }

}
