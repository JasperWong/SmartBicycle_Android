package com.jasperwong.smartbicycle.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.services.poisearch.PoiSearch;
import com.jasperwong.smartbicycle.R;

public class SearchActivity extends Activity {
    private String destination;
    private PoiSearch mPoiSearch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent=getIntent();
        destination=intent.getStringExtra("destination");
        Log.d("destination2",destination);


    }
}
