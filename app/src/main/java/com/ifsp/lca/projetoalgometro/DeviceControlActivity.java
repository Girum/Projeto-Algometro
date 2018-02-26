/*
* Aplicativo feito para integração com módulo BLE pertencente ao LCA - IFSP (desenvolvido por Carlos Eduardo Palmieri Teixeira).
*
* Aplicativo desenvolvido por Giovanni Antunes Bonin
*
* 2017
*
* */

package com.ifsp.lca.projetoalgometro;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String tst, tstValue;

    private TextView txtBat, txtTemp, txtVb, txtSP1, txtSP2, txtSP3, txtStatus, txtRef;
    private float n,o,p;
    private int count = 0, count2 = 0, count3 = 0;
    private float[] Sensor1 = new float[3];
    private float[] Sensor2 = new float[3];
    private float[] Sensor3 = new float[3];
    private String[] horaSensor1 = new String[3];
    private String[] horaSensor2 = new String[3];
    private String[] horaSensor3 = new String[3];

    private boolean state1 = false;

    private ImageView imGauge, imGauge2, imGauge3, imageViewBatt;

    private float tref = (float) 1.0;

    private EditText EditNome;

    private boolean para1 = true, para2 = true, para3 = true;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

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
           /* if(mBluetoothLeService != null){
            mBluetoothLeService.readCustomCharacteristicBattery();
            }*/

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
            DateFormat hour = new SimpleDateFormat("HH:mm:ss");

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                txtStatus.setTextColor(Color.GREEN);
                txtStatus.setText("Conectado");

                if (mBluetoothLeService != null) {
                    //mBluetoothLeService.writeCustomCharacteristic(um);
                    //mBluetoothLeService.writeCustomCharacteristicSensor1(1);
                    para1 = true;

                    Thread thread = new Thread() {
                        @Override
                        public void run() {

                            mBluetoothLeService.readCustomCharacteristic();

                        }
                    };

                    thread.start();

                }

                //ConnEquip.setTextColor(Color.parseColor("#00FF00"));
                //ConnEquip.setText(mDeviceName);
                updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
               //mBluetoothLeService.readCustomCharacteristicBattery();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                txtStatus.setTextColor(Color.RED);
                txtStatus.setText("Desconectado");
                //ConnEquip.setTextColor(Color.parseColor("#FF0000"));
                //ConnEquip.setText("Nenhum");
                updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action) && para1){
                //para1 = false;
                txtSP1.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                n = intent.getFloatExtra("val",0);
                /*count++;
                Sensor1[count-1] = n;

                horaSensor1[count-1] = hour.format(Calendar.getInstance().getTime());
                if(count == 3){
                    count = 0;
                    tst = "Teste1";
                    Query l = database.getReference().child(String.valueOf(EditNome.getText())).child("Sensor1").orderByKey().limitToLast(1);

                    l.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        // This method is called once with the initial value and again
                                                        // whenever data at this location is updated.
                                                        tstValue = dataSnapshot.getKey();
                                                        Log.d(TAG, "VALUE ISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS: " + tstValue);
                                                    }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });


                    DateFormat date = new SimpleDateFormat("dd-MM-yyyy");
                    String data = date.format(Calendar.getInstance().getTime());



                    Toast.makeText(getApplication(), "Enviando dados Sensor1", Toast.LENGTH_LONG).show();
                    myRef = database.getReference().child(String.valueOf(EditNome.getText())).child("Sensor1").child(tst).child(String.valueOf(data));
                    for(int i = 0; i < 3; i++) {

                        String hora = horaSensor1[i];
                        myRef.child(hora).setValue("Medida " + (i+1) + ": " + String.valueOf(Sensor1[i]));

                    }

                }*/

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;
                        float dd = n;

                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar(imGauge, (n*100)/tref);
                    }
                });

            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action) && para2){
                //para2 = false;
                txtSP2.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));
                o = intent.getFloatExtra("val2",0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;


                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar2(imGauge2, (o*100)/tref);
                    }
                });
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE3.equals(action) && para3){
                //para3 = false;
                txtSP3.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA3));
                p = intent.getFloatExtra("val3",0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float t = 3.21f;


                        //circularImageBar(imGauge,((t-dd)*(100/t)));
                        circularImageBar3(imGauge3, (p*100)/tref);
                    }
                });
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE4.equals(action)){
                txtRef.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA4));

                //float value = Float.parseFloat(intent.getStringExtra(BluetoothLeService.EXTRA_DATA4));
                String v = intent.getStringExtra(BluetoothLeService.EXTRA_DATA4);
                float valueFinal = Float.parseFloat(v.replace(" V\n", ""));
                tref = valueFinal;

                if(valueFinal <2.5){
                    txtRef.setTextColor(Color.RED);
                }else{
                    txtRef.setTextColor(Color.GREEN);
                }

            }else if(BluetoothLeService.ACTION_DATA_AVAILABLE5.equals(action)){
                //txtBat.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA4));
                txtVb.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA5));


                String bat = intent.getStringExtra(BluetoothLeService.EXTRA_DATA5);

                int value = Integer.parseInt(bat.replace("%\n",""));

                if(value > 50){
                    imageViewBatt.setImageResource(R.drawable.batteryalgometrofull);

                }
                if(value <50 && value > 25){
                    imageViewBatt.setImageResource(R.drawable.batteryalgometromedium);
                }
                if(value < 25){
                    imageViewBatt.setImageResource(R.drawable.batteryalgometrolow);
                }
            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE6.equals(action)){
                //txtTemp.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA5));
            }



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
        paint.setColor(Color.parseColor("#FF0000"));
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

    private void circularImageBar2(ImageView iv2, float i) {

        Bitmap b = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();



        paint.setColor(Color.parseColor("#c4c4c4"));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(250, 250, 220, paint);
        paint.setColor(Color.parseColor("#00FF00"));
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.button_control);
        setContentView(R.layout.activity_device_control);

        Toast.makeText(getApplication(), "OI", Toast.LENGTH_SHORT).show();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Toast.makeText(getApplication(), "Name: " +mDeviceName + " Address: " + mDeviceAddress, Toast.LENGTH_SHORT).show();


        //txtBat = (TextView) findViewById(R.id.txtBat);
        //txtTemp = (TextView) findViewById(R.id.txtTemp);

        imGauge = (ImageView) findViewById(R.id.imGauge);
        imGauge2 = (ImageView) findViewById(R.id.imGauge2);
        imGauge3 = (ImageView) findViewById(R.id.imGauge3);

        imageViewBatt = (ImageView) findViewById(R.id.imageViewBatt);

        txtVb = (TextView) findViewById(R.id.txtVb);

        txtSP1 = (TextView) findViewById(R.id.txtSP1);
        txtSP2 = (TextView) findViewById(R.id.txtSP2);
        txtSP3 = (TextView) findViewById(R.id.txtSP3);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        txtRef = (TextView) findViewById(R.id.txtRef);


        EditNome = (EditText) findViewById(R.id.EditNome);



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
            //mBluetoothLeService.writeCustomCharacteristicSensor1(1);
            para1 = true;

            Thread thread = new Thread() {
                @Override
                public void run() {

                        mBluetoothLeService.readCustomCharacteristic();

                }
            };

            thread.start();

        }
    }

    public void btnLer11 (View v){
        if (mBluetoothLeService != null) {
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor1(1);
            para1 = true;


            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(para1) {
                        mBluetoothLeService.readCustomCharacteristic();

                    }
                }
            };

            thread.start();


        }
    }
    public void btnLer2(View v){
        if (mBluetoothLeService != null) {
            para2 = true;
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor2(1);
            //mBluetoothLeService.readCustomCharacteristic2();
            //mBluetoothLeService.readCustomCharacteristicBattery();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(para2) {
                        mBluetoothLeService.readCustomCharacteristic2();
                    }
                }
            };


            thread.start();
            //thread2.start();


        }
    }

    public void btnLer12(View v){
        if (mBluetoothLeService != null) {
            para2 = true;
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor2(1);
            //mBluetoothLeService.readCustomCharacteristic2();
            //mBluetoothLeService.readCustomCharacteristicBattery();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(para2) {
                        mBluetoothLeService.readCustomCharacteristic2();
                    }
                }
            };


            thread.start();
            //thread2.start();


        }
    }
    public void btnLer3(View v){
        if (mBluetoothLeService != null) {
            para3 = true;
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor3(1);
            //mBluetoothLeService.readCustomCharacteristicTemperature();
            //mBluetoothLeService.readCustomCharacteristic3();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(para3) {
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

    public void btnLer13(View v){
        if (mBluetoothLeService != null) {
            para3 = true;
            //mBluetoothLeService.writeCustomCharacteristic(um);
            //mBluetoothLeService.writeCustomCharacteristicSensor3(1);
            //mBluetoothLeService.readCustomCharacteristicTemperature();
            //mBluetoothLeService.readCustomCharacteristic3();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(para3) {
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

    public void btnP1(View v){
        if (mBluetoothLeService != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicP1();
                    }
                }
            };
            thread.start();
        }
        }
    public void btnP2(View v){
        if (mBluetoothLeService != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicP2();
                    }
                }
            };

            thread.start();
        }
    }
    public void btnP3(View v){
        if (mBluetoothLeService != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicP3();
                    }
                }
            };
            thread.start();

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

    public void btnTudo(View v){
        if (mBluetoothLeService != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristic();
                    }
                }
            };
            Thread thread2 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristic2();
                    }
                }
            };
            Thread thread3 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristic3();
                    }
                }
            };
            Thread thread4 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicBattery();
                    }
                }
            };
            Thread thread5 = new Thread() {
                @Override
                public void run() {
                    while(true) {
                        mBluetoothLeService.readCustomCharacteristicRef();
                    }
                }
            };

            thread.start();
            thread2.start();
            thread3.start();
            thread4.start();
            thread5.start();

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

    @Override
    public void onBackPressed(){
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
        Intent intent = new Intent(this, DeviceScanActivity.class);
    }

    /*public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);

                //mBluetoothLeService.readCustomCharacteristicBattery();

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
    }*/


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

