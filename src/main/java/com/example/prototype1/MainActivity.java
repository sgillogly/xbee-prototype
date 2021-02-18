package com.example.prototype1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import java.io.IOException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class MainActivity extends AppCompatActivity {

    PendingIntent permissionIntent;
    UsbDevice device;
    FT_Device ftDev = null;
    D2xxManager ftD2xx;
    Context serial_context;
    UsbManager manager;
    UsbDeviceConnection connection;
    Button button;
    TextView textView;
    String petLocationPractice;
    Toast toast;
    int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public static final int readLength = 20;

    private static final String ACTION_USB_PERMISSION = "com.example.prototype1.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        if(device != null){
                            textView.setText("did it");
                            try{
                                ftD2xx = D2xxManager.getInstance(serial_context);
                                ftD2xx.createDeviceInfoList(serial_context);

                                ftDev = ftD2xx.openByIndex(serial_context, 0);

                                toast = Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT);
                                toast.show();

                                ftDev.setBaudRate(9600);

                                iavailable = ftDev.getQueueStatus();

                                if(iavailable > 0){
                                    if(iavailable > readLength){
                                        iavailable = readLength;
                                    }
                                }
                                readData = new byte[readLength];
                                readDataToText = new char[readLength];

                                ftDev.read(readData, iavailable);
                                for(int i = 0; i < iavailable; i++){
                                    readDataToText[i] = (char) readData[i];
                                }

                                toast = Toast.makeText(getApplicationContext(), Arrays.toString(readDataToText), Toast.LENGTH_SHORT);
                                toast.show();
                                String petLocation = new String(readDataToText);
                                textView.setText(petLocation);
                            } catch (D2xxManager.D2xxException e) {
                                e.printStackTrace();
                                toast = Toast.makeText(getApplicationContext(), "cannot connect", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }
                    else{
                        textView.setText("didn't work");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button2);

        serial_context = getApplicationContext();

        textView = findViewById(R.id.textView);
        //petLocationPractice = "42.367421 -63.056742";
        //textView.setText(petLocationPractice);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }

    public void onClick(View v){
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            device = deviceIterator.next();
            textView.setText(device.getDeviceName());
        }
        if(device!=null){
            manager.requestPermission(device, permissionIntent);
            connection = manager.openDevice(device);
            //run thread task
        }

        /*String[] arrayPetLoc = petLocationPractice.split(" ");
        double latitude = Double.parseDouble(arrayPetLoc[0]);
        double longitude = Double.parseDouble(arrayPetLoc[1]);*/
        //LatLng petLocation = new LatLng(latitude, longitude);
    }

}