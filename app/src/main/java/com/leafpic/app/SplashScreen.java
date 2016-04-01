package com.leafpic.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.utils.StringUtils;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends AppCompatActivity {

    //String now_playing, earned;
    HandlingAlbums albums;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        albums = new HandlingAlbums(SplashScreen.this);

        new PrefetchData().execute();
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashScreen.this, Manifest.permission.READ_EXTERNAL_STORAGE))
                StringUtils.showToast(SplashScreen.this, this.getString(R.string.storage_permision_denied));
            else {
                ActivityCompat.requestPermissions(SplashScreen.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        } else albums.loadPreviewAlbums();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                    checkPermissions(); //TODO spammare la richiesta di permission
                break;
        }
    }
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            checkPermissions();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashScreen.this, AlbumsActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("albums", albums);
            i.putExtras(b);
            startActivity(i);
            finish();
        }

    }

}
