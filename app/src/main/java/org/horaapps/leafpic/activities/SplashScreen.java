package org.horaapps.leafpic.activities;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.horaapps.leafpic.LookForMediaJob;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.util.PermissionUtils;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.liz.ColorPalette;

import java.io.File;

/**
 * Created by dnld on 01/04/16.
 */
public class SplashScreen extends SharedMediaActivity {

    private final String TAG = SplashScreen.class.getSimpleName();

    private final int EXTERNAL_STORAGE_PERMISSIONS = 12;
    private static final int PICK_MEDIA_REQUEST = 44;

    final static String CONTENT = "content";

    final static int ALBUMS_PREFETCHED = 2376;
    final static int PHOTOS_PREFETCHED = 2567;
    final static int ALBUMS_BACKUP = 1312;
    private boolean pickMode = false;
    public final static String ACTION_OPEN_ALBUM = "org.horaapps.leafpic.OPEN_ALBUM";

    //private Album tmpAlbum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_splash);


        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setNavBarColor();
        setStatusBarColor();

        pickMode = getIntent().getAction().equals(Intent.ACTION_GET_CONTENT) || getIntent().getAction().equals(Intent.ACTION_PICK);

        if (PermissionUtils.isStoragePermissionsGranted(this)) {


            if (getIntent().getAction().equals(ACTION_OPEN_ALBUM)) {
                    Bundle data = getIntent().getExtras();
                    if (data != null) {
                        String ab = data.getString("albumPath");
                        if (ab != null) {
                            File dir = new File(ab);
                            //tmpAlbum = new Album(getApplicationContext(), dir.getAbsolutePath(), data.getInt("albumId", -1), dir.getName(), -1);
                            // TODO: 4/10/17 handle
                            start();
                        }
                    } else StringUtils.showToast(getApplicationContext(), "Album not found");
                } else  // default intent
                    start();



        } else
            PermissionUtils.requestPermissions(this, EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        //startLookingForMedia();
    }

    private void start() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);

        if (pickMode) {
            intent.putExtra(MainActivity.ARGS_PICK_MODE, pickMode);
            startActivityForResult(intent, PICK_MEDIA_REQUEST);
        } else {
            startActivity(intent);
            finish();
        }
    }

    private void startLookingForMedia() {

        new Thread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /* TODO  && (has included folders) */) {

                JobInfo job = new JobInfo.Builder(0, new ComponentName(getApplicationContext(), LookForMediaJob.class))
                        .setPeriodic(1000)
                        .setRequiresDeviceIdle(true)
                        .build();

                JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                if (scheduler.getAllPendingJobs().size() == 0)
                    Log.wtf(TAG, scheduler.schedule(job) == JobScheduler.RESULT_SUCCESS
                            ? "LookForMediaJob scheduled successfully!" : "LookForMediaJob scheduled failed!");

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
            case EXTERNAL_STORAGE_PERMISSIONS:
                boolean gotPermission = grantResults.length > 0;

                for (int result : grantResults) {
                    gotPermission &= result == PackageManager.PERMISSION_GRANTED;
                }

                if (gotPermission) {
                    start();
                } else {
                    Toast.makeText(SplashScreen.this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        ((ProgressBar) findViewById(R.id.progress_splash)).getIndeterminateDrawable().setColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        findViewById(org.horaapps.leafpic.R.id.Splah_Bg).setBackgroundColor(getBackgroundColor());
    }
}
