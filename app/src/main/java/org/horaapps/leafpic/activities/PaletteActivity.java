package org.horaapps.leafpic.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.CustomTabService;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.horaapps.leafpic.R.string.path;

/**
 * Created by Jibo on 02/03/2016.
 */
public class PaletteActivity extends ThemedActivity {

    private Toolbar toolbar;
    private ImageView paletteImg;
    private Uri uri;

    private Palette palette;
    private RecyclerView rvPalette;
    private PaletteAdapter paletteAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        paletteImg = (ImageView) findViewById(R.id.palette_image);

        setSupportActionBar(toolbar);
        uri = Uri.parse(getIntent().getExtras().getString("imageUri"));
        paletteImg.setImageURI(null);
        paletteImg.setImageURI(uri);

        setPalette();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();

        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setStatusBarColor();
        setNavBarColor();
        setRecentApp(getString(R.string.palette));

        findViewById(R.id.palette_background).setBackgroundColor(getBackgroundColor());
        ((CardView) findViewById(R.id.palette_colors_card)).setCardBackgroundColor(getCardBackgroundColor());
        ((CardView) findViewById(R.id.palette_image_card)).setCardBackgroundColor(getCardBackgroundColor());
        ((TextView) findViewById(R.id.palette_image_title)).setTextColor(getTextColor());
        ((TextView) findViewById(R.id.palette_image_caption)).setTextColor(getSubTextColor());
    }

    public void setPalette(){
        Bitmap myBitmap = ((BitmapDrawable) paletteImg.getDrawable()).getBitmap();
        ((TextView) findViewById(R.id.palette_image_title)).setText(uri.getPath().substring(uri.getPath().lastIndexOf("/")+1));
        ((TextView)findViewById(R.id.palette_image_caption)).setText(uri.getPath());
        palette = Palette.generate(myBitmap);
        rvPalette = (RecyclerView) findViewById(R.id.paletteRecycler);
        rvPalette.setLayoutManager(new LinearLayoutManager(this));
        rvPalette.setNestedScrollingEnabled(false);
        paletteAdapter = new PaletteAdapter(palette.getSwatches());
        rvPalette.setAdapter(paletteAdapter);
    }

    /*** - PALETTE ADAPTER - ***/
    private class PaletteAdapter extends RecyclerView.Adapter<PaletteActivity.PaletteAdapter.ViewHolder> {

        private List<Palette.Swatch> swatches;
        private PaletteAdapter(List<Palette.Swatch> sws){this.swatches = sws;}

        public PaletteActivity.PaletteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.palette_item, parent, false);
            v.setOnClickListener(onClickListener);
            return new PaletteActivity.PaletteAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final PaletteActivity.PaletteAdapter.ViewHolder holder, final int position) {
            Palette.Swatch sw = swatches.get(position);
            holder.txtColor.setTextColor(sw.getTitleTextColor());
            holder.txtColor.setText(String.format("#%06X", (0xFFFFFF & sw.getRgb())));
            holder.itemBackground.setBackgroundColor(sw.getRgb());
        }

        public int getItemCount() {return null != swatches ? swatches.size() : 0;}

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtColor;
            LinearLayout itemBackground;
            ViewHolder(View itemView) {
                super(itemView);
                txtColor = (TextView) itemView.findViewById(R.id.palette_item_text);
                itemBackground = (LinearLayout) itemView.findViewById(R.id.ll_palette_item);
            }
        }
    }

    /*** - PALETTE ITEM ON CLICK - ***/
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String text = ((TextView) view.findViewById(R.id.palette_item_text)).getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Palette Color", text);
            clipboard.setPrimaryClip(clip);
            StringUtils.showToast(getApplicationContext(), getString(R.string.color) + ": " + text + " " + getString(R.string.copy_clipboard));
        }
    };
}