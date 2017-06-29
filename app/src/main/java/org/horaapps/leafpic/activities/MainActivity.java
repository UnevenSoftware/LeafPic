package org.horaapps.leafpic.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.SelectAlbumBuilder;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.ContentHelper;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.fragments.AlbumsFragment;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.leafpic.fragments.RvMediaFragment;
import org.horaapps.leafpic.util.Affix;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends SharedMediaActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    private PreferenceUtil SP;

    AlbumsFragment albumsFragment = new AlbumsFragment();

    @BindView(R.id.fab_camera) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.coordinator_main_layout) CoordinatorLayout mainLayout;

    private boolean pickMode = false;
    private boolean albumsMode = true;

    private View.OnClickListener photosOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Media m = (Media) v.findViewById(R.id.photo_path).getTag();
            if (!pickMode) {
                {
                    // TODO: 4/5/17 moveout
                    if(SP.getBoolean("video_instant_play", false) && m.isVideo()) {
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(ContentHelper.getUriForFile(getApplicationContext(), m.getFile()), m.getMimeType()));
                    } else {
                        getAlbum().setCurrentMedia(m);
                        Intent intent = new Intent(MainActivity.this, SingleMediaActivity.class);
                        intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                        startActivity(intent);
                    }
                }
            } else {
                setResult(RESULT_OK, new Intent().setData(m.getUri()));
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState != null)
            return;

        SP = PreferenceUtil.getInstance(getApplicationContext());

        initUi();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, albumsFragment, "albums")
                .commit();

    }

    private void displayAlbums(boolean hidden) {
        albumsMode = true;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        albumsFragment.displayAlbums(hidden);
    }

    public void displayMedia(Album album) {
        albumsMode = false;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, RvMediaFragment.make(album), "media")
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            fab.setVisibility(View.VISIBLE);
            fab.animate().translationY(fab.getHeight() * 2).start();
        } else
            fab.setVisibility(View.GONE);
    }

    public void goBackToAlbums() {
        albumsMode = true;
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportFragmentManager().popBackStack();
    }

    @Deprecated
    private boolean displayData(Intent data){

        // TODO: 3/25/17 pick porcodio
        pickMode = data.getBooleanExtra(SplashScreen.PICK_MODE, false);
        switch (data.getIntExtra(SplashScreen.CONTENT, SplashScreen.ALBUMS_BACKUP)) {

            case SplashScreen.PHOTOS_PREFETCHED:
                //TODO ask password if hidden
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //getAlbums().loadAlbums(getApplicationContext(), getAlbum().isHidden());
                    }
                }).start();
                return true;
        }
        return false;
    }

    private void initUi() {

        setSupportActionBar(toolbar);

        // TODO: 3/25/17 organize better
        /**** DRAWER ****/
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) { }
            public void onDrawerOpened(View drawerView) { }
        };


        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


        findViewById(R.id.ll_drawer_Donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DonateActivity.class));
            }
        });

        findViewById(R.id.ll_drawer_Setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        findViewById(R.id.ll_drawer_About).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        findViewById(R.id.ll_drawer_Default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                displayAlbums(false);
            }
        });

        findViewById(R.id.ll_drawer_hidden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: 3/25/17 redo
                if (Security.isPasswordOnHidden(getApplicationContext())){
                    Security.askPassword(MainActivity.this, new Security.PasswordInterface() {
                        @Override
                        public void onSuccess() {
                            drawer.closeDrawer(GravityCompat.START);
                            displayAlbums(true);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    drawer.closeDrawer(GravityCompat.START);
                    displayAlbums(true);
                }
            }
        });

        findViewById(R.id.ll_drawer_Wallpapers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        /**** FAB ***/
        fab.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (SP.getInt("last_version_code", 0) < BuildConfig.VERSION_CODE) {
                    String titleHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s <b>%s</b></font>", getTextColor(), getString(R.string.changelog), BuildConfig.VERSION_NAME),
                            buttonHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s</font>", getAccentColor(), getString(R.string.view).toUpperCase());
                    Snackbar snackbar = Snackbar
                            .make(mainLayout, StringUtils.html(titleHtml), Snackbar.LENGTH_LONG)
                            .setAction(StringUtils.html(buttonHtml), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialogsHelper.showChangelogDialog(MainActivity.this);
                                }
                            });
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(getBackgroundColor());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        snackbarView.setElevation(getResources().getDimension(R.dimen.snackbar_elevation));
                    snackbar.show();
                    SP.putInt("last_version_code", BuildConfig.VERSION_CODE);
                }
            }
        }).start();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        //TODO: MUST BE FIXED
        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());

        /**** SWIPE TO REFRESH ****/

        setStatusBarColor();
        setNavBarColor();

        fab.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fab.setVisibility(SP.getBoolean(getString(R.string.preference_show_fab), false) ? View.VISIBLE : View.GONE);
        mainLayout.setBackgroundColor(getBackgroundColor());

        setScrollViewColor((ScrollView) findViewById(R.id.drawer_scrollbar));
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));

        findViewById(R.id.Drawer_Header).setBackgroundColor(getPrimaryColor());
        findViewById(R.id.Drawer_Body).setBackgroundColor(getDrawerBackground());
        findViewById(R.id.drawer_scrollbar).setBackgroundColor(getDrawerBackground());
        findViewById(R.id.Drawer_Body_Divider).setBackgroundColor(getIconColor());

        /** TEXT VIEWS **/
        int color = getTextColor();
        ((TextView) findViewById(R.id.Drawer_Default_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_Setting_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_Donate_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_wallpapers_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_About_Item)).setTextColor(color);
        ((TextView) findViewById(R.id.Drawer_hidden_Item)).setTextColor(color);

        /** ICONS **/
        color = getIconColor();
        ((IconicsImageView) findViewById(R.id.Drawer_Default_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_Donate_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_Setting_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_wallpapers_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_About_Icon)).setColor(color);
        ((IconicsImageView) findViewById(R.id.Drawer_hidden_Icon)).setColor(color);

        setRecentApp(getString(R.string.app_name));
    }

    public void updateToolbar(String title, IIcon icon, View.OnClickListener onClickListener) {
        updateToolbar(title, icon);
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    public void updateToolbar(String title, IIcon icon) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getToolbarIcon(icon));
    }

    public void resetToolbar() {
        updateToolbar(
                getString(R.string.app_name),
                GoogleMaterial.Icon.gmd_menu,
                v -> drawer.openDrawer(GravityCompat.START));
    }

    public void nothingToShow(boolean status) {
        findViewById(R.id.nothing_to_show_placeholder).setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    public void checkNothing(boolean status) {
        //TODO: @jibo come vuo fare qua? o anzi sopra!
        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());

        if(status && SP.getInt("emoji_easter_egg", 0) == 1) {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.VISIBLE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.GONE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.GONE);
        }
    }

    //region MENU

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;




