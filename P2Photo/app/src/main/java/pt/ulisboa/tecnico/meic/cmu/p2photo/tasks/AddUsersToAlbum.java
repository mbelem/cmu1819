package pt.ulisboa.tecnico.meic.cmu.p2photo.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.sql.Timestamp;
import java.util.ArrayList;

import pt.ulisboa.tecnico.meic.cmu.p2photo.Cache;
import pt.ulisboa.tecnico.meic.cmu.p2photo.R;
import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.Main;
import pt.ulisboa.tecnico.meic.cmu.p2photo.api.P2PhotoException;

public class AddUsersToAlbum extends AsyncTask<Object,Void,Object[]> {
    @Override
    protected Object[] doInBackground(Object[] o) {
        Integer albumId = null;
        ArrayList<String> items2 = null;

        if (o != null) {
            albumId = (Integer) o[0];
            items2 = (ArrayList<String>) o[1];

            if (items2 != null) {
                for (String item : items2) {
                    try {
                        Log.d("inviteAlbum", item);
                        Main.getSv().updateAlbum(albumId, item);
                        Cache.getInstance().clientLog.add(Main.username + " added user " + item + "to an album with id " + albumId +
                                " at"  + new Timestamp(System.currentTimeMillis()));

                    } catch (P2PhotoException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //Cache.getInstance().loadingSpinner(false);
        //TODO
        return null;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        //TODO
    }
}
