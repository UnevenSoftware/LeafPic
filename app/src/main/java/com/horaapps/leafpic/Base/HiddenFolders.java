package com.horaapps.leafpic.Base;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by dnld on 24/04/16.
 */
public class HiddenFolders {
    private String androidFolderdata;

    public HiddenFolders(){
        androidFolderdata = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android";
    }

   public void getAlbums(File dir) {

       if (!dir.equals(new File(androidFolderdata))) {
           File[] children = dir.listFiles(new FoldersFileFilter());//dir.listFiles(new ImageFileFilter());
           for (File temp : children) {
               //Log.wtf("asd",temp.getAbsolutePath());

               if (temp.isDirectory()) {
                   Log.w("dirname:", temp.getAbsolutePath());
                   getImages(temp);
                   getAlbums(temp);
               }/* else{
                  Log.wtf("asd",temp.getAbsolutePath());
              }*/
           }
       }
    }

    public void getImages(File dir){
        File[] children = dir.listFiles(new ImageFileFilter());
        for (File child : children) {
            Log.wtf("file:",child.getAbsolutePath());
        }
    }

}
