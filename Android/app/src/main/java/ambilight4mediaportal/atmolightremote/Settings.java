package ambilight4mediaportal.atmolightremote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rick on 22-10-2015.
 */
public class Settings {

    SharedPreferences mPrefs;

    public void init(Context mContext)
    {
        // Settings
        mPrefs = mContext.getSharedPreferences("AtmoLightRemotePrefs", Context.MODE_PRIVATE);
    }

    public void SaveSettings()
    {

    }

    private void LoadSettings()
    {

    }
}
