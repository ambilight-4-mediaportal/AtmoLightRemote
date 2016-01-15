package ambilight4mediaportal.atmolightremote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import com.commonsware.cwac.colormixer.ColorMixer;
import com.commonsware.cwac.colormixer.ColorMixerActivity;

import ambilight4mediaportal.atmolightremote.ColorPickerDialog;
import ambilight4mediaportal.atmolightremote.ColorPickerDialog.OnColorSelectedListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    private Menu optionsMenu;

    // Default settings and bindings
    private static final int COLOR_REQUEST=1337;
    private TextView color=null;
    private CheckBox enablePrioties;
    private FrameLayout frameEffects;
    private TextView tvAtmoWin;
    private Button btnAtmoWinColorChanger;
    private Button btnGIFreader;
    private Button btnAtmoWinColorChangerLR;
    private Button btnAtmoWinExternalLive;
    private ColorMixer mixer=null;

    // Preference settings
    Context mContext;
    SharedPreferences mPrefs;
    SharedPreferences.Editor editor;
    Settings settings;
    boolean firstTimeRun;
    boolean startingApp = true;

    // Log settings
    private static final String TAG = "REMOTE_MAIN";

    // Multicast settings
    MulticastSocket m_socket;
    InetAddress mMulticastAddress;

    // Command information
    private static String cCommand;
    private static String cEffect;
    private static int cRed;
    private static int cGreen;
    private static int cBlue;

    @Override
    public void onCreate(Bundle icicle) {
        // Turn off multicast filter
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wm.createMulticastLock("debuginfo");
        multicastLock.acquire();

        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        // Default UI bindings
        color=(TextView)findViewById(R.id.color);
        enablePrioties =(CheckBox)findViewById(R.id.cbUsePriorities);

        mixer=(ColorMixer)findViewById(R.id.mixer);
        mixer.setOnColorChangedListener(onColorChange);
        tvAtmoWin =(TextView)findViewById(R.id.tvAtmoWinEffects);
        btnGIFreader =(Button)findViewById(R.id.btnGifReaderEffect);
        btnAtmoWinColorChanger =(Button)findViewById(R.id.btnAtmoWinColorChangerEffect);
        btnAtmoWinColorChangerLR =(Button)findViewById(R.id.btnAtmoWinColorChangerLREffect);
        btnAtmoWinExternalLive =(Button)findViewById(R.id.btnAtmoWinExternalLiveEffect);
        frameEffects  =(FrameLayout)findViewById(R.id.FrameLayoutEffects);

        // Settings
        settings = new Settings();
        mContext = getApplicationContext();
        settings.init(mContext);
        mPrefs = mContext.getSharedPreferences("AtmoLightRemotePrefs", Context.MODE_PRIVATE);

        MultiCastInstance();
        LoadSettings();
        startingApp = false;

        if(firstTimeRun) {
            firstRunSetup();
            firstTimeRun = false;
            SaveSettings();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.optionsMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            /*case R.id.action_settings:
                NavigateSettingsActivity();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void NavigateSettingsActivity()
    {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(i, 0);
    }

    public void MultiCastInstance()
    {
        try {
            mMulticastAddress = InetAddress.getByName("239.1.15.19");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            m_socket = new MulticastSocket(16400);
            m_socket.setSendBufferSize(50000);
            m_socket.setReceiveBufferSize(50000);

            m_socket.joinGroup(mMulticastAddress);
            m_socket.setLoopbackMode(true);
            m_socket.setTimeToLive(16);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void firstRunSetup()
    {
        final SpannableString url = new SpannableString("forum.team-mediaportal.com/threads/atmolight-remote.132467/");

        final TextView tx1 = new TextView(this);
        tx1.setText("For first time setup a tutorial is located here: " + url);
        tx1.setAutoLinkMask(RESULT_OK);
        tx1.setMovementMethod(LinkMovementMethod.getInstance());

        Linkify.addLinks(url, Linkify.WEB_URLS);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tutorial")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                            }
                        })

                .setView(tx1).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent result) {
        if(!startingApp) {
            if (requestCode == COLOR_REQUEST && resultCode == RESULT_OK) {
                mixer.setColor(result.getIntExtra(ColorMixerActivity.COLOR,
                        mixer.getColor()));
            } else {
                super.onActivityResult(requestCode, resultCode, result);
            }
        }

        if (requestCode == 0) {
            if (resultCode == RESULT_CANCELED) {
                // user pressed back from 2nd activity to go to 1st activity. code here
                LoadSettings();
            }
        }
    }

    private void LoadSettings()
    {
        int argb = mPrefs.getInt("AtmoLightColors", 0);
        Boolean PrioritiesEnabled = mPrefs.getBoolean("PrioritiesEnabled", false);
        Boolean AtmoWinEffectsEnabled = mPrefs.getBoolean("AtmoWinEffectsEnabled", true);
        Boolean GIFEffectEnabled = mPrefs.getBoolean("GifEffectEnabled", true);
        firstTimeRun = mPrefs.getBoolean("FirstRun", true);

        mixer.setColor(argb);
        int red = Color.red(argb);
        int green = Color.green(argb);
        int blue = Color.blue(argb);
        String colorText = "R: " + red + " / " + "G: " + green + " / " + "B: " + blue;
        color.setText(colorText);

        enablePrioties.setChecked(PrioritiesEnabled);

        /*
        if(AtmoWinEffectsEnabled) {
            tvAtmoWin.setVisibility(View.GONE);
            btnAtmoWinColorChanger.setVisibility(View.VISIBLE);
            btnAtmoWinColorChangerLR.setVisibility(View.VISIBLE);
            btnAtmoWinExternalLive.setVisibility(View.VISIBLE);
        }
        else
        {
            tvAtmoWin.setVisibility(View.GONE);
            btnAtmoWinColorChanger.setVisibility(View.GONE);
            btnAtmoWinColorChangerLR.setVisibility(View.GONE);
            btnAtmoWinExternalLive.setVisibility(View.GONE);
        }

        if(GIFEffectEnabled)
        {
            btnGIFreader.setVisibility(View.VISIBLE);
        }
        else
        {
            btnGIFreader.setVisibility(View.GONE);
        }*/
    }

    private void SaveSettings()
    {
        int argb = mixer.getColor();
        Boolean PrioritiesEnabled = enablePrioties.isChecked();

        editor = mPrefs.edit();
        editor.putBoolean("FirstRun", firstTimeRun);
        editor.putInt("AtmoLightColors", argb);
        editor.putBoolean("PrioritiesEnabled", PrioritiesEnabled);
        editor.commit();
    }

    private ColorMixer.OnColorChangedListener onColorChange=
            new ColorMixer.OnColorChangedListener() {
                public void onColorChange(int argb) {
                    if (!startingApp) {

                        /// On color change send commands to Orb
                        //Log.d(TAG, String.valueOf(Color.red(argb)));
                        //Log.d(TAG, String.valueOf(Color.green(argb)));
                        //Log.d(TAG, String.valueOf(Color.blue(argb)));

                        int red = Color.red(argb);
                        int green = Color.green(argb);
                        int blue = Color.blue(argb);

                        String colorText = "R: " + red + " / " + "G: " + green + " / " + "B: " + blue;
                        color.setText(colorText);
                        SaveSettings();
                        setColor(red, green, blue);
                    }
                }
            };

    private void setColor(int red, int green, int blue)
    {
        cRed = red;
        cGreen = green;
        cBlue = blue;
        cCommand = "static";

        new SendMultiCastData().execute("");
    }

    private void setEffect(String Effect)
    {
        cRed = 0;
        cGreen = 0;
        cBlue = 0;
        cEffect = Effect;
        cCommand = "effect";

        new SendMultiCastData().execute("");
    }

    private void clearPriorities()
    {
        cCommand = "priorityClear";

        new SendMultiCastData().execute("");
    }

    private void createColorWheel()
    {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, mixer.getColor(), new OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                mixer.setColor(color);
            }
        });
        colorPickerDialog.show();
    }

    private class SendMultiCastData extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            {
                try {
                    String colorRed = String.valueOf(cRed);
                    String colorGreen = String.valueOf(cGreen);
                    String colorBlue = String.valueOf(cBlue);
                    String colors = colorRed + ":" + colorGreen + ":" + colorBlue;
                    String msg = "";
                    String priority = "999";

                    if(enablePrioties.isChecked())
                    {
                        priority = "100";
                    }

                    if(cCommand == "effect") {
                        msg = "atmolight|" + cCommand + "|" + cEffect + "|" + priority + "|;";
                    }
                    else if (cCommand == "static")
                    {
                        msg = "atmolight|" + cCommand + "|" + colors + "|" + priority + "|;";
                    }
                    else if(cCommand == "priorityClear") {
                        msg = "atmolight|priority|" + "0" + "|;";
                    }

                    byte data[] = msg.toString().getBytes();
                    DatagramPacket dp = new DatagramPacket(data, data.length, mMulticastAddress, 16400);
                    try {
                        m_socket.send(dp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                catch (Exception e){}
            }

            return null;
        }
    }

    public void btnSetEffectDisable(View v) {
        setEffect("LEDsDisabled");
    }
    public void btnSetEffectLiveMode(View v) {
        setEffect("MediaPortalLiveMode");
    }

    public void btnSetEffectGifReader(View v) {
        setEffect("GIFReader");
    }

    public void btnSetEffectAtmoWinColorChanger(View v) {
        setEffect("AtmoWinColorchanger");
    }

    public void btnSetEffectAtmoWinExternalLiveMode(View v) {
        setEffect("ExternalLiveMode");
    }

    public void btnSetEffectAtmoWinColorChangerLR(View v) {
        setEffect("AtmoWinColorchangerLR");
    }

    public void btnSetEffectStaticColor(View v) {
        setEffect("StaticColor");
    }

    public void chkSavePriorties(View v) {
        SaveSettings();
    }

    public void btnClearPriorities(View v) {
        clearPriorities();
    }

    public void  btnShowColorPicker (View v) {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this, mixer.getColor(), new OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                mixer.setColor(color);
            }
        });
        colorPickerDialog.show();
    }

}
