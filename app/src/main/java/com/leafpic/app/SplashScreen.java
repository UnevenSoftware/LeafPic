package com.leafpic.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leafpic.app.Base.HandlingAlbums;
import com.leafpic.app.Views.ThemedActivity;
import com.leafpic.app.utils.PermissionUtils;
import com.leafpic.app.utils.StringUtils;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends ThemedActivity {

    public final int READ_EXTERNAL_STORAGE_ID = 12;

    HandlingAlbums albums;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        albums = new HandlingAlbums(SplashScreen.this);

        if (PermissionUtils.isDeviceInfoGranted(this)) {
            new PrefetchData().execute();
        } else {
            String[] permissions = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
            PermissionUtils.requestPermissions(this, READ_EXTERNAL_STORAGE_ID, permissions);
        }

        RelativeLayout RL = (RelativeLayout) findViewById(R.id.Splah_RelativeLayout);
        RL.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.md_dark_background));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.md_dark_background));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_ID:
                boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (granted)
                    new PrefetchData().execute();
                 else
                    Toast.makeText(SplashScreen.this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            albums.loadPreviewAlbums();
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
