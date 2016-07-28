package com.jasperwong.smartbicycle.activity;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.jasperwong.smartbicycle.R;
import com.jasperwong.smartbicycle.ble.GATTUtils;
import com.jasperwong.smartbicycle.fragment.EditDialogFragment;
import com.jasperwong.smartbicycle.service.BLEService;
import com.jasperwong.smartbicycle.util.TTSController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends Activity implements AMapNaviListener,AMapNaviViewListener{


    private String TAG=this.getClass().getSimpleName();
    public final static String[] dirActions = { "无", "自车", "左转", "右转", "左前方行驶",
            "右前方行驶", "左后方行驶", "右后方行驶", "左转掉头", "直行", "到达途经点", "进入环岛", "驶出环岛",
            "到达服务区", "到达收费站", "到达目的地", "进入隧道", "靠左", "靠右", "通过人行横道", "通过过街天桥",
            "通过地下通道", "通过广场", "到道路斜对面" };

    private String EndLat;
    private String EndLng;
    private String StartLat;
    private String StartLng;

    private BLEService mBluetoothLeService=null;
    BluetoothGattCharacteristic mCharacteristic;
    ArrayList<InputStream> inputStreamArrayList = new ArrayList<InputStream>();

    TTSController mTtsManager;
    AMapNaviView mAMapNaviView;
    AMapNavi mAMapNavi;
    NaviLatLng mEndLatLng;
    NaviLatLng mStartLatlng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_);
        naviInit();
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        Intent intent =this.getIntent();
        EndLat=intent.getStringExtra("EndLat");
        EndLng=intent.getStringExtra("EndLng");
        StartLat=intent.getStringExtra("StartLat");
        StartLng=intent.getStringExtra("StartLng");
        mEndLatLng=new NaviLatLng(Double.parseDouble(EndLat),Double.parseDouble(EndLng));
        mStartLatlng=new NaviLatLng(Double.parseDouble(StartLat),Double.parseDouble(StartLng));

//        Intent gattServiceIntent=new Intent(GuideActivity.this,BLEService.class);
//        bindService(gattServiceIntent,mServiceConnection,BIND_AUTO_CREATE);

    }

//    private final ServiceConnection mServiceConnection = new ServiceConnection()
//    {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service)
//        {
//            Log.d(TAG, "start service Connection");
//
//            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
//
//            //从搜索出来的services里面找出合适的service
//            List<BluetoothGattService> gattServiceList = mBluetoothLeService.getSupportedGattServices();
//            mCharacteristic = GATTUtils.lookupGattServices(gattServiceList, GATTUtils.BLE_TX);
//            mCharacteristic.setValue("123");
//            mBluetoothLeService.writeCharacteristic(mCharacteristic);
////            //
////            if( null != mCharacteristic )
////            {
//                mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
////                InputStream inputStream = buildSendData();
////                inputStreamArrayList.add(inputStream);
////                byte[] writeBytes = new byte[11];
////                int byteCount = 0;
////                try
////                {
////                    byteCount = inputStream.read(writeBytes,0,11);
////                    if( byteCount > 0)
////                    {
////                        mCharacteristic.setValue(writeBytes);
////                        mBluetoothLeService.writeCharacteristic(mCharacteristic);
////                    }
////                }
////                catch (IOException e)
////                {
////                    e.printStackTrace();
////                }
////            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName)
//        {
//            Log.d(TAG, "end Service Connection");
//            mBluetoothLeService = null;
//        }
//    };
//
//
//    private static IntentFilter buildGattUpdateIntentFilter()
//    {
//        final IntentFilter intentFilter = new IntentFilter();
//
//        intentFilter.addAction(BLEService.ACTION_DATA_WRITE);
//
//        return intentFilter;
//    }
//
//
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
//    {
//        @Override
//        public void onReceive(Context context, Intent intent)
//        {
////            Log.d(TAG, "enter BroadcastReceiver");
////            final String action = intent.getAction();
////            Log.d(TAG, "action = " + action);
////            if (BLEService.ACTION_DATA_WRITE.equals(action))
////            {
////                Log.d(TAG, "receive data");
////                if( inputStreamArrayList.size() != 0)
////                {
////                    InputStream writeStream = inputStreamArrayList.get(0);
////                    byte[] readBytes = new byte[11];
////                    try
////                    {
////                        int byteCount = writeStream.read(readBytes, 0, 11);
////                        if (byteCount > 0)
////                        {
////                            mCharacteristic.setValue(readBytes);
////                            mBluetoothLeService.writeCharacteristic(mCharacteristic);
////                            return;
////                        }
////                        else
////                        {
////                            inputStreamArrayList.remove(0);
////                            Log.d(TAG, "finish send stream!");
////                        }
////                    }
////                    catch (IOException e)
////                    {
////                        e.printStackTrace();
////                    }
////                }
////
////                if (inputStreamArrayList.size() == 0)
////                {
////                    InputStream inputStream = buildSendData();
////                    inputStreamArrayList.add(inputStream);
////                    byte[] writeBytes = new byte[11];
////                    int byteCount = 0;
////                    try
////                    {
////                        byteCount = inputStream.read(writeBytes,0,11);
////                        if( byteCount > 0)
////                        {
////                            mCharacteristic.setValue(writeBytes);
////                            mBluetoothLeService.writeCharacteristic(mCharacteristic);
////                        }
////                    }
////                    catch (IOException e)
////                    {
////                        e.printStackTrace();
////                    }
////                }
////            }
//        }
//    };
    @Override
    public void onInitNaviSuccess() {
        mAMapNavi.calculateWalkRoute(mStartLatlng,mEndLatLng);
    }

    private void naviInit(){
        mTtsManager = TTSController.getInstance(getApplicationContext());
        mTtsManager.init();
        mTtsManager.startSpeaking();

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        mAMapNavi.addAMapNaviListener(mTtsManager);
        mAMapNavi.setEmulatorNaviSpeed(1000);
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        Log.d("test_info", "前方 " + dirActions[naviInfo.m_Icon]);
    }

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onNaviViewLoaded() {
        Log.d("wlx", "导航页面加载成功");
        Log.d("wlx", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
//        mStartList.add(mStartLatlng);
//        mEndList.add(mEndLatLng);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();

//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
        mTtsManager.stopSpeaking();
//
//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
//        mAMapNavi.stopNavi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        //since 1.6.0
        //不再在naviview destroy的时候自动执行AMapNavi.stopNavi();
        //请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        mTtsManager.destroy();
    }

    @Override
    public void onCalculateRouteSuccess() {
        mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

}
