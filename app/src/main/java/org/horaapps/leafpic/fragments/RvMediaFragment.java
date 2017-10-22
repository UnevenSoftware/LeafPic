package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.MainActivity;
import org.horaapps.leafpic.activities.PaletteActivity;
import org.horaapps.leafpic.activities.SingleMediaActivity;
import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.adapters.ProgressAdapter;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.AlbumsHelper;
import org.horaapps.leafpic.data.HandlingAlbums;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.data.filter.FilterMode;
import org.horaapps.leafpic.data.filter.MediaFilter;
import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.util.Affix;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.file.DeleteException;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 3/13/17.
 */

public class RvMediaFragment extends BaseFragment {

    private static final String TAG = "asd";

    @BindView(R.id.media) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private MediaAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;

    private MainActivity act;

    private Album album;

    public static RvMediaFragment make(Album album) {
        RvMediaFragment f = new RvMediaFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("album", album);
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        album = getArguments().getParcelable("album");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        act = ((MainActivity) context);
    }

    @Override
    public void onResume() {
        super.onResume();
        clearSelected();
        updateToolbar();
        setUpColumns();
    }

    private void display() {

        adapter.clear();


        CPHelper.getMedia(getContext(), album, sortingMode(), sortingOrder())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(album.filterMode()).accept(media))
                .subscribe(media -> adapter.add(media),
                        throwable -> {
                            refresh.setRefreshing(false);
                            Log.wtf("asd", throwable);
                        },
                        () -> {
                            album.setCount(getCount());
                            act.nothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);
                        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rv_media, null);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        rv.setItemAnimator(new LandingAnimator(new OvershootInterpolator(1f)));

        adapter = new MediaAdapter(
                getContext(), sortingMode(), sortingOrder());

