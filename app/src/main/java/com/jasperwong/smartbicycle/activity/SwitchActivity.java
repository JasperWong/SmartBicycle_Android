package com.jasperwong.smartbicycle.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.ble.GATTUtils;
import com.jasperwong.smartbicycle.service.BLEService;
import com.jasperwong.smartbicycle.sqlite.MyDatabaseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SwitchActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener{
    private String TAG = this.getClass().getSimpleName();
    private BLEService mBluetoothLeService=null;
    BluetoothGattCharacteristic mCharacteristic=null;
    Button button;
    private MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_switch);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        Intent gattServiceIntent=new Intent(SwitchActivity.this,BLEService.class);
//        bindService(gattServiceIntent,mServiceConnection,BIND_AUTO_CREATE);
        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(this);
        dbHelper = new MyDatabaseHelper(this, "test.db", null, 1);

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            Log.d(TAG, "start service Connection");

            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();

            //从搜索出来的services里面找出合适的service
            List<BluetoothGattService> gattServiceList = mBluetoothLeService.getSupportedGattServices();
            mCharacteristic = GATTUtils.lookupGattServices(gattServiceList, GATTUtils.BLE_TX);
//            mCharacteristic.setValue("吴彦祖");
//            mBluetoothLeService.writeCharacteristic(mCharacteristic);
//            mBluetoothLeService.readCharacteristic(mCharacteristic);
//            //
            if( null != mCharacteristic )
            {
                mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
//                InputStream inputStream = buildSendData();
//                inputStreamArrayList.add(inputStream);
//                byte[] writeBytes = new byte[11];
//                int byteCount = 0;
//                try
//                {
//                    byteCount = inputStream.read(writeBytes,0,11);
//                    if( byteCount > 0)
//                    {
//                        mCharacteristic.setValue(writeBytes);
//                        mBluetoothLeService.writeCharacteristic(mCharacteristic);
//                    }
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
           }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            Log.d(TAG, "end Service Connection");
            mBluetoothLeService = null;
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_guide) {
            Intent guideIntent= new Intent(this,RouteActivity.class);
            startActivity(guideIntent);
        } else if (id == R.id.nav_switch) {
            Intent switchIntent=new Intent(this,SwitchActivity.class);
            startActivity(switchIntent);
        } else if (id == R.id.nav_setting) {
            Intent settingIntent=new Intent(this,SettingActivity.class);
            startActivity(settingIntent);
        }else if(id==R.id.nav_share){
            Intent intent=new Intent(this,UserActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_connect) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
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
    private void sendRequestWithHttpURLConnection() {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://jasperwong.cn:8082/SmartBicycle_Server/user/insert?username=JasperWong&" +
                            "date=2016年8月25日&distanceDay=22.12&" +
                            "distanceTotal=12.1&HourTotal=12&timesTotal=13");
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

    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id) {
            case R.id.button:

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            // 开始组装第一条数据
            values.put("username", "JasperWong");
            values.put("date", "2016年8月18日");
            values.put("distanceDay", 12.11);
            values.put("distanceTotal", 13.22);
            values.put("hourTotal",55.1);
            values.put("timesTotal",1123);
            db.replace("USER", null, values); // 插入第一条数据
            values.clear();
                values.put("username", "JasperWong");
                values.put("date", "2016年8月20日");
                values.put("distanceDay", 12.11);
                values.put("distanceTotal", 13.22);
                values.put("hourTotal",55.1);
                values.put("timesTotal",1123);
                db.replace("USER", null, values); // 插入第一条数据
                values.clear();
                values.put("username", "JasperWong");
                values.put("date", "2016年8月21日");
                values.put("distanceDay", 12.11);
                values.put("distanceTotal", 13.22);
                values.put("hourTotal",55.1);
                values.put("timesTotal",1123);
                db.replace("USER", null, values); // 插入第一条数据

        }
    }

}
