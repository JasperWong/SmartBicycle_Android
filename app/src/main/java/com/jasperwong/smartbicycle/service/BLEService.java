/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jasperwong.smartbicycle.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.jasperwong.smartbicycle.activity.UserActivity;
import com.jasperwong.smartbicycle.ble.SampleGattAttributes;
import com.jasperwong.smartbicycle.sqlite.MyDatabaseHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//import android.app.Activity;
//import android.bluetooth.BluetoothGattDescriptor;


public class BLEService extends Service
{

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
    private byte []recData;
    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;
    private String username;
    private SharedPreferences.Editor saver;
    private SharedPreferences loader;
    private MyDatabaseHelper dbHelper;

    //private final int REQUEST_ENABLE_BT = 1;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_RSSI =
            "com.example.bluetooth.le.ACTION_GATT_RSSI";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_READ =
            "com.example.bluetooth.le.ACTION_DATA_READ";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_DATA_WRITE =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);


    public final static UUID UUID_BLE_TX = UUID
            .fromString(SampleGattAttributes.BLE_TX);
    public final static UUID UUID_BLE_RX = UUID
            .fromString(SampleGattAttributes.BLE_RX);
    public final static UUID UUID_BLE_SERVICE = UUID
            .fromString(SampleGattAttributes.BLE_SERVICE);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            Log.d(TAG, "onReadRemoteRssi");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_RSSI, rssi);
            }
            else
            {
                Log.w(TAG, "onReadRemoteRssi received: " + status);
            }
        };

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else
            {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d("action","onRec");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_READ, characteristic);
            }
            else
            {
                Log.w(TAG, "onCharacteristicRead: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d("action123","onWrite");
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_WRITE, characteristic);
            }
            else
            {
                Log.w(TAG, "onCharacteristicWrite: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.d("action123","onChange");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int rssi)
    {
        Log.d(TAG, "broadcastUpdate - rssi");
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {
        saver = getSharedPreferences("data", MODE_PRIVATE).edit();
        loader= getSharedPreferences("data",MODE_PRIVATE);
        int times;
        float distance=0;
        float hour=0;
        int distance_metre=0;
        int min=0;
        dbHelper = new MyDatabaseHelper(this, "test.db", null, 1);
        dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
//        recData=data;
        if(data[0]==117) {
            if(data[1]==100) {
                min = data[7];
                distance_metre = data[2];
                Log.d("data", "metre:" + distance_metre + " min:" + min);
                distance = (float) (distance_metre / 1000.0);
                hour = (float) (min / 60.0);
                float distanceTotal=loader.getFloat("distanceTotal", 0) + distance;
                float hourTotal=loader.getFloat("hourTotal",0)+hour;
                int timesTotal=loader.getInt("timesTotal",0)+1;
                saver.putFloat("distanceTotal", distanceTotal);
                saver.putFloat("hourTotal",hourTotal);
                saver.putInt("timesTotal",timesTotal);
                saver.commit();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                values.put("username", loader.getString("username","error"));
                values.put("date", FORMATTER.format(System.currentTimeMillis()));
                values.put("distanceDay", distance);
                values.put("distanceTotal", distanceTotal);
                values.put("hourTotal",hourTotal);
                values.put("timesTotal",timesTotal);
                db.replace("USER", null, values); // 插入第一条数据
                values.clear();
                db.close();
                Toast.makeText(this,"update success",Toast.LENGTH_SHORT).show();
//                UserActivity.totalHoursTV.setText(hourTotal+"");
//                UserActivity.totalTimesTV.setText(timesTotal+"");
//                UserActivity.distanceTotalTV.setText(distanceTotal+"");
//                UserActivity.totalHoursTV.setText(2+"");

            }
        }



        username=loader.getString("username","none");
        times=loader.getInt("timesTotal",0);
//        distanceTotal=loader.getFloat("");
//        Log.d("usart",data+"");
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
//            for(byte byteChar : data){
//
//            }
//            stringBuilder.toString()
//                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            data1.equals(new String(0xff+""));


            String string=new String("ring");
            if(new String(data).equals(string)){
                Intent intentCall = new Intent(Intent.ACTION_CALL);
                intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentCall.setData(Uri.parse("tel:123"));
                startActivity(intentCall);
            }

            Log.d("usart",new String(data));
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder
    {
        public BLEService getService()
        {
            return BLEService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize()
    {
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }


        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to get Bluetooth Adapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }


        // Previously connected device.  Try to reconnect.
        if (
                mBluetoothDeviceAddress != null
                        && address.equals(mBluetoothDeviceAddress)
                        && mBluetoothGatt != null)
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect())
            {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else
            {
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
                mBluetoothDeviceAddress = address;
                Log.d(TAG, "Connection failed");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect()
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given util.BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close()
    {
        if (mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    public void readRssi()
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    /**
     * Write to a given char
     *
     * @param characteristic The characteristic to write to
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_BLE_RX.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
            UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readDescriptor(descriptor);
    }


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (mBluetoothGatt == null)
        {
            Log.d(TAG, "getSupportedGattService: mBluetoothGatt == null");
            return null;
        }

        return mBluetoothGatt.getServices();
    }


}