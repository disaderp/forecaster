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

        super.onSaveInstanceState(outState);
    }

    // called after onStart()
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // NOTSURE in this case the default impl is unnecessary?
        super.onRestoreInstanceState(savedInstanceState);

        mStateForecastUpdating = savedInstanceState.getBoolean(STATE_UPDATE_IN_PROGRESS);
        mStateForecastSending = savedInstanceState.getBoolean(STATE_SEND_IN_PROGRESS);
        // RUNTEST add default value?

        if (mStateForecastUpdating) {
            mViewUpdateBtn.setText(R.string.updating_btn);
        }
        if (mStateForecastSending) {
            mViewSendBtn.setText(R.string.sending_btn);
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
        Log.d(TAG, "Send button pressed.");

        if (!mStateForecastSending && mConnectorFragment == null) {
            // TODO (lazy init) start the BtForecastDisplayConnector Fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(new BtForecastDisplayConnector(), CONNECTOR_TAG);
            fragmentTransaction.commit();
            // RUNTEST or commitNow?
        }

        // TODO add checking if necessary
        mStateForecastSending = true;
        mViewSendBtn.setText(R.string.sending_btn);
        // TODO change the state after finishing sending
    }

    // ConnectorCallback methods:
    @Override
    public void onBtNotSupported(BtForecastDisplayConnector connector) {
        getFragmentManager().beginTransaction().remove(connector).commit();
        // TODO set reference to ConnectorFragment to null
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
