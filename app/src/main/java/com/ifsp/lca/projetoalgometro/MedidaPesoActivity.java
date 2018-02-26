/*
* Aplicativo feito para integração com módulo BLE pertencente ao LCA - IFSP (desenvolvido por Carlos Eduardo Palmieri Teixeira).
*
* Aplicativo desenvolvido por Giovanni Antunes Bonin
*
* 2017
*
* */

package com.ifsp.lca.projetoalgometro;

import android.*;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MedidaPesoActivity extends Activity {

    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic, temp, r;
    private BluetoothGatt mBluetoothGatt;

    private boolean ativa_data = true;
    private String date, date2, date3;
    private DateFormat data, data2, data3;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public boolean para1 = true, para2 = false, para3 = true;

    private EditText EditPeso, EditNameArq;
    private TextView txtMedida;
    private Button btnMedir, btnFinalizar;

    private int countMedida = 0;
    private boolean flag;

    private String s;

    private FileWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medida_peso);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //EditPeso = (EditText) findViewById(R.id.EditPeso);
        txtMedida = (TextView) findViewById(R.id.txtMedida);
        EditNameArq = (EditText) findViewById(R.id.EditNameArq);
        btnMedir = (Button) findViewById(R.id.btnMedir);
        btnFinalizar = (Button) findViewById(R.id.btnFinalizar);

        btnFinalizar.setEnabled(false);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public void btnMedir (View v){

        if (mBluetoothLeService != null) {
            para1 = true;
            //countMedida++;
        try {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TxtMedicoesValores");
            if (!root.exists()) {
                root.mkdirs();
            }
            //File gpxfile = new File(root, "EMG-SAVE - ALL" + ".txt");
            String nomeArq = EditNameArq.getText().toString();
            File gpxfile = new File(root, nomeArq + ".txt");
            //File gpxfile2 = new File(root, nomeArq + " - RMS.txt");
            if(gpxfile.exists()){
                gpxfile = new File(root, nomeArq + ".txt");
                //gpxfile2 = new File(root, nomeArq + " - RMS.txt");
            }
            writer = new FileWriter(gpxfile, true);
            //writer = new FileWriter(gpxfile2, true);

        } catch (IOException e) {
            Log.e("Estado:", "NAO DEU!");
        }


            mBluetoothLeService.readCustomCharacteristic();
            btnMedir.setEnabled(false);
            btnFinalizar.setEnabled(true);

        }
    }

    public void btnFinalizar (View v){
        try {
            para1 = para2 = false;
            ativa_data = true;
            writer.close();
            Toast.makeText(getApplication(), "As medidas foram concluídas!", Toast.LENGTH_LONG).show();
            btnMedir.setEnabled(true);
            btnFinalizar.setEnabled(false);
            EditNameArq.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
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

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //txtStatus.setTextColor(Color.GREEN);
                //txtStatus.setText("Conectado");

                //ConnEquip.setTextColor(Color.parseColor("#00FF00"));
                //ConnEquip.setText(mDeviceName);
                updateConnectionState(R.string.connected);
                //invalidateOptionsMenu();
                //mBluetoothLeService.readCustomCharacteristicBattery();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //txtStatus.setTextColor(Color.RED);
                //txtStatus.setText("Desconectado");
                //ConnEquip.setTextColor(Color.parseColor("#FF0000"));
                //ConnEquip.setText("Nenhum");
                updateConnectionState(R.string.disconnected);
                //invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action) && para1) {
                mBluetoothLeService.readCustomCharacteristic2();


                if(ativa_data){
                    ativa_data = false;
                    data = new SimpleDateFormat("ss.SSS");
                    data2 = new SimpleDateFormat("mm");
                    data3 = new SimpleDateFormat("HH");

                }
                date = data.format(Calendar.getInstance().getTime());
                date2 = data2.format(Calendar.getInstance().getTime());
                date3 = data3.format(Calendar.getInstance().getTime());


                s = "";
                s = String.valueOf(countMedida)+",Sensor1," + intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                s = s + ",t," + date3 + "," + date2 + "," + date;
                Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show();

                /*try {
                    writer.write("Sensor1," + String.valueOf(countMedida)+ "," + (intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+"\n"));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                para1 = false;
                para2 = true;

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action) && para2 /*&& !s.equals("")*/) {



                try {
                    date = data.format(Calendar.getInstance().getTime());
                    date2 = data2.format(Calendar.getInstance().getTime());
                    String ss = s + ",Sensor2,"+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA2) + ",t," + date3 + "," + date2 + "," + date;
                    Toast.makeText(getApplication(), ss , Toast.LENGTH_SHORT).show();
                    writer.write(ss + "\n");
                    writer.flush();
                    countMedida++;
                    txtMedida.setText(String.valueOf(countMedida));
                    Toast.makeText(getApplication(), "Escreveu!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                para2 = false;
                para1 = true;

            }
        }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
        //txt_temp_rest.setText("--:--");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);

        }

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
        startActivity(intent);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
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
