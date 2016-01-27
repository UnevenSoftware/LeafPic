package com.leafpic.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by dnld on 1/27/16.
 */
public class PhotoRecordingService extends Service {

    static final String TAG = "FILEOBSERVER";
    FileObserver observer;
    //PinMarkerDataSource pinMarkerDataSource;
    Context context;

    String provider;
    private String photoDirectory;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {


        photoDirectory = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera";

        //init the watching function of photo directory
        observer = initSingleDirectoryObserver(photoDirectory);
        observer.startWatching();
    }

    @Override
    public void onDestroy() {

        observer.stopWatching();
        super.onDestroy();
    }


    private FileObserver initSingleDirectoryObserver(String directoryPath) {
        final String rootPath = directoryPath;
        FileObserver observer = new FileObserver(directoryPath) {
            @Override
            public void onEvent(int event, String path) {
                String filePath = rootPath + "/" + path;
                switch (event) {
                    case FileObserver.CREATE:
                        Log.d(TAG, "CREATE:" + rootPath + path);
                        break;
                    case FileObserver.DELETE:
                        Log.d(TAG, "DELETE:" + rootPath + path);
                        break;
                    case FileObserver.DELETE_SELF:
                        Log.d(TAG, "DELETE_SELF:" + rootPath + path);
                        break;
                    case FileObserver.MODIFY:
                        Log.d(TAG, "MODIFY:" + rootPath + path);
                        break;
                    case FileObserver.MOVED_FROM:
                        Log.d(TAG, "MOVED_FROM:" + rootPath + path);
                        break;
                    case FileObserver.MOVED_TO:
                        Log.d(TAG, "MOVED_TO:" + path);
                        break;
                    case FileObserver.MOVE_SELF:
                        Log.d(TAG, "MOVE_SELF:" + path);
                        break;
                    default:
                        // just ignore
                        break;
                }
            }
        };
        return observer;
    }


}