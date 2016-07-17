package com.jasperwong.smartbicycle.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.services.poisearch.PoiSearch;
import com.jasperwong.smartbicycle.R;

public class GuideActivity extends BaseActivity{

    public final static String[] dirActions = { "无", "自车", "左转", "右转", "左前方行驶",
            "右前方行驶", "左后方行驶", "右后方行驶", "左转掉头", "直行", "到达途经点", "进入环岛", "驶出环岛",
            "到达服务区", "到达收费站", "到达目的地", "进入隧道", "靠左", "靠右", "通过人行横道", "通过过街天桥",
            "通过地下通道", "通过广场", "到道路斜对面" };

    private String destination;
    private PoiSearch mPoiSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

    }

    @Override
    public void onInitNaviSuccess() {
        mAMapNavi.calculateWalkRoute(endLatLng);
    }

}
