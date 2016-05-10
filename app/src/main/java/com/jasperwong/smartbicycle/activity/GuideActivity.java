package com.jasperwong.smartbicycle.activity;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.jasperwong.smartbicycle.R;

public class GuideActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,LocationSource,
        AMapLocationListener,RadioGroup.OnCheckedChangeListener {

    private static final String LTAG = GuideActivity.class.getSimpleName();
    private MapView mMapView = null;
    private AMap aMap;
    private Button button;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
//    private RadioGroup mGPSModeGroup;
    private OnLocationChangedListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mMapView = (MapView) findViewById(R.id.aMap);
        mMapView.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_guide);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_guide) {
            Intent guideIntent= new Intent(this,GuideActivity.class);
            startActivity(guideIntent);
            // Handle the camera action
        } else if (id == R.id.nav_switch) {
            Intent switchIntent=new Intent(this,SwitchActivity.class);
            startActivity(switchIntent);
        } else if (id == R.id.nav_setting) {
            Intent settingIntent=new Intent(this,SettingActivity.class);
            startActivity(settingIntent);
        }
//        else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

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
    }

    protected void onResume(){
        super.onResume();
        mMapView.onResume();
    }

    protected void onPause(){
        super.onPause();
        mMapView.onPause();
        deactivate();
    }

    protected void onDestroy(){
        super.onDestroy();
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
//        mGPSModeGroup = (RadioGroup) findViewById(R.id.gps_radio_group);
//        mGPSModeGroup.setOnCheckedChangeListener(this);
//        mLocationErrText = (TextView)findViewById(R.id.location_errInfo_text);
//        mLocationErrText.setVisibility(View.GONE);
    }

    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
//                mLocationErrText.setVisibility(View.GONE);
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
//                mLocationErrText.setVisibility(View.VISIBLE);
//                mLocationErrText.setText(errText);
            }
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
//        switch (checkedId) {
//            case R.id.gps_locate_button:
//                // 设置定位的类型为定位模式
//                aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
//                break;
//            case R.id.gps_follow_button:
//                // 设置定位的类型为 跟随模式
//                aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
//                break;
//            case R.id.gps_rotate_button:
//                // 设置定位的类型为根据地图面向方向旋转
//                aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
//                break;
//        }

    }

}
