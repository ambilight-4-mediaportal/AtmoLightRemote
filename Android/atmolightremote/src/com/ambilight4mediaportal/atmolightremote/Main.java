package com.ambilight4mediaportal.atmolightremote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View;

import com.commonsware.cwac.colormixer.ColorMixer;
import com.commonsware.cwac.colormixer.ColorMixerActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Main extends Activity {

  // Default ColorMixer settings
  private static final int COLOR_REQUEST=1337;
  private TextView color=null;
  private CheckBox EnablePrioties;
  private ColorMixer mixer=null;

  // Preference settings
  Context mContext;
  SharedPreferences mPrefs;

  // Log settings
  private static final String TAG = ColorMixer.class.getSimpleName();

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
    setContentView(R.layout.main);

    // Default UI bindings
    color=(TextView)findViewById(R.id.color);
    EnablePrioties=(CheckBox)findViewById(R.id.cbUsePriorities);

    mixer=(ColorMixer)findViewById(R.id.mixer);
    mixer.setOnColorChangedListener(onColorChange);

    // Settings
    mContext = getApplicationContext();
    mPrefs = mContext.getSharedPreferences("AtmoLightRemotePrefs", Context.MODE_PRIVATE);

    MultiCastInstance();
    LoadSettings();
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

  @Override
  public void onActivityResult(int requestCode, int resultCode,
                               Intent result) {
    if (requestCode==COLOR_REQUEST && resultCode==RESULT_OK) {
      mixer.setColor(result.getIntExtra(ColorMixerActivity.COLOR,
              mixer.getColor()));
    }
    else {
      super.onActivityResult(requestCode, resultCode, result);
    }
  }

  private void LoadSettings()
  {

    int argb = mPrefs.getInt("AtmoLightColors", 0);
    Boolean PrioritiesEnabled = mPrefs.getBoolean("PrioritiesEnabled", false);

    mixer.setColor(argb);
    EnablePrioties.setChecked(PrioritiesEnabled);
  }

  private void SaveSettings()
  {
    int argb = mixer.getColor();
    Boolean PrioritiesEnabled = EnablePrioties.isChecked();

    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putInt("AtmoLightColors", argb);
    editor.putBoolean("PrioritiesEnabled", PrioritiesEnabled);
    editor.commit();
  }

  private ColorMixer.OnColorChangedListener onColorChange=
          new ColorMixer.OnColorChangedListener() {
            public void onColorChange(int argb) {

              /// On color change send commands to Orb
              //Log.d(TAG, String.valueOf(Color.red(argb)));
              //Log.d(TAG, String.valueOf(Color.green(argb)));
              //Log.d(TAG, String.valueOf(Color.blue(argb)));

              int red = Color.red(argb);
              int green = Color.green(argb);
              int blue = Color.blue(argb);

              String colorText = "R: " + red + " / " + "G: " + green + " / " +"B: " + blue;
              color.setText(colorText);
              SaveSettings();
              setColor(red,green,blue);
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

            if(EnablePrioties.isChecked())
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

  private ColorMixer.OnColorChangedListener onDialogSet=
          new ColorMixer.OnColorChangedListener() {
            public void onColorChange(int argb) {
              mixer.setColor(argb);
            }
          };

  public void btnTurnOffLights(View v) {
    mixer.setColor(0);
    SaveSettings();
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

  public void chkSavePriorties(View v) {
    SaveSettings();
  }

  public void btnClearPriorities(View v) {
    clearPriorities();
  }

}