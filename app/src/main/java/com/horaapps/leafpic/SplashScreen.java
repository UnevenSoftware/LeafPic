package com.horaapps.leafpic;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.Base.Album;
import com.horaapps.leafpic.Base.HandlingAlbums;
import com.horaapps.leafpic.Views.SharedMediaActivity;
import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.ColorPalette;
import com.horaapps.leafpic.utils.PermissionUtils;
import com.horaapps.leafpic.utils.PreferenceUtil;
import com.horaapps.leafpic.utils.StringUtils;

import java.io.File;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends SharedMediaActivity {

    private final int READ_EXTERNAL_STORAGE_ID = 12;
    private static final int PICK_MEDIA_REQUEST = 44;

    final static String CONTENT = "content";
    final static String PICK_MODE = "pick_mode";

    final static int ALBUMS_PREFETCHED = 23;
    final static int PHOTS_PREFETCHED = 2;
    final static int ALBUMS_BACKUP = 60;
    private boolean PICK_INTENT = false;
    public final static String ACTION_OPEN_ALBUM = "com.horaapps.leafpic.OPEN_ALBUM";

    //private HandlingAlbums albums;
    private Album album;

    private PreferenceUtil SP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SP = PreferenceUtil.getInstance(getApplicationContext());
        /**** START APP ****/

        //albums = new HandlingAlbums(getApplicationContext());

        TextView logo = (TextView) findViewById(R.id.txtLogo);
        logo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Figa.ttf"));
        //logo.setPadding(0,0,0,25+ Measure.getNavBarHeight(getApplicationContext()));
        //logo.animate().translationY(-Measure.getNavBarHeight(getApplicationContext())).start();
        logo.setTextColor(getSubTextColor());
        //logo.setLetterSpacing((float)0.2);



        RelativeLayout RelLay = (RelativeLayout) findViewById(R.id.Splah_Bg);
        RelLay.setBackgroundColor(getBackgroundColor());


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setNavBarColor();
        setStatusBarColor();

        if (PermissionUtils.isDeviceInfoGranted(this)) {
            if (getIntent().getAction().equals(Intent.ACTION_MAIN))
                new PrefetchAlbumsData().execute(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
            //new PrefetchAlbumsData().execute();
            else if (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT)
                    || getIntent().getAction().equals(Intent.ACTION_PICK)) {
                PICK_INTENT = true;
                new PrefetchAlbumsData().execute(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
            } else if (getIntent().getAction().equals(ACTION_OPEN_ALBUM)) {
                Bundle data = getIntent().getExtras();
                if (data != null) {
                    String ab = data.getString("albumPath");
                    if (ab != null) {
                        File dir = new File(ab);
                        album = new Album(dir.getAbsolutePath(),dir.getName());
                        new PrefetchPhotosData().execute();
                    }
                } else StringUtils.showToast(getApplicationContext(), "Album not found");
            }

        } else {
            String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            PermissionUtils.requestPermissions(this, READ_EXTERNAL_STORAGE_ID, permissions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_MEDIA_REQUEST) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
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
                    new PrefetchAlbumsData().execute(SP.getBoolean(getString(R.string.preference_auto_update_media), false));
                else
                    Toast.makeText(SplashScreen.this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class PrefetchAlbumsData extends AsyncTask<Boolean, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... arg0) {
            if (arg0[0])
                getAlbums().loadAlbums(getApplicationContext(), false);
            else {
                getAlbums().restoreBackup(getApplicationContext());
                if(getAlbums().dispAlbums.size() == 0)
                    getAlbums().loadAlbums(getApplicationContext(), false);
                else return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            //((MyApplication) getApplicationContext()).setAlbums(albums);
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putInt(CONTENT, result ? ALBUMS_PREFETCHED : ALBUMS_BACKUP);
            b.putBoolean(PICK_MODE, PICK_INTENT);
            i.putExtras(b);
            if (PICK_INTENT)
                startActivityForResult(i, PICK_MEDIA_REQUEST);
            else {
                startActivity(i);
                finish();
            }
            if(result)
                getAlbums().saveBackup(getApplicationContext());
        }
    }

    private class PrefetchPhotosData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            album.updatePhotos(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            Bundle b = new Bundle();
            getAlbums().addAlbum(0, album);
            b.putInt(CONTENT, PHOTS_PREFETCHED);
            i.putExtras(b);
            startActivity(i);
            finish();
        }
    }
}
