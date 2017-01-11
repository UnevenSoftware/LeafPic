package org.horaapps.leafpic.activities;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.horaapps.leafpic.LookForMediaJob;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;
import org.horaapps.leafpic.util.ColorPalette;
import org.horaapps.leafpic.util.PermissionUtils;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import java.io.File;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends SharedMediaActivity {

    private final String TAG = SplashScreen.class.getSimpleName();

    private final int READ_EXTERNAL_STORAGE_ID = 12;
    private static final int PICK_MEDIA_REQUEST = 44;

    final static String CONTENT = "content";
    final static String PICK_MODE = "pick_mode";

    final static int ALBUMS_PREFETCHED = 2376;
    final static int PHOTOS_PREFETCHED = 2567;
    final static int ALBUMS_BACKUP = 1312;
    private boolean PICK_INTENT = false;
    public final static String ACTION_OPEN_ALBUM = "org.horaapps.leafpic.OPEN_ALBUM";

    private Album tmpAlbum;

    private PreferenceUtil SP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_splash);
        SP = PreferenceUtil.getInstance(getApplicationContext());


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setNavBarColor();
        setStatusBarColor();

        if (PermissionUtils.isDeviceInfoGranted(this)) {

            if (getIntent().getAction().equals(ACTION_OPEN_ALBUM)) {
                Bundle data = getIntent().getExtras();
                if (data != null) {
                    String ab = data.getString("albumPath");
                    if (ab != null) {
                        File dir = new File(ab);
                        tmpAlbum = new Album(getApplicationContext(), dir.getAbsolutePath(), data.getInt("albumId", -1), dir.getName(), -1);
                        new PrefetchPhotosData().execute();
                    }
                } else StringUtils.showToast(getApplicationContext(), "Album not found");
            } else  // default intent
                new PrefetchAlbumsData().execute();

            PICK_INTENT = getIntent().getAction().equals(Intent.ACTION_GET_CONTENT) || getIntent().getAction().equals(Intent.ACTION_PICK);

        } else
            PermissionUtils.requestPermissions(this, READ_EXTERNAL_STORAGE_ID, Manifest.permission.READ_EXTERNAL_STORAGE);

        startLookingForMedia();
    }

    private void startLookingForMedia() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getAlbums().getFoldersCount(HandlingAlbums.INCLUDED) > 0) {

                    JobInfo job = new JobInfo.Builder(0, new ComponentName(getApplicationContext(), LookForMediaJob.class))
                            .setPeriodic(1000)
                            .setRequiresDeviceIdle(true)
                            .build();

                    JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    if (scheduler.getAllPendingJobs().size() == 0)
                        Log.wtf(TAG, scheduler.schedule(job) == JobScheduler.RESULT_SUCCESS
                                ? "LookForMediaJob scheduled successfully!" : "LookForMediaJob scheduled failed!");

                }
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_MEDIA_REQUEST:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void setNavBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), org.horaapps.leafpic.R.color.md_black_1000), 70));
        }
    }

    @Override
    protected void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ColorPalette.getTransparentColor(
                    ContextCompat.getColor(getApplicationContext(), org.horaapps.leafpic.R.color.md_black_1000), 70));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) new PrefetchAlbumsData().execute();
                else Toast.makeText(SplashScreen.this, getString(org.horaapps.leafpic.R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void updateUiElements() {
        ((ProgressBar) findViewById(R.id.progress_splash)).getIndeterminateDrawable().setColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        findViewById(org.horaapps.leafpic.R.id.Splah_Bg).setBackgroundColor(getBackgroundColor());
    }

    private class PrefetchAlbumsData extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            if(!getAlbums().restoreBackup(getApplicationContext()) || getAlbums().getCount() == 0) {
                getAlbums().loadAlbums(getApplicationContext(), false);
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            i.putExtra(CONTENT, result ? ALBUMS_PREFETCHED : ALBUMS_BACKUP);
            i.putExtra(PICK_MODE, PICK_INTENT);

            if (!PICK_INTENT) {
                startActivity(i);
                finish();
            } else
                startActivityForResult(i, PICK_MEDIA_REQUEST);

            if(result)
                getAlbums().saveBackup(getApplicationContext());
        }
    }

    private class PrefetchPhotosData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            tmpAlbum.updatePhotos(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            getAlbums().addAlbum(0, tmpAlbum);
            i.putExtra(CONTENT, PHOTOS_PREFETCHED);
            startActivity(i);
            finish();
        }
    }
}
