package pt.ulisboa.tecnico.meic.cmu.p2photo.bcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.sql.Timestamp;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.meic.cmu.p2photo.Cache;
import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.Main;
import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.P2PhotoActivity;
import pt.ulisboa.tecnico.meic.cmu.p2photo.tasks.LocalCacheInit;

import static pt.ulisboa.tecnico.meic.cmu.p2photo.activities.ChooseCloudOrLocal.wifiConnector;

public class P2PhotoWiFiDBroadcastReceiver extends BroadcastReceiver {

    private P2PhotoActivity activity;

    public P2PhotoWiFiDBroadcastReceiver(P2PhotoActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // This action is triggered when the Termite service changes state:
            // - creating the service generates the WIFI_P2P_STATE_ENABLED event
            // - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(activity, "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            //wifiConnector.requestPeersInRange();
            Toast.makeText(activity, "Peer list changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Toast.makeText(activity, "Network membership changed",
                    Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
            ginfo.print();
            Cache.getInstance().cleanArrays();

            //clears ARP cache
            wifiConnector.getArpCache().removeAllEntries();

            //DEBUG TO USE WITH ONLY ONE EMU!
            /*wifiConnector.getArpCache().addEntry("vitor", "192.168.0.1");
            wifiConnector.getArpCache().addEntry("alfredo", "192.168.0.1");
            wifiConnector.getArpCache().addEntry("aristides", "192.168.0.1");*/

            Cache.getInstance().clientLog.add("WiFiD Group Change detected " + new Timestamp(System.currentTimeMillis()));
            File userFolder = new File(context.getFilesDir(), Main.username);
            new LocalCacheInit().execute(userFolder, Cache.getInstance());

            wifiConnector.requestGroupInfo();
            Toast.makeText(activity, "Group ownership changed",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
