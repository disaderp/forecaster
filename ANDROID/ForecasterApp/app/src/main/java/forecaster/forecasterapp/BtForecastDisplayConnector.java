package forecaster.forecasterapp;

import android.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.ParcelUuid;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.Arrays;

import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import android.util.Log;

import android.bluetooth.BluetoothClass;

import android.support.annotation.Nullable;

/**
 * This class represents Bluetooth connection with the ForecastDisplay device.
 * It handles pushing of forecast data to the device and controlling of device's acknowledgements.
 * TODO communication with the main (UI) thread
 */
    // MAYBE make it an IntentService?
    // If needed to run when app is stopped.


public class BtForecastDisplayConnector extends Fragment {
    // tag for logging
    private static final String TAG = "CONNECTOR_FRAGMENT";

    public static final UUID DISPLAY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");    // Serial Port Profile (SPP)
    public static final String DISPLAY_MAC = "98:D3:36:00:CD:A0";         // "94:E9:79:73:BC:04" //"98D3:36:CDA0"; //"98:D3:00:36:CD:A0"
    public static final String DISPLAY_NAME = "forecaster";
    // TODO input from user when connecting for the first time and save on local storage
    public static final String DISPLAY_PIN = "3637";

    // int id for enable Bluetooth Activity
    public static final int REQUEST_ENABLE_BT = 1;

    private /*final*/ BluetoothAdapter mBtAdapter;
    private BluetoothDevice mDisplayDevice;
    //private BluetoothSocket mBtSocket;
    private ConnectionThread mConnThread;

    private ConnectorCallback mListener;

    //TODO register to listen for the ACTION_STATE_CHANGED broadcast intent (BroadcastReceiver)
    //MAYBE TODO use ACTION_ACL_DISCONNECT_REQUESTED for graceful disconnection


    /** The container Activity must implement this interface.*/
    public interface ConnectorCallback {
        void onBtNotSupported(BtForecastDisplayConnector connector);
        void onConnected();
        void onConnectError(/*int/enum errorCode*/);
        void onForecastDataSent(); // needed?
        // TODO add more
    }

    // COMPAT added in API level 23 (Marshmallow), before: arg type: Activity
    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(/*Context*/Activity context) {
        Log.d(TAG, "Fragment attached, called onAttach(Activity).");
        super.onAttach(context);

        try {
            mListener = (ConnectorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ConnectorCallback.");
        }
    }
    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Fragment attached, called onAttach(Context).");
        super.onAttach(context);
        // calls onAttach(Activity)
    }

    public boolean connect() {
        // if (!connected)
        if (!mBtAdapter.isEnabled()) {
            // ask user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToDisplayDevice();
        }

        //mBtAdapter.cancelDiscovery(); // should be called before establishing connection to a remote device
        // connect to the obtained mDisplayDevice (if found)
        // MAYBE set state to connected (outside of ConnectionThread)
        // get BluetoothSocket
        // start ConnectionThread
        return true; // TEMP
    }

    private boolean connectToDisplayDevice() {
        if (mDisplayDevice == null) Log.d(TAG, "BtDevice null as expected before finding the remote device.");
        try {
            mDisplayDevice = mBtAdapter.getRemoteDevice(DISPLAY_MAC);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "findDisplayDevice: wrong MAC", ex);
            mListener.onConnectError(/*reason*/);
            return false;
        }

        if (mDisplayDevice == null) Log.e(TAG, "BtDevice not found");

        IntentFilter sdpFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        getActivity().registerReceiver(mSdpReceiver, sdpFilter);
        boolean initACLConnStarted = mDisplayDevice.fetchUuidsWithSdp();
        Log.d(TAG, "ACL connection init started (for remote services discovery)?: " + initACLConnStarted);
        return initACLConnStarted;



        /*try {
            BluetoothSocket btSocket = mDisplayDevice.createInsecureRfcommSocketToServiceRecord(DISPLAY_UUID);
            // not necessarily here:
            mConnThread = new ConnectionThread(btSocket);
            mConnThread.start();
        } catch (IOException ex) {
            Log.e(TAG, "findDisplayDevice: error establishing connection", ex);
            //e.g. BtDevice not available or insufficient permissions
            mListener.onConnectError(/*reason*//*);
            return false;
        }

        mListener.onConnected();
        return true; // TEMP
        */


