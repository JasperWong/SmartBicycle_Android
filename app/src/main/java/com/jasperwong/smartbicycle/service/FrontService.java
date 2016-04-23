package com.jasperwong.smartbicycle.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.activity.MainActivity;

import static android.content.Intent.getIntent;

/**
 * Created by JasperWong on 2016/4/14.
 */
public class FrontService extends Service {

    public static String TAG="test";
    public void onCreate(){
        super.onCreate();
        Log.d("test","service onCreate");
//        Toast.makeText(this,"service onCreate",Toast.LENGTH_LONG).show();
    }

    public int onStartCommand(Intent intent,int flags,int startId){
//        Toast.makeText(this,"service onStart",Toast.LENGTH_LONG).show();
        Log.d("test","service onStart");
        displayNotification();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void displayNotification(){
        Intent intent =new Intent(TAG);
         PendingIntent piResult0 = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification mNotification = new Notification.Builder(FrontService.this)
                .setContentTitle("SmartBicycle")
                .setContentText("点击停止")
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentIntent(piResult0)
                .build();
        FrontService.this.startForeground(1, mNotification);
//        Toast.makeText(this, "开启前台服务", Toast.LENGTH_LONG).show();
    }


}
