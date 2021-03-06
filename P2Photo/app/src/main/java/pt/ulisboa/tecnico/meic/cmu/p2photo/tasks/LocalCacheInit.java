package pt.ulisboa.tecnico.meic.cmu.p2photo.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pt.ulisboa.tecnico.meic.cmu.p2photo.Cache;

public class LocalCacheInit extends AsyncTask<Object, String, String> {

    private static final String TAG = LocalCacheInit.class.getName();

    @Override
    protected String doInBackground(Object... params) {
        File folder = (File) params[0];
        Cache cacheInstance = (Cache) params[1];
        Log.d(TAG, "Started LocalCacheInit");
        Log.d(TAG, "FolderPath: " + folder);
        folder.mkdir();
        File[] files = folder.listFiles();
        BufferedReader br;
        String line;

        //get my albums
        Thread t1 = new Thread(new AllAlbums());
        t1.start();
        Thread t2 = new Thread(new OwningAlbums());
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "OwningAlbums Size: " + Cache.getInstance().ownedAlbumsIDs.size());
        Log.d(TAG, "AllAlbums Size: " + Cache.getInstance().ownedAndPartAlbumsIDs.size());

        for (File file : files) {
            if (file.getName().endsWith("_catalog.txt")) {
                try {
                    Log.d(TAG, "Found " + file.getName());
                    br = new BufferedReader(new FileReader(file));
                    line = br.readLine();

                    synchronized (cacheInstance) {
                        String[] splited = line.split(" ");

                        if (!cacheInstance.albums.contains(splited[1])) {
                            Log.d(TAG, file.getName() + " first line: " + splited[0] + " " + splited[1]);

                            cacheInstance.albumsIDs.add(Integer.parseInt(splited[0]));
                            cacheInstance.albums.add(splited[1]);
                            //add to owned
                            if (cacheInstance.ownedAlbumsIDs.contains(Integer.parseInt(splited[0]))) {
                                cacheInstance.ownedAlbums.add(splited[1]);
                                cacheInstance.ownedAndPartAlbums.add(splited[1]);
                                cacheInstance.ownedAlbumWithIDs.add(splited[0] + " " + splited[1]); //same but parsed
                            }
                            //add to owned and parsed
                            else if (cacheInstance.ownedAndPartAlbumsIDs.contains(Integer.parseInt(splited[0]))) {
                                cacheInstance.ownedAndPartAlbums.add(splited[1]);
                                cacheInstance.ownedAlbumWithIDs.add(splited[0] + " " + splited[1]); //same but parsed
                            }


                        }
                        Log.d(TAG, "Cache Size: " + Cache.ownedAlbumWithIDs.size());
                        cacheInstance.notifyAdapters();
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

        return null;
    }
}