        adapter.getClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pos -> {

                    Intent intent = new Intent(getActivity(), SingleMediaActivity.class);
                    intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                    intent.putExtra("album", RvMediaFragment.this.album);
                    intent.putExtra("media", adapter.getMedia());
                    intent.putExtra("position", pos);

                    getContext().startActivity(intent);
                    //Toast.makeText(getContext(), album.toString(), Toast.LENGTH_SHORT).show();
                });

        adapter.getSelectedClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(album -> {
                    updateToolbar();
                    getActivity().invalidateOptionsMenu();
                });

        refresh.setOnRefreshListener(this::display);
        rv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        display();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            ((GridLayoutManager) rv.getLayoutManager()).getSpanCount();
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
            rv.addItemDecoration(spacingDecoration);
        }
    }

    public int columnsCount() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? Hawk.get("n_columns_media", 3)
                : Hawk.get("n_columns_media_landscape", 4);
    }

    private void updateToolbar() {
        if (editMode())
            act.updateToolbar(
                    String.format(Locale.ENGLISH, "%d/%d",
                            adapter.getSelectedCount(), adapter.getItemCount()),
                    GoogleMaterial.Icon.gmd_check,
                    v -> adapter.clearSelected());
        else act.updateToolbar(
                album.getName(),
                GoogleMaterial.Icon.gmd_arrow_back,
                v -> act.goBackToAlbums());
    }

    public SortingMode sortingMode() {
        return adapter != null
                ? adapter.sortingMode()
                : album.settings.getSortingMode();
    }

    public SortingOrder sortingOrder() {
        return adapter != null
                ? adapter.sortingOrder()
                : album.settings.getSortingOrder();
    }

    private HandlingAlbums db() {
        return HandlingAlbums.getInstance(getContext().getApplicationContext());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grid_media, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sharePhotos).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_share)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.filter_menu).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_filter_list)));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(R.id.general_album_items, !editMode);
        menu.setGroupVisible(R.id.edit_mode_items, editMode);
        menu.setGroupVisible(R.id.one_selected_items, oneSelected);

        menu.findItem(R.id.select_all).setTitle(
                getSelectedCount() == getCount()
                        ? R.string.clear_selected
                        : R.string.select_all);
        if (editMode) {
            menu.findItem(R.id.filter_menu).setVisible(false);
            menu.findItem(R.id.sort_action).setVisible(false);
        } else {
            menu.findItem(R.id.filter_menu).setVisible(true);
            menu.findItem(R.id.sort_action).setVisible(true);

            menu.findItem(R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(R.id.numeric_sort_mode).setChecked(true); break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.all_media_filter:
                album.setFilterMode(FilterMode.ALL);
                item.setChecked(true);
                display();
                return true;

            case R.id.video_media_filter:
                album.setFilterMode(FilterMode.VIDEO);
                item.setChecked(true);
                display();
                return true;

            case R.id.image_media_filter:
                album.setFilterMode(FilterMode.IMAGES);
                item.setChecked(true);
                display();
                return true;

            case R.id.gifs_media_filter:
                album.setFilterMode(FilterMode.GIF);
                item.setChecked(true);
                display();
                return true;

            case R.id.sharePhotos:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sent_to_action));

                ArrayList<Uri> files = new ArrayList<>();
                for (Media f : adapter.getSelected())
                    files.add(f.getUri());

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setType("*/*");
                startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                return true;

            case R.id.set_as_cover:
                String path = adapter.getFirstSelected().getPath();
                album.setCover(path);
                db().setCover(album.getPath(), path);
                adapter.clearSelected();
                return true;

            case R.id.action_palette:
                Intent paletteIntent = new Intent(act, PaletteActivity.class);
                paletteIntent.setData(adapter.getFirstSelected().getUri());
                startActivity(paletteIntent);
                return true;

            case R.id.rename:
                final EditText editTextNewName = new EditText(getActivity());
                editTextNewName.setText(StringUtils.getPhotoNameByPath(adapter.getFirstSelected().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(act, editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), (dialog, which) -> {
                    if (editTextNewName.length() != 0) {
                        boolean b = MediaHelper.renameMedia(getActivity(), adapter.getFirstSelected(), editTextNewName.getText().toString());
                        if (!b) {
                            StringUtils.showToast(getActivity(), getString(R.string.rename_error));
                            //adapter.notifyDataSetChanged();
                        } else
                            adapter.clearSelected(); // Deselect media if rename successful
                    } else
                        StringUtils.showToast(getActivity(), getString(R.string.nothing_changed));
                });
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), (dialog, which) -> dialog.dismiss());
                renameDialog.show();
                return true;

            case R.id.select_all:
                if (adapter.getSelectedCount() == adapter.getItemCount())
                    adapter.clearSelected();
                else adapter.selectAll();
                return true;

            case R.id.name_sort_mode:
                adapter.changeSortingMode(SortingMode.NAME);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.NAME);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                adapter.changeSortingMode(SortingMode.DATE);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.DATE);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                adapter.changeSortingMode(SortingMode.SIZE);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                adapter.changeSortingMode(SortingMode.NUMERIC);
                AlbumsHelper.setSortingMode(getContext(), SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                adapter.changeSortingOrder(sortingOrder);
                AlbumsHelper.setSortingOrder(getContext(), sortingOrder);
                return true;

            case R.id.delete:

                ProgressAdapter errorsAdapter = new ProgressAdapter(getContext());
                ArrayList<Media> selected = adapter.getSelected();

                AlertDialog alertDialog = AlertDialogsHelper.getProgressDialogWithErrors(((ThemedActivity) getActivity()), R.string.deleting_images, errorsAdapter, selected.size());

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.cancel).toUpperCase(), (dialog, id) -> {
                    alertDialog.dismiss();
                });
                alertDialog.show();

                MediaHelper.deleteMedia(getContext(), selected)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(m -> {
                                    adapter.remove(m);
                                    errorsAdapter.add(new ProgressAdapter.ListItem(m.getName()), false);
                                },
                                throwable -> {
                                    if (throwable instanceof DeleteException)
                                        errorsAdapter.add(new ProgressAdapter.ListItem(
                                                (DeleteException) throwable), true);
                                },
                                () -> {
                                    if (errorsAdapter.getItemCount() == 0)
                                        alertDialog.dismiss();
                                    adapter.clearSelected();
                                });
                return true;

            //region Affix
            // TODO: 11/21/16 move away from here
            case R.id.affix:

                //region Async MediaAffix
                class affixMedia extends AsyncTask<Affix.Options, Integer, Void> {
                    private AlertDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = AlertDialogsHelper.getProgressDialog((ThemedActivity) getActivity(), getString(R.string.affix), getString(R.string.affix_text));
                        dialog.show();
                    }

                    @Override
                    protected Void doInBackground(Affix.Options... arg0) {
                        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
                       for (int i = 0; i<adapter.getSelectedCount(); i++) {
                            if(!adapter.getSelected().get(i).isVideo())
                                bitmapArray.add(adapter.getSelected().get(i).getBitmap());
                       }

                        if (bitmapArray.size() > 1)
                            Affix.AffixBitmapList(getActivity(), bitmapArray, arg0[0]);
                        else getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), R.string.affix_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        adapter.clearSelected();
                        dialog.dismiss();
                    }
                }
                //endregion

                final AlertDialog.Builder builder = new AlertDialog.Builder((ThemedActivity) getActivity(), getDialogStyle());
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
                llExample.setVisibility(Hawk.get("show_tips", true) ? View.VISIBLE : View.GONE);
                final LinearLayout llExampleH = (LinearLayout) dialogLayout.findViewById(R.id.affix_example_horizontal);
                //llExampleH.setBackgroundColor(getCardBackgroundColor());
                final LinearLayout llExampleV = (LinearLayout) dialogLayout.findViewById(R.id.affix_example_vertical);
                //llExampleV.setBackgroundColor(getCardBackgroundColor());


                //endregion

                //region THEME STUFF
                getThemeHelper().setScrollViewColor((ScrollView) dialogLayout.findViewById(R.id.affix_scrollView));

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
                color = getThemeHelper().getSubTextColor();
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
                color = getCardBackgroundColor();
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt1)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt2)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt1)).setBackgroundColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt2)).setBackgroundColor(color);

                seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
                seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));

                getThemeHelper().themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_jpeg));
                getThemeHelper().themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_png));
                getThemeHelper().themeRadioButton((RadioButton) dialogLayout.findViewById(R.id.radio_webp));
                getThemeHelper().setSwitchCompactColor( swSaveHere, getAccentColor());
                getThemeHelper().setSwitchCompactColor( swVertical, getAccentColor());
                //#endregion

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
                        getThemeHelper().setSwitchCompactColor(swVertical, getAccentColor());
                        llExampleH.setVisibility(swVertical.isChecked() ? View.GONE : View.VISIBLE);
                        llExampleV.setVisibility(swVertical.isChecked() ? View.VISIBLE : View.GONE);
                    }
                });

                swSaveHere.setClickable(false);
                llSwSaveHere.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swSaveHere.setChecked(!swSaveHere.isChecked());
                        getThemeHelper().setSwitchCompactColor(swSaveHere, getAccentColor());
                    }
                });

                builder.setView(dialogLayout);
                builder.setPositiveButton(this.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bitmap.CompressFormat compressFormat;
                        switch (radioFormatGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_jpeg:
                            default:
                                compressFormat = Bitmap.CompressFormat.JPEG;
                                break;
                            case R.id.radio_png:
                                compressFormat = Bitmap.CompressFormat.PNG;
                                break;
                            case R.id.radio_webp:
                                compressFormat = Bitmap.CompressFormat.WEBP;
                                break;
                        }

                        Affix.Options options = new Affix.Options(
                                swSaveHere.isChecked() ? adapter.getFirstSelected().getPath() : Affix.getDefaultDirectoryPath(),
                                compressFormat,
                                seekQuality.getProgress(),
                                swVertical.isChecked());
                        new affixMedia().execute(options);
                    }
                });
                builder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
                builder.show();
                return true;
            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    public int getCount() {
        return adapter.getItemCount();
    }

    public int getSelectedCount() {
        return adapter.getSelectedCount();
    }

    @Override
    public boolean editMode() {
        return adapter.selecting();
    }

    @Override
    public void clearSelected() {
        adapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }
}
