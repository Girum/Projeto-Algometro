

package com.ifsp.lca.projetoalgometro;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView txtValor, txtValor2, txtValor3, txtBat, txtTemp;
    private float n,o,p;

    private ImageView imGauge, imGauge2, imGauge3;

    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic, temp, r;
    private BluetoothGatt mBluetoothGatt;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //ConnEquip.setTextColor(Color.parseColor("#00FF00"));
                //ConnEquip.setText(mDeviceName);
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //ConnEquip.setTextColor(Color.parseColor("#FF0000"));
                //ConnEquip.setText("Nenhum");
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                txtValor.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                n = intent.getFloatExtra("val",0);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;
                        float dd = n;

                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar(imGauge, (n*100)/t);
                    }
                });

            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action)){
                txtValor2.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));
                o = intent.getFloatExtra("val2",0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;


                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar(imGauge2, (o*100)/t);
                    }
                });
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE3.equals(action)){
                txtValor3.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA3));
                p = intent.getFloatExtra("val3",0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;


                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar3(imGauge3, (p*100)/t);
                    }
                });
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE4.equals(action)){
                txtBat.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA4));
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE5.equals(action)){
                txtTemp.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA5));
            }


            /*else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //check = true;
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //displayDataCheck(intent.getStringExtra(BluetoothLeService.extra));
                txtValor.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                mBluetoothLeService.readCustomCharacteristic2();
                if(BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action)){
                    txtValor2.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));
                    mBluetoothLeService.readCustomCharacteristic3();

                    if(BluetoothLeService.ACTION_DATA_AVAILABLE3.equals(action)){
                        txtValor3.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA3));

                    }
                }

            }*/
        }
    };



    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
        //txt_temp_rest.setText("--:--");
    }

    private void circularImageBar(ImageView iv2, float i) {

        Bitmap b = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();



        paint.setColor(Color.parseColor("#c4c4c4"));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(250, 250, 220, paint);
        paint.setColor(Color.parseColor("#0000FF"));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.FILL);
        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);
        oval.set(31,31,470,470);// left top right bottom
        canvas.drawArc(oval, 270, ((-i*360)/100), false, paint);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.parseColor("#0087B0"));
        paint.setTextSize(140);


        //canvas.drawText(/*""+"00"+":"+(int)dd1*/"  "+cc, 250, 250+(paint.getTextSize()/3), paint);
        iv2.setImageBitmap(b);

    }

    private void circularImageBar3(ImageView iv2, float i) {

        Bitmap b = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();



        paint.setColor(Color.parseColor("#c4c4c4"));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(250, 250, 220, paint);
        paint.setColor(Color.parseColor("#FC0F02"));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.FILL);
        final RectF oval = new RectF();
        paint.setStyle(Paint.Style.STROKE);
        oval.set(31,31,470,470);// left top right bottom
        canvas.drawArc(oval, 270, ((-i*360)/100), false, paint);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.parseColor("#0087B0"));
        paint.setTextSize(140);


        //canvas.drawText(/*""+"00"+":"+(int)dd1*/"  "+cc, 250, 250+(paint.getTextSize()/3), paint);
        iv2.setImageBitmap(b);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.button_control);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Toast.makeText(getApplication(), "Name: " +mDeviceName + " Address: " + mDeviceAddress, Toast.LENGTH_SHORT).show();


        txtValor = (TextView) findViewById(R.id.txtValor);
        txtValor2 = (TextView) findViewById(R.id.txtValor2);
        txtValor3 = (TextView) findViewById(R.id.txtValor3);

        txtBat = (TextView) findViewById(R.id.txtBat);
        txtTemp = (TextView) findViewById(R.id.txtTemp);

        imGauge = (ImageView) findViewById(R.id.imGauge);
        imGauge2 = (ImageView) findViewById(R.id.imGauge2);
        imGauge3 = (ImageView) findViewById(R.id.imGauge3);

        // Sets up UI references.

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    public void btnLiga(View v){

        if (mBluetoothLeService != null) {

            //mBluetoothLeService.writeCustomCharacteristicSensor1(1);
            //mBluetoothLeService.readCustomCharacteristic();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicBattery();
                    }
                }
            };

            Thread thread2 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicTemperature();
                    }
                }
            };

            thread.start();
            thread2.start();
        }
    }

    public void btnStop(View v) {

        if (mBluetoothLeService != null) {
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristic(0);

        }
    }

    public void btnLer1(View v){
        if (mBluetoothLeService != null) {
            //mBluetoothLeService.writeCustomCharacteristic(um);
            mBluetoothLeService.writeCustomCharacteristicSensor1(1);
            mBluetoothLeService.readCustomCharacteristic();


        }
    }
    public void btnLer2(View v){
        if (mBluetoothLeService != null) {
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor2(1);
            //mBluetoothLeService.readCustomCharacteristic2();
            //mBluetoothLeService.readCustomCharacteristicBattery();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristic2();
                    }
                }
            };

            Thread thread2 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicBattery();
                    }
                }
            };

            thread.start();
            //thread2.start();


        }
    }
    public void btnLer3(View v){
        if (mBluetoothLeService != null) {
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor3(1);
            //mBluetoothLeService.readCustomCharacteristicTemperature();
            //mBluetoothLeService.readCustomCharacteristic3();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristic3();
                    }
                }
            };

            Thread thread2 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicTemperature();
                    }
                }
            };

            thread.start();
            //thread2.start();

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {


        }
    }



    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE2);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE3);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE4);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE5);
        return intentFilter;
    }

}

