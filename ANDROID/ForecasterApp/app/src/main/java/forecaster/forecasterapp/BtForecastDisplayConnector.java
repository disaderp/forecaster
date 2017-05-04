package forecaster.forecasterapp;

import android.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.os.Bundle;

import java.util.UUID;
import java.util.Set;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * This class represents Bluetooth connection with the ForecastDisplay device.
 * It handles pushing of forecast data to the device and controlling of device's acknowledgements.
 * TODO communication with the main (UI) thread
 */
    // MAYBE make it an IntentService?
    // If needed to run when app is stopped.


public class BtForecastDisplayConnector extends Fragment {
    // tag for debug logging
    private static final String TAG = "CONNECTOR_FRAGMENT";

    public static final UUID DISPLAY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String DISPLAY_MAC = "98D3:36:CDA0"; // probably wrong (too short)
    public static final String DISPLAY_NAME = "forecaster";
    public static final String DISPLAY_PIN = "3637";

    // int id for enable Bluetooth Activity
    public static final int REQUEST_ENABLE_BT = 1;

    private /*final*/ BluetoothAdapter mBtAdapter;
    private BluetoothDevice mDisplayDevice;

    private ConnectorCallback mListener;

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

                if (checkIsDisplayDevice(device)) {
                    mDisplayDevice = device;
                    mBtAdapter.cancelDiscovery();
                    // NOTSURE would it work correctly?
                    //getActivity().unregisterReceiver(this);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Device discovery started.");
            }
        }
    }; //TODO also register to listen for the ACTION_STATE_CHANGED broadcast intent


    /** The container Activity must implement this interface.*/
    public interface ConnectorCallback {
        void onBtNotSupported(BtForecastDisplayConnector connector);
        // TODO add more
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Fragment attached.");
        super.onAttach(context);

        try {
            mListener = (ConnectorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ConnectorCallback.");
        }
    }

    public boolean connect() {
        // if (!connected)
        if (!mBtAdapter.isEnabled()) {
            // ask user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mBtAdapter.cancelDiscovery(); // should be called before establishing connection to a remote device
        // connect to the obtained mDisplayDevice (if found)
        // MAYBE set state to connected (outside of ConnectionThread)
        // get BluetoothSocket
        // start ConnectionThread
        return true; // TEMP
    }

    private boolean findDisplayDevice() {
        // query paired devices, as this Android device can already be paired with the Display
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (checkIsDisplayDevice(device)) {
                    mDisplayDevice = device;
                    return true;
                }
            }
        }

        if (mDisplayDevice != null) return true; // TEMP

        // Display not already paired, so perform the device discovery
        Log.d(TAG, "Performing device discovery.");

        IntentFilter broadcastFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);

        broadcastFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);
        // the BroadcastReceiver is unregistered in onStop() callback

        mBtAdapter.startDiscovery();
        // Found devices will be deliver to the registered BroadcastReceiver callback

        return false; // TEMP
    }

    private boolean checkIsDisplayDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceMAC = device.getAddress();
        Log.d(TAG, "Paired device: " + deviceName + " , MAC: " + deviceMAC);
        // RUNTEST
        if (deviceName == DISPLAY_NAME) {
            // check if MAC addr is correct
            return true;
        }
        return false;
    }

    public void closeConnection() {
        Log.d(TAG, "Called to close the connection.");
        // TODO close socket but do not remove Fragment
    }

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

        IntentFilter broadcastFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // MAYBE: API level >23 --> getContext() ?
        getActivity().registerReceiver(mBtDiscoveryReceiver, broadcastFilter);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Fragment stopped.");
        super.onStop();

        mBtAdapter.cancelDiscovery();
        // NOTSURE unregister here, or just after connecting?
        getActivity().unregisterReceiver(mBtDiscoveryReceiver);
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

            } else {
                Log.i(TAG, "Bt not enabled by user or error.");
                Toast.makeText(getActivity(), "Bt not enabled.", Toast.LENGTH_SHORT).show();
                // tell Activity to finish (remove) this Fragment
                mListener.onBtNotSupported(this);
            }
        }
        //TODO also register to listen for the ACTION_STATE_CHANGED broadcast intent
    }

    // TODO
    private /*static*/ class ConnectionThread extends Thread {
        @Override
        public void run() {
            //whole logic
        }

        //NOTSURE or boolean?
        public void cancel() {
            //break loop in run()
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
