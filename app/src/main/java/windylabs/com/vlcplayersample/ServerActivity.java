package windylabs.com.vlcplayersample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

public class ServerActivity extends Activity {

    TextView infoip, msg, infoWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        infoWifi = (TextView) findViewById(R.id.infoWifi);

        startService(new Intent(this, Server.class));

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            infoWifi.setText("This device is connected to " + wifi.getConnectionInfo().getSSID()
                    + "\n" + "IP address is " + Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress()));
        } else {
            infoWifi.setText("This device is not connected to WiFi. List of all SSIDs are below.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
