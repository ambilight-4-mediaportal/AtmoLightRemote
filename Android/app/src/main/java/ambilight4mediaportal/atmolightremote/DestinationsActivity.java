package ambilight4mediaportal.atmolightremote;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DestinationsActivity extends Activity  implements MultiSpinner.MultiSpinnerListener {

    private static final String TAG = "REMOTE_DESTINATIONS";
    private MultiSpinner multiSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        multiSpinner = (MultiSpinner) findViewById(R.id.multispinner_Devices);

        List<String> items = new ArrayList<>();
        items.add("AmbiBox");
        items.add("AtmoOrb");
        items.add("AtmoWin");
        items.add("BobLight");
        items.add("Hue");
        items.add("Hyperion");

        multiSpinner.setItems(items, "Select devices", this);
    }

    private void CreateSpinnerContent()
    {
    }

    @Override
    public void onItemsSelected(boolean[] selected) {

    }
}