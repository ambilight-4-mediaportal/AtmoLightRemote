package ambilight4mediaportal.atmolightremote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {
    Context mContext;
    SharedPreferences mPrefs;
    SharedPreferences.Editor editor;
    private CheckBox atmoWinEffects;
    private CheckBox gifEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Settings
        mContext = getApplicationContext();
        mPrefs = mContext.getSharedPreferences("AtmoLightRemotePrefs", Context.MODE_PRIVATE);

        atmoWinEffects=(CheckBox)findViewById(R.id.chkAtmoWinEffects);
        gifEffect=(CheckBox)findViewById(R.id.chkGifEffect);
        LoadSettings();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }


    public void NavigateDestinationsActivity()
    {
        Intent i = new Intent(SettingsActivity.this, DestinationsActivity.class);
        startActivityForResult(i, 0);
    }

    public void btnManageDestinations(View v) {

        NavigateDestinationsActivity();
    }

    public void chkSaveSettings(View v)
    {
        SaveSettings();
    }

    public void LoadSettings()
    {
        Boolean AtmoWinEffectsEnabled = mPrefs.getBoolean("AtmoWinEffectsEnabled", true);
        Boolean GIFEffectEnabled = mPrefs.getBoolean("GifEffectEnabled", true);

        atmoWinEffects.setChecked(AtmoWinEffectsEnabled);
        gifEffect.setChecked(GIFEffectEnabled);
    }
    public void SaveSettings()
    {
        editor = mPrefs.edit();
        editor.putBoolean("AtmoWinEffectsEnabled", atmoWinEffects.isChecked());
        editor.putBoolean("GifEffectEnabled", gifEffect.isChecked());
        editor.commit();
    }
}
