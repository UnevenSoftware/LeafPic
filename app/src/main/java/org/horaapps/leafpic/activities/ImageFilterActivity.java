package org.horaapps.leafpic.activities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.adapters.ThumbnailsAdapter;
import org.horaapps.leafpic.data.ThumbnailItem;
import org.horaapps.leafpic.items.ThumbnailCallback;
import org.horaapps.leafpic.util.ThumbnailsManager;
import org.horaapps.liz.ThemedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by temidayo on 30/05/2018.
 */

public class ImageFilterActivity extends ThemedActivity implements ThumbnailCallback {
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private Activity activity;
    private Toolbar toolbar;
    private Uri uri;
    Bitmap  mBitmap;
    private RecyclerView thumbListView;
    private ImageView placeHolderImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filter);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;
        uri = getIntent().getData();
        try {
              mBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initUIWidgets();
    }
    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();

        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.filter));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filters, menu);
        menu.findItem(R.id.action_done).setIcon(getToolbarIcon(CommunityMaterial.Icon.cmd_check));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_done:
                Bitmap bm=((BitmapDrawable)placeHolderImageView.getDrawable()).getBitmap();
                SaveFilteredImage(bm);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void initUIWidgets() {
        thumbListView = findViewById(R.id.thumbnails);
        placeHolderImageView = findViewById(R.id.place_holder_imageview);

        placeHolderImageView.setImageBitmap(Bitmap.createScaledBitmap(mBitmap, 640, 640, false));
        initHorizontalList();
    }

    private void initHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }
    private void SaveFilteredImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(String.format("%s/leafpic_images", root));
        myDir.mkdirs();
        String fname = String.format("%d_image.jpg", System.currentTimeMillis());
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(activity, R.string.filter_success, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void bindDataToAdapter() {
        final Context context = this.getApplication();
        Handler handler = new Handler();
        Runnable r = () -> {
            Bitmap thumbImage = Bitmap.createScaledBitmap(mBitmap, 640, 640, false);
            ThumbnailItem t1 = new ThumbnailItem();
            ThumbnailItem t2 = new ThumbnailItem();
            ThumbnailItem t3 = new ThumbnailItem();
            ThumbnailItem t4 = new ThumbnailItem();
            ThumbnailItem t5 = new ThumbnailItem();
            ThumbnailItem t6 = new ThumbnailItem();

            t1.image = thumbImage;
            t2.image = thumbImage;
            t3.image = thumbImage;
            t4.image = thumbImage;
            t5.image = thumbImage;
            t6.image = thumbImage;
            ThumbnailsManager.clearThumbs();
            ThumbnailsManager.addThumb(t1); // Original Image

            t2.filter = SampleFilters.getStarLitFilter();
            ThumbnailsManager.addThumb(t2);

            t3.filter = SampleFilters.getBlueMessFilter();
            ThumbnailsManager.addThumb(t3);

            t4.filter = SampleFilters.getAweStruckVibeFilter();
            ThumbnailsManager.addThumb(t4);

            t5.filter = SampleFilters.getLimeStutterFilter();
            ThumbnailsManager.addThumb(t5);

            t6.filter = SampleFilters.getNightWhisperFilter();
            ThumbnailsManager.addThumb(t6);

            List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

            ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, (ThumbnailCallback) activity);
            thumbListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        };
        handler.post(r);
    }

    @Override
    public void onThumbnailClick(Filter filter) {
        placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(mBitmap, 640, 640, false)));
    }

}
