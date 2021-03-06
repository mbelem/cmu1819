package pt.ulisboa.tecnico.meic.cmu.p2photo.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.Main;

/**
 * Task to download a file from Dropbox and put it in a local folder
 */
public class DownloadFileFromLink extends AsyncTask<String, Void, File> {

    private static final String TAG = DownloadFileFromLink.class.getName();

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DownloadFileFromLink(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(String... params) {
        String url = params[0];
        String folderPath = params[1];
        String fileName = params[2];

        try {
            File path;
            if (folderPath == "") {
                path = new File (Main.CACHE_FOLDER + "/" + Main.username);
            } else {
                path = new File(Main.CACHE_FOLDER + "/" + Main.username + "/" + folderPath);
            }
            File file = new File(path, fileName);

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                    return null;
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.

            Log.d(TAG, "I'm about to download file at: " + url + "&raw=1");
            URL fileUrl = new URL(url + "&raw=1");

            ReadableByteChannel readableByteChannel = Channels.newChannel(fileUrl.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();

            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            return file;
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }
}
