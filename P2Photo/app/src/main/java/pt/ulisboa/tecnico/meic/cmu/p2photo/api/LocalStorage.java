package pt.ulisboa.tecnico.meic.cmu.p2photo.api;

import android.content.Context;

import java.io.File;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.meic.cmu.p2photo.Cache;
import pt.ulisboa.tecnico.meic.cmu.p2photo.activities.Main;
import pt.ulisboa.tecnico.meic.cmu.p2photo.tasks.AddAlbumSliceCatalogURL;
import pt.ulisboa.tecnico.meic.cmu.p2photo.tasks.LocalCacheInit;
import pt.ulisboa.tecnico.meic.cmu.p2photo.tasks.UpdateAlbumCatalog;

/**
 * Support for reading and writing files locally on the device in order to use Wi-Fi Direct file
 * transfer
 */
public class LocalStorage extends StorageProvider {

    public LocalStorage(Context context, AlbumCatalog catalog, Operation operation) {
        super(context, catalog, operation);
    }

    public LocalStorage(Context context, int albumId, Operation operation) {
        super(context, albumId, operation);
    }

    public LocalStorage(Context context, AlbumCatalog catalog, Operation operation, Object[] args) {
        super(context, catalog, operation, args);
    }

    @Override
    void writeFile(String fileURL) {

        try {
            //just to accept my own invitation in order to be considered as the owner of my album
            //instead of a URL to a cloud file send my username
            new AddAlbumSliceCatalogURL().execute(getCatalog().getAlbumId(), Main.username).get();

            //just to refresh the cache
            File userFolder = new File(getContext().getFilesDir(), Main.username);
            new LocalCacheInit().execute(userFolder, Cache.getInstance()).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    AlbumCatalog readFile(String fileURL, String description, String folderPath, String fileName, int option) {
        return null;
    }

    @Override
    protected void loadData() {

    }
}
