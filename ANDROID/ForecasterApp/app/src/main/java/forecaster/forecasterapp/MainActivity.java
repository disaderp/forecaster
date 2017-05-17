package forecaster.forecasterapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.support.annotation.Nullable;

import android.util.Log;

/**
 * The main screen of the ForecasterApp.
 */

public class MainActivity extends Activity implements BtForecastDisplayConnector.ConnectorCallback {
    // tag for debug logging
    private static final String TAG = "MAIN_ACTIVITY";

    // keys for saving the component's state
    private static final String STATE_UPDATE_IN_PROGRESS = "updating_forecast";
    private static final String STATE_SEND_IN_PROGRESS = "sending_forecast";
    private static final String STATE_SEND_DISABLED = "send_disabled";

    // Connector Fragment reference
    private static final String CONNECTOR_TAG = "BT_CONNECTOR_FRAGMENT";
    private BtForecastDisplayConnector mConnectorFragment = null;

    // references to widgets
    private TextView mViewLastUpdatedText;
    private Button mViewSendBtn;
    private Button mViewUpdateBtn;

    // component's state
    // MAYBE change to enum
    private boolean mStateForecastUpdating = false;
    private boolean mStateForecastSending = false;
    private boolean mStateSendDisabled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Activity created.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references to widgets
        mViewLastUpdatedText = (TextView) findViewById(R.id.lastUpdateText);
        mViewUpdateBtn = (Button) findViewById(R.id.updateBtn);
        mViewSendBtn = (Button) findViewById(R.id.sendBtn);

        mConnectorFragment = (BtForecastDisplayConnector) getFragmentManager().findFragmentByTag(CONNECTOR_TAG);
        if (mConnectorFragment == null) Log.d(TAG, "ConnectorFragment is null");

        if (savedInstanceState == null) {
            // if component not recreated after its process have been killed by the system
        } else {
            Log.d(TAG, "Loaded previous instance state.");
        }
        // TODO fetch last update date from local storage
        mViewLastUpdatedText.append(" Never");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_UPDATE_IN_PROGRESS, mStateForecastUpdating);
        outState.putBoolean(STATE_SEND_IN_PROGRESS, mStateForecastSending);
        outState.putBoolean(STATE_SEND_DISABLED, mStateSendDisabled);

        super.onSaveInstanceState(outState);
    }

    // called after onStart()
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // NOTSURE in this case the default impl is unnecessary?
        super.onRestoreInstanceState(savedInstanceState);

        mStateForecastUpdating = savedInstanceState.getBoolean(STATE_UPDATE_IN_PROGRESS);
        mStateForecastSending = savedInstanceState.getBoolean(STATE_SEND_IN_PROGRESS);
        mStateSendDisabled = savedInstanceState.getBoolean(STATE_SEND_DISABLED);
        // RUNTEST add default value?

        if (mStateForecastUpdating) {
            mViewUpdateBtn.setText(R.string.updating_btn);
        }
        if (mStateForecastSending) {
            mViewSendBtn.setText(R.string.sending_btn);
        }
        if (mStateSendDisabled) {
            mViewSendBtn.setEnabled(false);
        }
    }

    public void updateForecast(View view) {
        Log.d(TAG, "Update button pressed.");
        // TODO add checking if necessary
        mStateForecastUpdating = true;

        mViewUpdateBtn.setText(R.string.updating_btn);
        //delegate the processing to the background...
    }

    public void sendFetchedForecastToDisplay(View view) {
        mViewSendBtn.setEnabled(false);
        mStateSendDisabled = true;
        Log.d(TAG, "Send button pressed.");

        if (!mStateForecastSending && mConnectorFragment == null) {
            // TODO (lazy init) start the BtForecastDisplayConnector Fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(new BtForecastDisplayConnector(), CONNECTOR_TAG);
            // ctor call <- destroys if bt not supported!
            fragmentTransaction.commit();
            // COMPAT used instead of fragmentTransaction.commitNow() (API level 24)
            boolean arePending = fragmentManager.executePendingTransactions();
            Log.d(TAG, "Are there any pending transactions?: " + arePending);
            // RUNTEST or commitNow?
            mConnectorFragment = (BtForecastDisplayConnector) fragmentManager.findFragmentByTag(CONNECTOR_TAG);

            // DEBUG
            if (mConnectorFragment == null) Log.d(TAG, "FragmentTransaction has not been committed yet.");
        }

        // FIXME check if Fragment has been created! (if transaction was committed)

        if (mConnectorFragment == null) {
            // TODO deactivate DisplayBtConnection feature (degrade gracefully)
            // TODO deactivate button
            //mViewSendBtn.setEnabled(false);
            //setClickable
            //setActivated
            //setSelected
            Log.d(TAG, "Got BtNotSupported. Graceful degradation (TODO).");
            return;
        }

        // TODO add checking if necessary
        mStateForecastSending = true;
        mViewSendBtn.setText(R.string.sending_btn);
        // TODO change the state after finishing sending

        // TODO add checking
        mConnectorFragment.connect();
    }

    // ConnectorCallback methods:
    @Override
    public void onBtNotSupported(BtForecastDisplayConnector connector) {
        Log.d(TAG, "onBtNotSupported callback from ConnectorFragment.");
        getFragmentManager().beginTransaction().remove(connector).commit();

        if (mConnectorFragment == null) Log.d(TAG, "mConnectorFragment is still null.");
        // DEBUG
        /*if (connector != mConnectorFragment) {
            throw new AssertionError("ConnectorFragment mismatch error.");
        }*/

        mConnectorFragment = null;
        // leave state sendDisabled as true
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected callback from ConnectorFragment.");
        //mConnectorFragment.send(data);
    }

    @Override
    public void onConnectError() {
        Log.d(TAG, "onConnectError callback from ConnectorFragment.");

        mStateForecastSending = false;
        mViewSendBtn.setText(R.string.send_btn);
        mViewSendBtn.setEnabled(true);
        mStateSendDisabled = false;
    }

    @Override
    public void onForecastDataSent() {
        Log.d(TAG, "onForecastDataSent callback from ConnectorFragment.");

        mStateForecastSending = false;
        mViewSendBtn.setText(R.string.send_btn);
        mViewSendBtn.setEnabled(true);
        mStateSendDisabled = false;
    }




    // callback logging for debugging:

    @Override
    protected void onStart() {
        Log.d(TAG, "Activity started.");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "Activity restarted.");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed.");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Activity paused.");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Activity stopped.");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity destroyed.");
        super.onDestroy();
    }
}
