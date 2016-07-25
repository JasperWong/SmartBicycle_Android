package com.jasperwong.smartbicycle.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jasperwong.smartbicycle.R;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    public static List<Activity> activities = new ArrayList<Activity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_base);
        ActivityCollector.addActivity(this);
    }


    public static class ActivityCollector {
        public static void addActivity(Activity activity) {
            activities.add(activity);
        }


        public static void finishAll() {
            for (Activity activity : activities) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }

        public static void removeActivity(Activity activity) {
            activities.remove(activity);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}
