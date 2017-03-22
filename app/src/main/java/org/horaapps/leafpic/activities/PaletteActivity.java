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
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.CustomTabService;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.StringUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.horaapps.leafpic.R.string.path;

/**
 * Created by Jibo on 02/03/2016.
 */
public class PaletteActivity extends ThemedActivity {

    private Toolbar toolbar;
    private ScrollView scr;
    private ImageView paletteImg;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        scr = (ScrollView)findViewById(R.id.paletteAct_scrollView);
        paletteImg=(ImageView) findViewById(R.id.palette_image);

        initUi();
        setUpActions();
        setPalette();
    }

    private void setUpActions() {

    }

    private void initUi() {
        setSupportActionBar(toolbar);
        uri = Uri.parse(getIntent().getExtras().getString("imageUri"));
        paletteImg.setImageURI(null);
        paletteImg.setImageURI(uri);
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

        setScrollViewColor(scr);
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

        Palette palette = Palette.generate(myBitmap);
        int def = 0x000000;
        //TODO: It will be done better i swear
        TextView txtVibrant = (TextView) findViewById(R.id.palette_vibrant);
        TextView txtVibrantDark = (TextView) findViewById(R.id.palette_vibrant_dark);
        TextView txtVibrantLight = (TextView) findViewById(R.id.palette_vibrant_light);
        TextView txtMuted = (TextView) findViewById(R.id.palette_muted);
        TextView txtMutedDark = (TextView) findViewById(R.id.palette_muted_dark);
        TextView txtMutedLight = (TextView) findViewById(R.id.palette_muted_light);

        txtVibrant.setBackgroundColor(palette.getVibrantColor(def));
        txtVibrantDark.setBackgroundColor(palette.getDarkVibrantColor(def));
        txtVibrantLight.setBackgroundColor(palette.getLightVibrantColor(def));
        txtMuted.setBackgroundColor(palette.getMutedColor(def));
        txtMutedDark.setBackgroundColor(palette.getDarkMutedColor(def));
        txtMutedLight.setBackgroundColor(palette.getLightMutedColor(def));

        txtVibrant.setText(String.format("#%06X", (0xFFFFFF & palette.getVibrantColor(def))));
        txtVibrantDark.setText(String.format("#%06X", (0xFFFFFF & palette.getDarkVibrantColor(def))));
        txtVibrantLight.setText(String.format("#%06X", (0xFFFFFF & palette.getLightVibrantColor(def))));
        txtMuted.setText(String.format("#%06X", (0xFFFFFF & palette.getMutedColor(def))));
        txtMutedDark.setText(String.format("#%06X", (0xFFFFFF & palette.getDarkMutedColor(def))));
        txtMutedLight.setText(String.format("#%06X", (0xFFFFFF & palette.getLightMutedColor(def))));

        txtVibrant.setVisibility( txtVibrant.getText().toString() != "#000000" ? View.VISIBLE : View.GONE);
        txtVibrantDark.setVisibility( txtVibrantDark.getText().toString() != "#000000" ? View.VISIBLE: View.GONE);
        txtVibrantLight.setVisibility( txtVibrantLight.getText().toString() != "#000000" ? View.VISIBLE : View.GONE);
        txtMuted.setVisibility( txtMuted.getText().toString() !="#000000" ? View.VISIBLE : View.GONE);
        txtMutedDark.setVisibility( txtMutedDark.getText().toString() != "#000000" ? View.VISIBLE : View.GONE);
        txtMutedLight.setVisibility( txtMutedLight.getText().toString() != "#000000" ? View.VISIBLE : View.GONE);

    }

    //@Override
    public void onClick ( View v ) {
        TextView txt = (TextView)v;
        copyClipboard(txt.getText().toString());
    }

    private void copyClipboard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Palette Color", text);
        clipboard.setPrimaryClip(clip);
        StringUtils.showToast(getApplicationContext(), getString(R.string.color) + ": " + text + " " + getString(R.string.copy_clipboard));
    }
}