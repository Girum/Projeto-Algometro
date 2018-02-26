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

/*
* Aplicativo feito para integração com módulo BLE pertencente ao LCA - IFSP (desenvolvido por Carlos Eduardo Palmieri Teixeira).
*
* Aplicativo desenvolvido por Giovanni Antunes Bonin
*
* 2017
*
* */

package com.ifsp.lca.projetoalgometro;

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
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();


    public static String dd;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothGattCharacteristic mNotifyCharacteristic, temp;
    private BluetoothLeService mBluetoothLeService;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private String dataf, datab, datat, dataf3, dataf2, f1, f2, f3, fref, fbat, ftemp;
    private float nAux, nAux2, nAux3;
    private long datag, datah, datai;
    private int dataa, ff, ff2, ff3, ffref, ffbat, fftemp;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE2 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE2";
    public final static String ACTION_DATA_AVAILABLE3 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE3";
    public final static String ACTION_DATA_AVAILABLE4 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE4";
    public final static String ACTION_DATA_AVAILABLE5 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE5";
    public final static String ACTION_DATA_AVAILABLE6 =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE6";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_DATA2 =
           "com.example.bluetooth.le.EXTRA_DATA2";
    public final static String EXTRA_DATA3 =
            "com.example.bluetooth.le.EXTRA_DATA3";
    public final static String EXTRA_DATA4 =
            "com.example.bluetooth.le.EXTRA_DATA4";
    public final static String EXTRA_DATA5 =
            "com.example.bluetooth.le.EXTRA_DATA5";
    public final static String EXTRA_DATA6 =
            "com.example.bluetooth.le.EXTRA_DATA6";
    public final static String extra = "";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID UUID_ALG_SERVICE =
            UUID.fromString(SampleGattAttributes.ALG_SERVICE);



    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001") ){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE2, characteristic);
                }
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE3, characteristic);
                }
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE4, characteristic);
                }
                /*if(characteristic.getUuid() == UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE4, characteristic);
                }*/
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE5, characteristic);
                }
                if(characteristic.getUuid() == UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0006") ){
                    broadcastUpdate(ACTION_DATA_AVAILABLE6, characteristic);
                }

            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            /*if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE2, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE3, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE4, characteristic);
            }
            //if (UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())){
              //  broadcastUpdate(ACTION_DATA_AVAILABLE4, characteristic);
            //}
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE5, characteristic);
            }*/
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Toast.makeText(getApplication(), "ATIVOU 1", Toast.LENGTH_SHORT).show();

            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE2, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE3, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE4, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE5, characteristic);
            }
            if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0006").equals(characteristic.getUuid())){
                broadcastUpdate(ACTION_DATA_AVAILABLE6, characteristic);
            }

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);


    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final Intent intent2 = new Intent(action);
        final Intent intent3 = new Intent(action);

        //Sensor 1
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            ff = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);
                Log.e("Valores Sensor 1 são: ", "Byte 2:"+ data[2] + " Byte 1: " + data[1] + " Byte 0: " + data[0] + "----- Convertido: " + datad);
                float n = (float) (datad/1000000.0);
                float dm = (float) (ff/1000.0);
                nAux = dm;
                dataf = String.valueOf(ff);
                f1 = String.format(Locale.US,"%.3f", dm);


            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            //intent.putExtra(EXTRA_DATA,  f1 /*dataf*/ + " V" + "\n" /*+ stringBuilder.toString()*/);
            intent.putExtra(EXTRA_DATA,  f1 /*dataf*/);
            intent.putExtra("val", nAux);
            sendBroadcast(intent);

        }

        //Sensor 2
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            ff2 = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);
                Log.e("Valores Sensor 2 são: ","Byte2: "+ data[2] + " Byte 1: " + data[1] + " Byte 0: " + data[0] + "----- Convertido: " + datad);
                float o = (float) (datad/1000000.0);
                float dm = (float) (ff2/1000.0);
                dataf2 = String.valueOf(ff2);
                nAux2 = dm;
                f2 = String.format(Locale.US,"%.3f", dm);

            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            //intent.putExtra(EXTRA_DATA2, f2 /*dataf2*/ + " V"  + "\n" /*+ stringBuilder.toString()*/);
            intent.putExtra(EXTRA_DATA2,  f2 /*dataf*/);
            intent.putExtra("val2", nAux2);

            sendBroadcast(intent);

        }

        //Sensor 3
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            ff3 = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);
                Log.e("Valores Sensor 3 são: ", "Byte2: "+ data[2] + " Byte 1: " + data[1] + " Byte 0: " + data[0] + "----- Convertido: " + datad);
                //float m = (float) (datad/1000000.0);
                float m = (float) (datad);
                dataf3 = String.valueOf(ff3);
                float dm = (float) (ff3/1000.0);
                nAux3 = dm;
                //f3 = String.format(Locale.US,"%.3f", m);
                f3 = String.format(Locale.US,"%.3f", dm);

            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            intent.putExtra(EXTRA_DATA3,  f3/*dataf3*/ + " V" + "\n" /*+ stringBuilder.toString()*/);
            intent.putExtra("val3", nAux3);

            sendBroadcast(intent);

        }

        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            ffref = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);
                Log.e("Valor Referencia é: ", "Byte 2:"+ data[2] + " Byte 1: " + data[1] + " Byte 0: " + data[0] + "----- Convertido: " + datad);
                float dm = (float) (ffref/1000.0);
                fref = String.format(Locale.US,"%.2f", dm);


            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            intent.putExtra(EXTRA_DATA4,  fref /*dataf*/ + " V" + "\n" /*+ stringBuilder.toString()*/);
            sendBroadcast(intent);

        }

        //Bateria
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005").equals(characteristic.getUuid())){
            //if (UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            ffbat = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);

                Log.e("Valores bateria são: ", "Byte 1: " + data[1] + "Byte 0: " + data[0] + "----- Convertido: " + datad);
                datab = String.valueOf(ffbat);


            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            intent.putExtra(EXTRA_DATA5,  datab +"%" + "\n" /*+ stringBuilder.toString()*/);

            sendBroadcast(intent);

        }


        //Temperatura
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0006").equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final int f = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);

            fftemp = f;
            if (data != null && data.length > 0) {

                long datad = convertByteToInt(data);
                float dm = (float) (ffref/1000);
                //Log.e("Valores são: ", "Byte 1: " + data[1] + "Byte 0: " + data[0] + "----- Convertido: " + datad);
                float t = (float) (datad/10.0);
                datat = String.valueOf(dm);
            }

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%4d", byteChar));
            intent3.putExtra(EXTRA_DATA6,  datat + " ºC" + "\n" /*+ stringBuilder.toString()*/);

            sendBroadcast(intent3);

        }

    }

    public long convertByteToInt(byte[] b){
        long value= 0;
        /*for(int i=0;i<b.length;i++){
            int n=(b[i]<0?(int)b[i]+256:(int)b[i])<<(8*i);
            value+=n;
        }*/

        //value = b[3]*16777216 + b[2]*65536 + b[1]*256 + b[0];
        value = b[2]*65536 + b[1]*256 + b[0];
        return value;
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
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
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
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




    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }



        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0006").equals(characteristic.getUuid())) {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }





    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void readCustomCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        // BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07")); //servico raspi
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb11")); //caracteristica raspi
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff5")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004



        setCharacteristicNotification(mReadCharacteristic, true);


        mBluetoothGatt.readCharacteristic(mReadCharacteristic);


        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/


    }

    public void readCustomCharacteristic2() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        // BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07")); //servico raspi
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb11")); //caracteristica raspi
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff5")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0002")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004


        setCharacteristicNotification(mReadCharacteristic, true);
        mBluetoothGatt.readCharacteristic(mReadCharacteristic);

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/



    }

    public void readCustomCharacteristic3() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/

        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0003"));


        setCharacteristicNotification(mReadCharacteristic, true);


        mBluetoothGatt.readCharacteristic(mReadCharacteristic);

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/

    }

    public void readCustomCharacteristicP1() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        // BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07")); //servico raspi
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb11")); //caracteristica raspi
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff5")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004


        setCharacteristicNotification(mReadCharacteristic, false);


        //mBluetoothGatt.readCharacteristic(mReadCharacteristic);

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/


    }

    public void readCustomCharacteristicP2() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        // BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07")); //servico raspi
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb11")); //caracteristica raspi
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff5")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004


        setCharacteristicNotification(mReadCharacteristic, false);


        //mBluetoothGatt.readCharacteristic(mReadCharacteristic);

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/


    }

    public void readCustomCharacteristicP3() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        // BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07")); //servico raspi
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0"));
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb11")); //caracteristica raspi
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff5")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0001")); // caracterista 101 6E400001-B5A3-F393-E0A9-E50E24DC0004


        setCharacteristicNotification(mReadCharacteristic, false);


        //mBluetoothGatt.readCharacteristic(mReadCharacteristic);

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setCharacteristicNotification(mReadCharacteristic, false);*/


    }

    public void readCustomCharacteristicRef() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/

        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0004"));
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));



        setCharacteristicNotification(mReadCharacteristic, true);


        mBluetoothGatt.readCharacteristic(mReadCharacteristic);

    }

    public void readCustomCharacteristicBattery() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));
        //BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/

        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0005"));
        //BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));



        setCharacteristicNotification(mReadCharacteristic, true);


        mBluetoothGatt.readCharacteristic(mReadCharacteristic);

    }

    public void readCustomCharacteristicTemperature() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));


        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/

        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0006"));


        setCharacteristicNotification(mReadCharacteristic, true);


        mBluetoothGatt.readCharacteristic(mReadCharacteristic);

    }




    public void writeCustomCharacteristicSensor1(int value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/


        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));

        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0011"));
        mWriteCharacteristic.setValue(value,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);

        if(mBluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
            Log.w(TAG, "Failed to write characteristic");
        }
    }

    public void writeCustomCharacteristicSensor2(int value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/


        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));

        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0012"));
        mWriteCharacteristic.setValue(value,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);

        if(mBluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
            Log.w(TAG, "Failed to write characteristic");
        }
    }

    public void writeCustomCharacteristicSensor3(int value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/


        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0000"));

        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dc0013"));
        mWriteCharacteristic.setValue(value,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);

        if(mBluetoothGatt.writeCharacteristic(mWriteCharacteristic) == false){
            Log.w(TAG, "Failed to write characteristic");
        }
    }



}


