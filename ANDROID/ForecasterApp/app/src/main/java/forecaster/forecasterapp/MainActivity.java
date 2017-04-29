package forecaster.forecasterapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.support.annotation.Nullable;

/**
 * The main screen of the ForecasterApp.
 */

public class MainActivity extends Activity {

    private TextView mLastUpdated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLastUpdated = (TextView) findViewById(R.id.lastUpdateText);

        //TODO fetch last update date from local storage
        mLastUpdated.append(" Now");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //activity's logic goes here
    }

    public void updateForecast(View view) {
        final Button button = (Button) findViewById(R.id.updateBtn);
        //TODO catch ClassCastException

        button.setText(R.string.updating_btn);
        //delegate the processing to the background...
    }
}