/*            case R.id.hideAlbumButton:
                final AlertDialog dialog = AlertDialogsHelper.getTextDialog(MainActivity.this,
                        hidden ? R.string.unhide : R.string.hide,
                        hidden ? R.string.unhide_album_message : R.string.hide_album_message);

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(hidden ? R.string.unhide : R.string.hide).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (albumsMode) {
                            if (hidden) getAlbums().unHideSelectedAlbums(getApplicationContext());
                            else getAlbums().hideSelectedAlbums(getApplicationContext());
                            albumsAdapter.notifyDataSetChanged();
                            supportInvalidateOptionsMenu();
                        } else {
                            if(hidden) getAlbums().unHideAlbum(getAlbum().getPath(), getApplicationContext());
                            else getAlbums().hideAlbum(getAlbum().getPath(), getApplicationContext());
                            displayAlbums(true);
                        }
                    }
                });

                if (!hidden) {
                    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.exclude).toUpperCase(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (albumsMode) {
                                getAlbums().excludeSelectedAlbums();
                                albumsAdapter.notifyDataSetChanged();
                                supportInvalidateOptionsMenu();
                            } else {
                                getAlbums().excludeAlbum(getAlbum().getPath());
                                displayAlbums(true);
                            }
                        }
                    });
                }
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;


            case R.id.delete_action:
                class DeletePhotos extends AsyncTask<String, Integer, Boolean> {
                    private AlertDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = AlertDialogsHelper.getProgressDialog(MainActivity.this, getString(R.string.delete), getString(R.string.deleting_images));
                        dialog.show();
                    }

                    @Override
                    protected Boolean doInBackground(String... arg0) {
                        if (albumsMode)
                            return getAlbums().deleteSelectedAlbums(MainActivity.this);
                        else {
                            if (editMode())
                                return getAlbum().deleteSelectedMedia(getApplicationContext());
                            else {
                                boolean succ = getAlbums().deleteAlbum(getAlbum(), getApplicationContext());
                                getAlbum().getMedia().clear();
                                return succ;
                            }
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result) {
                            if (albumsMode) {
                                albumsAdapter.clearSelected();
                                //albumsAdapter.notifyDataSetChanged();
                            } else {
                                if (getAlbum().getMedia().size() == 0) {
                                    getAlbums().removeCurrentAlbum();
                                    albumsAdapter.notifyDataSetChanged();
                                    displayAlbums();
                                } else
                                    oldMediaAdapter.swapDataSet(getAlbum().getMedia());
                            }
                        } else requestSdCardPermissions();

                        supportInvalidateOptionsMenu();
                        checkNothing();
                        dialog.dismiss();
                    }
                }


                final AlertDialog alertDialog = AlertDialogsHelper.getTextDialog(this, R.string.delete, albumsMode || !editMode() ? R.string.delete_album_message : R.string.delete_photos_message);

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Security.isPasswordOnDelete(getApplicationContext())) {

                            Security.askPassword(MainActivity.this, new Security.PasswordInterface() {
                                @Override
                                public void onSuccess() {
                                    new DeletePhotos().execute();
                                }

                                @Override
                                public void onError() {
                                    Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else new DeletePhotos().execute();
                    }
                });
                alertDialog.show();
                return true;*/

            //region Affix
            // TODO: 11/21/16 move away from here
            case  R.id.affix:

                //region Async MediaAffix
                class affixMedia extends AsyncTask<Affix.Options, Integer, Void> {
                    private AlertDialog dialog;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = AlertDialogsHelper.getProgressDialog(MainActivity.this, getString(R.string.affix), getString(R.string.affix_text));
                        dialog.show();
                    }

                    @Override
                    protected Void doInBackground(Affix.Options... arg0) {
                        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
                       /* for (int i = 0; i<getAlbum().getSelectedMediaCount(); i++) {
                            if(!getAlbum().getSelectedMedia(i).isVideo())
                                bitmapArray.add(getAlbum().getSelectedMedia(i).getBitmap());
                        }*/

                        if (bitmapArray.size() > 1)
                            Affix.AffixBitmapList(getApplicationContext(), bitmapArray, arg0[0]);
                        else runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.affix_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void result) {
                        //editMode = false;
                        //getAlbum().clearSelectedMedia();
                        dialog.dismiss();
                        supportInvalidateOptionsMenu();
                        //oldMediaAdapter.notifyDataSetChanged();
                        //new PreparePhotosTask().execute();
                    }
                }
                //endregion

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, getDialogStyle());
                final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_affix, null);

                dialogLayout.findViewById(R.id.affix_title).setBackgroundColor(getPrimaryColor());
                ((CardView) dialogLayout.findViewById(R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

                //ITEMS
                final SwitchCompat swVertical = (SwitchCompat) dialogLayout.findViewById(R.id.affix_vertical_switch);
                final SwitchCompat swSaveHere = (SwitchCompat) dialogLayout.findViewById(R.id.save_here_switch);

                final LinearLayout llSwVertical = (LinearLayout) dialogLayout.findViewById(R.id.ll_affix_vertical);
                final LinearLayout llSwSaveHere = (LinearLayout) dialogLayout.findViewById(R.id.ll_affix_save_here);

                final RadioGroup radioFormatGroup = (RadioGroup) dialogLayout.findViewById(R.id.radio_format);

                final TextView txtQuality = (TextView) dialogLayout.findViewById(R.id.affix_quality_title);
                final SeekBar seekQuality = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_quality);

                //region Example
                final LinearLayout llExample = (LinearLayout) dialogLayout.findViewById(R.id.affix_example);
                llExample.setBackgroundColor(getBackgroundColor());
                llExample.setVisibility(SP.getBoolean("show_tips", true) ? View.VISIBLE : View.GONE);
                final LinearLayout llExampleH = (LinearLayout) dialogLayout.findViewById(R.id.affix_example_horizontal);
                //llExampleH.setBackgroundColor(getCardBackgroundColor());
                final LinearLayout llExampleV = (LinearLayout) dialogLayout.findViewById(R.id.affix_example_vertical);
                //llExampleV.setBackgroundColor(getCardBackgroundColor());



                //endregion

                //region THEME STUFF
                setScrollViewColor((ScrollView) dialogLayout.findViewById(R.id.affix_scrollView));

                /** TextViews **/
                int color = getTextColor();
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.compression_settings_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.save_here_title)).setTextColor(color);

                //Example Stuff
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt1)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt2)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt1)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt2)).setTextColor(color);


                /** Sub TextViews **/
                color = getSubTextColor();
                ((TextView) dialogLayout.findViewById(R.id.save_here_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_format_sub)).setTextColor(color);
                txtQuality.setTextColor(color);

                /** Icons **/
                color = getIconColor();
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_quality_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_format_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.affix_vertical_icon)).setColor(color);
                ((IconicsImageView) dialogLayout.findViewById(R.id.save_here_icon)).setColor(color);

                //Example bg
                color=getCardBackgroundColor();
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt1)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt2)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt1)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt2)).setBackgroundColor(color);

                seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
                seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(),PorterDuff.Mode.SRC_IN));

                themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_jpeg));
                themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_png));
                themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_webp));
                setSwitchColor(getAccentColor(), swSaveHere, swVertical);
                //endregion

                seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtQuality.setText(StringUtils.html(String.format(Locale.getDefault(), "%s <b>%d</b>", getString(R.string.quality), progress)));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                seekQuality.setProgress(50);

                swVertical.setClickable(false);
                llSwVertical.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swVertical.setChecked(!swVertical.isChecked());
                        setSwitchColor(getAccentColor(), swVertical);
                        llExampleH.setVisibility(swVertical.isChecked() ? View.GONE : View.VISIBLE);
                        llExampleV.setVisibility(swVertical.isChecked() ? View.VISIBLE : View.GONE);
                    }
                });

                swSaveHere.setClickable(false);
                llSwSaveHere.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swSaveHere.setChecked(!swSaveHere.isChecked());
                        setSwitchColor(getAccentColor(), swSaveHere);
                    }
                });

                builder.setView(dialogLayout);
                builder.setPositiveButton(this.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bitmap.CompressFormat compressFormat;
                        switch (radioFormatGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_jpeg: default:
                                compressFormat = Bitmap.CompressFormat.JPEG; break;
                            case R.id.radio_png:
                                compressFormat = Bitmap.CompressFormat.PNG; break;
                            case R.id.radio_webp:
                                compressFormat = Bitmap.CompressFormat.WEBP; break;
                        }

                        Affix.Options options = new Affix.Options(
                                swSaveHere.isChecked() ? getAlbum().getPath() : Affix.getDefaultDirectoryPath(),
                                compressFormat,
                                seekQuality.getProgress(),
                                swVertical.isChecked());
                        new affixMedia().execute(options);
                    }});
                builder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
                builder.show();


                return true;
            //endregion

            case R.id.action_move:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.move_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                //swipeRefreshLayout.setRefreshing(true);
                                if (getAlbum().moveSelectedMedia(getApplicationContext(), path) > 0) {
                                    if (getAlbum().getMedia().size() == 0) {
                                        //getAlbums().removeCurrentAlbum();
                                        //albumsAdapter.notifyDataSetChanged();
                                        displayAlbums(false);
                                    }
                                    //oldMediaAdapter.swapDataSet(getAlbum().getMedia());
                                    //finishEditMode();
                                    supportInvalidateOptionsMenu();
                                } else requestSdCardPermissions();

                                //swipeRefreshLayout.setRefreshing(false);
                            }}).show();
                return true;

            case R.id.action_copy:
                SelectAlbumBuilder.with(getSupportFragmentManager())
                        .title(getString(R.string.copy_to))
                        .onFolderSelected(new SelectAlbumBuilder.OnFolderSelected() {
                            @Override
                            public void folderSelected(String path) {
                                boolean success = getAlbum().copySelectedPhotos(getApplicationContext(), path);
                                //finishEditMode();

                                if (!success) // TODO: 11/21/16 handle in other way
                                    requestSdCardPermissions();
                            }
                        }).show();

                return true;

            case R.id.rename:
                /*final EditText editTextNewName = new EditText(getApplicationContext());
                editTextNewName.setText(albumsMode ? firstSelectedAlbum.getName() : getAlbum().getName());

                final AlertDialog insertTextDialog = AlertDialogsHelper.getInsertTextDialog(MainActivity.this, editTextNewName, R.string.rename_album);

                insertTextDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        insertTextDialog.dismiss();
                    }
                });

                insertTextDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editTextNewName.length() != 0) {
                            swipeRefreshLayout.setRefreshing(true);
                            boolean success;
                            if (albumsMode) {

                                int index = getAlbums().albums.indexOf(firstSelectedAlbum);
                                getAlbums().getAlbum(index).updatePhotos(getApplicationContext());
                                success = getAlbums().getAlbum(index).renameAlbum(getApplicationContext(),
                                        editTextNewName.getText().toString());
                                albumsAdapter.notifyItemChanged(index);
                            } else {
                                success = getAlbum().renameAlbum(getApplicationContext(), editTextNewName.getText().toString());
                                toolbar.setTitle(getAlbum().getName());
                                oldMediaAdapter.notifyDataSetChanged();
                            }
                            insertTextDialog.dismiss();
                            if (!success) requestSdCardPermissions();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            StringUtils.showToast(getApplicationContext(), getString(R.string.insert_something));
                            editTextNewName.requestFocus();
                        }
                    }
                });

                insertTextDialog.show();*/
                return true;







            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {

        if (albumsMode) {
            if (!albumsFragment.onBackPressed()) {
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                else finish();
            }
        } else {
            if (!((BaseFragment) getSupportFragmentManager().findFragmentByTag("media")).onBackPressed())
               goBackToAlbums();
        }
    }
}
