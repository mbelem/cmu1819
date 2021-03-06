package pt.ulisboa.tecnico.meic.cmu.p2photo.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CreateFolderResult;

import java.sql.Timestamp;

import pt.ulisboa.tecnico.meic.cmu.p2photo.Cache;
import pt.ulisboa.tecnico.meic.cmu.p2photo.DropboxClientFactory;
import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.Main;

public class CreateFolder extends AsyncTask<Object,Object,Object[]> {

    @Override
    protected Object[] doInBackground(Object[] objects) {
        String albumName = (String) objects[0];
        Object[] result = new Object[3];
        result[2] = albumName;
        result[0] = objects[1];
        try {
            CreateFolderResult res = DropboxClientFactory.getClient().files().createFolderV2("/" + Main.username + "/" + albumName);

            result[1] = "OK";
            return result;
        } catch (DbxException e1) {
            result[1] = "NOK";
            return result;
        }
    }

    @Override
    protected void onPostExecute(Object[] result) {
        String res = (String) result[1];
        String albumName = (String) result[2];
        if (res == "OK") {
            Cache.getInstance().notifyAdapters();

            Toast.makeText((Context) result[0], "Album created in your dropbox",
                    Toast.LENGTH_LONG).show();
        } else {
           Toast.makeText((Context) result[0], "Album NOT created in your dropbox",
                    Toast.LENGTH_LONG).show();
        }
        Cache.getInstance().loadingSpinner(false);
    }
}