        // query paired devices, as this Android device can already be paired with the Display
        /*Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Log.d(TAG, "Paired devices:");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (checkIsDisplayDevice(device)) {
                    mDisplayDevice = device;
                    return true;
                }
            }
        }
        Log.d(TAG, "End of paired devices.");*/
        // Display not already paired, so perform the device discovery

        /*
        PERFORM DISCOVERY -- REMEMBER TO REGISTER (AND THEN UNREGISTER) THE RECEIVER
        Log.d(TAG, "Performing device discovery.");
        IntentFilter broadcastFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);
        broadcastFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);
        // the BroadcastReceiver is unregistered in onStop() callback
        mBtAdapter.startDiscovery();
        // Found devices will be deliver to the registered BroadcastReceiver callback
        return true;*/
    }

    private boolean connectTemp() {
        try {
            mDisplayDevice.createBond();
            byte[] pin = new byte[]{'3','6','3','7'};
            mDisplayDevice.setPin(pin);
            BluetoothSocket btSocket = mDisplayDevice.createInsecureRfcommSocketToServiceRecord(DISPLAY_UUID);
            // not necessarily here:
            mConnThread = new ConnectionThread(btSocket);
            mConnThread.start();
        } catch (/*IO*/Exception ex) {
            Log.e(TAG, "findDisplayDevice: error establishing connection", ex);
            //e.g. BtDevice not available or insufficient permissions
            mListener.onConnectError(/*reason*/);
            return false;
        }

        //mListener.onConnected(); -- called after connecting in ConnectionThread
        return true; // TEMP
    }

    private boolean checkIsDisplayDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceMAC = device.getAddress();
        Log.d(TAG, "Device: " + deviceName + " , MAC: " + deviceMAC);
        // RUNTEST
        if (deviceName == DISPLAY_NAME) {
            // check if MAC addr is correct
            return true;
        }
        return false;
    }

    public void closeConnection() {
        Log.d(TAG, "Called to close the connection.");
        // close socket but do not remove Fragment
        // RUNTEST NOTSURE enough?
        mConnThread.close();
    }

    // byte[] data
    private void sendData(String data) {
        Log.d(TAG, "Called to send data to Display. Data: " + data);
    }

    // Fragment needs empty default constructor
    public BtForecastDisplayConnector() {
        Log.d(TAG, "Fragment ctor called.");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Fragment created.");
        super.onCreate(savedInstanceState);

        // retain this Fragment across its Activity re-creation
        setRetainInstance(true);

        if (savedInstanceState == null) {
            // ... recover saved state if any
            // only if Fragment set to be re-created with Activity re-creation
        }

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Log.i(TAG, "Device does not support Bt.");
            Toast.makeText(getActivity(), R.string.bt_not_supported, Toast.LENGTH_LONG).show();

            // tell Activity to remove this Fragment
            mListener.onBtNotSupported(this);
            return;
        }

        // debug
        Log.i(TAG, "Device supports Bt.");
        Toast.makeText(getActivity(), "Bt supported!", Toast.LENGTH_LONG).show();
    }

    //protected boolean findDisplayDevice
    @Override
    public void onStart() {
        Log.d(TAG, "Fragment started.");
        super.onStart();

        //IntentFilter broadcastFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // MAYBE: API level >23 --> getContext() ?
        //getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);

        IntentFilter pairingFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        getActivity().registerReceiver(mSdpReceiver, pairingFilter);
        getActivity().registerReceiver(mSdpReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Fragment stopped.");
        super.onStop();

        // FIXME can be null (if Bt not supported)!
        //if (mBtAdapter != null)
        //    mBtAdapter.cancelDiscovery();
        // NOTSURE unregister here, or just after connecting?
        //getActivity().unregisterReceiver(mBtDiscoveryReceiver);

        try {   // TEMP
            getActivity().unregisterReceiver(mSdpReceiver);
            Log.d(TAG, "SDP Receiver unregistered");
        } catch (IllegalArgumentException ex){ // RuntimeException
            Log.d(TAG, "SDP Receiver not registered");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "On Activity Result.");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Bt enabled.");
                //init Bt connection
                //1. search through bonded (paired) devices
                //2. if not found: conduct device discovery
                //3. get the BluetoothDevice object representing the forecast display
                //4. connect to the obtained BluetoothDevice
                connectToDisplayDevice();

            } else {
                Log.i(TAG, "Bt not enabled by user or error.");
                Toast.makeText(getActivity(), "Bt not enabled.", Toast.LENGTH_SHORT).show();
                // return; // MAYBE do not destroy this Fragment (for now)
                if (mListener == null) Log.d(TAG, "mListener is null.");
                //mListener.onBtNotSupported(this); // TEMP
                mListener.onConnectError();
            }
        }
    }

    private class ConnectionThread extends Thread {
        // tag for logging
        private static final String TAG = "CONNECTION_THREAD";

        private BluetoothSocket mBtSocket;
        private InputStream mInput;
        private OutputStream mOutput;
        private final Handler mMainHandler = new Handler(Looper.getMainLooper());

        private boolean mCancel = false;

        ConnectionThread (BluetoothSocket btSocket) {
            super();
            mBtSocket = btSocket;
        }

        @Override
        public void run() {
            // connect
            mBtAdapter.cancelDiscovery();
            try {
                mBtSocket.connect();
            } catch (IOException ex) {
                Log.e(TAG, "run: connection failure", ex);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnectError();
                    }
                });
                return;
                // TODO Handler <- also tell the Fragment (onConnectError)
            }
            //Log.d(TAG, "Connection type: " + mBtSocket.getConnectionType()); //COMPAT API level 23
            try {
                mInput = mBtSocket.getInputStream();
                mOutput = mBtSocket.getOutputStream();
            } catch (IOException ex) {
                Log.e(TAG, "run: could not obtain stream associated with bt socket", ex);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onConnectError();
                    }
                });
                return;
            }

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConnected();
                }
            });

            byte[] buffer = new byte[10];

            try {
                String msg = "ENA1;80;75;1;-5;4;3;A0;0;0;1;64;50;R";
                byte[] bmsg = msg.getBytes(Charset.defaultCharset());
                mOutput.write(bmsg);

                while (!mCancel) {
                    // state machine?
                    Log.d(TAG, "run: starting to read response");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    mInput.read(buffer);
                    String received = new String(buffer, Charset.defaultCharset());
                    Log.d(TAG, "run: read::" + received/*Arrays.toString(buffer)*/ + "::"); // NOTSURE will it work? RUNTEST
                    // TODO Handler <- print message in Activity

                    break;  // TEMP
                }
            } catch (IOException ex) {
                Log.e(TAG, "run: I/O error occurred or stream closed", ex);
                return;
            }
            Log.d(TAG, "run: got cancel request");
            close();
            Log.d(TAG, "Closed connection");
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConnectError();
                }
            }); // mocne TEMP
        }

        //NOTSURE or boolean?
        public void cancel() {
            //break loop in run()
            mCancel = true;
        }

        public void close() {
            try {
                mBtSocket.close();
                // NOTSURE close streams?
            } catch (IOException ex) {
                Log.e(TAG, "close: fail", ex);
                ex.printStackTrace();
            }
        }
    }

    // NOTSURE divide into a couple of methods?
    // FUTURE change String to ForecastData (Serializable) object
    /*public void sendForecastData(String forecastData) {
        // NOTSURE
        // Ask user to enable Bluetooth if not already enabled.
        // If it was already enabled - dispatch send request to the ConnectionThread.
        // When enabled by user - send request will be dispatched to the ConnectionThread in the onActivityResult callback.
        if (!mBtAdapter.isEnabled()) {
            // ask user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // TODO also check if connected!
            // TEMP

            sendData(forecastData);
        }
    }*/

    private final BroadcastReceiver mSdpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_UUID.equals(action)) {
                Log.d(TAG, "Entered SDP Receiver");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "SDP Receiver: got device");
                //ParcelUuid[] uuids = (ParcelUuid[]) intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                //ArrayList<ParcelUuid> uuids = null;
                ParcelUuid[] uuids = null;
                try {
                    //uuids = intent.getParcelableArrayListExtra(BluetoothDevice.EXTRA_UUID);
                    Parcelable[] extraUuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if (extraUuids != null)
                        uuids = Arrays.copyOf(extraUuids, extraUuids.length, ParcelUuid[].class);
                } catch (ClassCastException ex) {
                    Log.e(TAG, "We've got a problem, sir!", ex);
                } catch (Exception ex) {
                    Log.e(TAG, "Something bad happened..", ex);
                }
                Log.d(TAG, "SDP Receiver: got UUIDs");
                Log.d(TAG, "SDP Receiver: Is received?: device: " + (device!=null) + " , uuids: " + (uuids!=null));
                Log.d(TAG, "Fetched remote device MAC: " + device.getAddress());
                if (uuids != null) {
                    Log.d(TAG, "Fetched remote services UUIDs: " + uuids.length);
                    for (ParcelUuid uuid : uuids)
                        Log.d(TAG, "UUID: " + uuid.toString());
                }

                // TODO unregisterReceiver <- now in onPause callback
                // be careful: http://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android
                try {   // TEMP
                    getActivity().unregisterReceiver(this);
                    Log.d(TAG, "SDP Receiver unregistered");
                } catch (IllegalArgumentException ex) { // RuntimeException
                    Log.d(TAG, "SDP Receiver not registered");
                }

                if (uuids == null)
                    mListener.onConnectError(/*reason : service UUID not found or remote device unreachable*/);
                else
                    // TODO NOTE: can be cached UUIDs !!
                    connectTemp();
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.d(TAG, "Entered Pairing Request Receiver");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // TODO? add device.equals(mDisplayDevice) ? <-- can device be null??
                int pin = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, 0);
                //the pin in case you need to accept for an specific pin
                Log.d(TAG, "PIN: " + pin);

                Log.d(TAG, "Bonded: " + device.getAddress());
                byte[] pinBytes;
                try {
                    pinBytes = ("" + pin).getBytes("UTF-8");
                    boolean pinSet = device.setPin(pinBytes);

                    Log.d(TAG, "Is PIN set?: " + pinSet);
                } catch(UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }

                //setPairing confirmation if neeeded
                //device.setPairingConfirmation(true);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                Log.d(TAG, "Bond state: " + state + " , device: " + device.getAddress());
            }
        }
    };


    /*
    private final BroadcastReceiver mBtDiscoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass deviceClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);

                // TEMP
                Log.d(TAG, "Discovered device class: Major: " + deviceClass.getMajorDeviceClass()
                        + " minor: " + deviceClass.getDeviceClass());

                /*if (checkIsDisplayDevice(device)) {
                    mDisplayDevice = device;
                    mBtAdapter.cancelDiscovery();
                    // NOTSURE would it work correctly?
                    //getActivity().unregisterReceiver(this);
                }*//*
                String deviceName = device.getName();
                String deviceMAC = device.getAddress();
                Log.d(TAG, "Device: " + deviceName + " , MAC: " + deviceMAC);


            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Device discovery started.");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Device discovery finished.");
            }
        }
    };
    */

    // callback logging for debugging:

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Fragment's view (not) created.");
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Fragment's activity created.");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Fragment resumed.");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Fragment paused.");
        super.onPause();

        /*try {   // TEMP
            getActivity().unregisterReceiver(mSdpReceiver);
            Log.d(TAG, "SDP Receiver unregistered");
        } catch (IllegalArgumentException ex) { // RuntimeException
            Log.d(TAG, "SDP Receiver not registered");
        }*/
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "Fragment's view destroyed.");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Fragment destroyed.");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "Fragment detached.");
        super.onDetach();
    }
}
