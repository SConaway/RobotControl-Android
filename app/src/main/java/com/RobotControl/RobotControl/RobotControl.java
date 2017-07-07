package com.RobotControl.RobotControl;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class RobotControl extends ActionBarActivity {

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Button btnFor, btnStop, btnRev, btnRight, btnLeft, btnLED, btnOBJA, btnDis;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the RobotControl
        setContentView(R.layout.activity_robot_control);

        //call the widgets
        btnFor = (Button) findViewById(R.id.forward_button);
        btnStop = (Button) findViewById(R.id.stop_button);
        btnRev = (Button) findViewById(R.id.reverse_button);
        btnRight = (Button) findViewById(R.id.right_button);
        btnLeft = (Button) findViewById(R.id.left_button);
        btnLED = (Button) findViewById(R.id.led_button);
        btnOBJA = (Button) findViewById(R.id.obja_button);
        btnDis = (Button) findViewById(R.id.disconnect_button);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { Forward();    } }); //method to go forward

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stop();   //method to stop
            }
        });

        btnRev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  Reverse(); Stop(); } }); //method to reverse then stop

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  Right(); Stop();  //method to turn right then stop
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  Left(); Stop();  //method to turn left then stop
            }
        });

        btnLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LED();   //method to turn LED ON/OFF
            }
        });

        btnOBJA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  OBJA();   } }); //method to enter object avoidance mode

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  Stop();  Disconnect();  } }); // method to stop and disconnect

    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }

    private void Stop() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("s".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Forward() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("f".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Reverse() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("b".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }


    private void Right() {
        if (btSocket != null) {
            try {
                    btSocket.getOutputStream().write("r".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Left() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("l".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void LED() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("A".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void OBJA() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("o".getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> { // UI thread
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(RobotControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {

                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {

                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
