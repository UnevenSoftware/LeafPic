package com.horaapps.leafpic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.PermissionUtils;
import com.horaapps.leafpic.utils.StringUtils;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends ThemedActivity {

    public final int READ_EXTERNAL_STORAGE_ID = 12;

    public final static String CONTENT = "content";
    public final static String PICK_MODE = "pick_mode";

    public final static int ALBUMS_PREFETCHED = 23;
    public final static int PHOTS_PREFETCHED = 2;
    public boolean PICK_INTENT = false;
    public final static String ACTION_OPEN_ALBUM = "com.horaapps.leafpic.OPEN_ALBUM";

    HandlingAlbums albums;
    Album album;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /**** START APP ****/
        /*
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean isFirstStart = SP.getBoolean("firstStart", true);
            if (isFirstStart) {
                SharedPreferences.Editor e = SP.edit();
                e.putBoolean("firstStart", false);
                e.apply();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(MainActivity.this, IntroActivity.class));
                    }
                }).start();
            }
        */

        albums = new HandlingAlbums(SplashScreen.this);

        TextView logo = (TextView) findViewById(R.id.txtLogo);
        logo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Figa.ttf"));

        RelativeLayout RelLay = (RelativeLayout) findViewById(R.id.Splah_Bg);
        RelLay.setBackgroundColor(getBackgroundColor());
        logo.setTextColor(getInvertedBackgroundColor());

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setNavBarColor();
        setStatusBarColor();

        if (PermissionUtils.isDeviceInfoGranted(this)) {
            if (getIntent().getAction().equals(Intent.ACTION_MAIN))
                new PrefetchAlbumsData().execute();
            else if (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT)
                    || getIntent().getAction().equals(Intent.ACTION_PICK)) {
                PICK_INTENT = true;
                Log.wtf("asd","pickmode");
                new PrefetchAlbumsData().execute();
            } else if (getIntent().getAction().equals(ACTION_OPEN_ALBUM)) {
                Bundle data = getIntent().getExtras();
                if (data != null) {
                    String ab = data.getString("albumID");
                    if (ab != null) {
                        album = new Album(ab, SplashScreen.this);
                        album.DisplayName = data.getString("albumName");
                        new PrefetchPhotosData().execute();
                    }
                } else StringUtils.showToast(getApplicationContext(), "Album not found");
            }

        } else {
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            PermissionUtils.requestPermissions(this, READ_EXTERNAL_STORAGE_ID, permissions);
        }

        if (getIntent().getAction() != null &&
                (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT) ||
                        getIntent().getAction().equals(Intent.ACTION_PICK))) {
           PICK_INTENT = true;
        }
    }

    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 70));
        }
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.md_black_1000), 70));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_ID:
                boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (granted)
                    new PrefetchAlbumsData().execute();
                else
                    Toast.makeText(SplashScreen.this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class PrefetchAlbumsData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            albums.loadPreviewAlbums();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("albums", albums);
            b.putInt(CONTENT, ALBUMS_PREFETCHED);
            b.putBoolean(PICK_MODE, PICK_INTENT);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    }

    private class PrefetchPhotosData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            album.updatePhotos();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("album", album);
            b.putInt(CONTENT, PHOTS_PREFETCHED);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    }
}
