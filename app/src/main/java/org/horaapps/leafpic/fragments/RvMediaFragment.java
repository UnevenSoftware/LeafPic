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
import android.support.annotation.NonNull;
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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.PaletteActivity;
import org.horaapps.leafpic.adapters.MediaAdapter;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.HandlingAlbums;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.data.filter.FilterMode;
import org.horaapps.leafpic.data.filter.MediaFilter;
import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.interfaces.MediaClickListener;
import org.horaapps.leafpic.progress.ProgressBottomSheet;
import org.horaapps.leafpic.util.Affix;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.AnimationUtils;
import org.horaapps.leafpic.util.DeviceUtils;
import org.horaapps.leafpic.util.LegacyCompatFileProvider;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.MimeTypeUtils;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedIcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 3/13/17.
 */

public class RvMediaFragment extends BaseFragment {

    public static final String TAG = "RvMediaFragment";
    private static final String BUNDLE_ALBUM = "album";

    @BindView(R.id.media) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    private MediaAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;

    private Album album;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            album = getArguments().getParcelable(BUNDLE_ALBUM);
            return;
        }

        album = savedInstanceState.getParcelable(BUNDLE_ALBUM);
    }

    public static RvMediaFragment make(Album album) {
        RvMediaFragment fragment = new RvMediaFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!clearSelected())
            updateToolbar();
        setUpColumns();
    }

    private void reload() {
        loadAlbum(album);
    }

    private void loadAlbum(Album album) {
        this.album = album;
        adapter.setupFor(album);
        CPHelper.getMedia(getContext(), album)
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
                            if (getNothingToShowListener() != null)
                                getNothingToShowListener().changedNothingToShow(getCount() == 0);
                            refresh.setRefreshing(false);
                        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(BUNDLE_ALBUM, album);
        super.onSaveInstanceState(outState);
    }

    private MediaClickListener listener;

    public void setListener(MediaClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rv_media, container, false);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        rv.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));

        adapter = new MediaAdapter(getContext(), album.settings.getSortingMode(), album.settings.getSortingOrder(), this);

        refresh.setOnRefreshListener(this::reload);
        rv.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reload();
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
        return DeviceUtils.isPortrait(getResources())
                ? Prefs.getMediaColumnsPortrait()
                : Prefs.getMediaColumnsLandscape();
    }

    private void updateToolbar() {
        if (getEditModeListener() != null) {
            if (editMode())
                getEditModeListener().changedEditMode(true, adapter.getSelectedCount(), adapter.getItemCount(), v -> adapter.clearSelected(), null);

            else getEditModeListener().changedEditMode(false, 0, 0, null, album.getName());
        }
    }

    public SortingMode sortingMode() {
        return album.settings.getSortingMode();
    }

    public SortingOrder sortingOrder() {
        return album.settings.getSortingOrder();
    }

    private HandlingAlbums db() {
        return HandlingAlbums.getInstance(getContext().getApplicationContext());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.grid_media, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sharePhotos).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_share)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.filter_menu).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_filter_list)));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

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

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.all_media_filter:
                album.setFilterMode(FilterMode.ALL);
                item.setChecked(true);
                reload();
                return true;

            case R.id.video_media_filter:
                album.setFilterMode(FilterMode.VIDEO);
                item.setChecked(true);
                reload();
                return true;

            case R.id.image_media_filter:
                album.setFilterMode(FilterMode.IMAGES);
                item.setChecked(true);
                reload();
                return true;

            case R.id.gifs_media_filter:
                album.setFilterMode(FilterMode.GIF);
                item.setChecked(true);
                reload();
                return true;

            case R.id.sharePhotos:
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

                HashMap<String, Integer> types = new HashMap<>();
                ArrayList<Uri> files = new ArrayList<>();

                for (Media f : adapter.getSelected()) {
                    String mimeType = MimeTypeUtils.getTypeMime(f.getMimeType());
                    int count = 0;
                    if (types.containsKey(mimeType)) {
                        count = types.get(mimeType);
                    }
                    types.put(mimeType, count);
                    files.add(LegacyCompatFileProvider.getUri(getContext(), f.getFile()));
                }

                Set<String> fileTypes = types.keySet();
                if (fileTypes.size() > 1) {
                    Toast.makeText(getContext(), R.string.waring_share_multiple_file_types, Toast.LENGTH_SHORT).show();
                }

                int max = -1;
                String type = null;
                for (String fileType : fileTypes) {
                    Integer count = types.get(fileType);
                    if (count > max) {
                        type = fileType;
                    }
                }

                intent.setType(type + "/*");

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_to)));
                return true;

            case R.id.set_as_cover:
                String path = adapter.getFirstSelected().getPath();
                album.setCover(path);
                db().setCover(album.getPath(), path);
                adapter.clearSelected();
                return true;

            case R.id.action_palette:
                Intent paletteIntent = new Intent(getActivity(), PaletteActivity.class);
                paletteIntent.setData(adapter.getFirstSelected().getUri());
                startActivity(paletteIntent);
                return true;

            case R.id.rename:
                final EditText editTextNewName = new EditText(getActivity());
                editTextNewName.setText(StringUtils.getPhotoNameByPath(adapter.getFirstSelected().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), editTextNewName, R.string.rename_photo_action);

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
                HandlingAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.NAME.getValue());
                album.setSortingMode(SortingMode.NAME);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                adapter.changeSortingMode(SortingMode.DATE);
                HandlingAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.DATE.getValue());
                album.setSortingMode(SortingMode.DATE);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                adapter.changeSortingMode(SortingMode.SIZE);
                HandlingAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.SIZE.getValue());
                album.setSortingMode(SortingMode.SIZE);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                adapter.changeSortingMode(SortingMode.NUMERIC);
                HandlingAlbums.getInstance(getContext()).setSortingMode(album.getPath(), SortingMode.NUMERIC.getValue());
                album.setSortingMode(SortingMode.NUMERIC);
                item.setChecked(true);
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                SortingOrder sortingOrder = SortingOrder.fromValue(item.isChecked());
                adapter.changeSortingOrder(sortingOrder);
                HandlingAlbums.getInstance(getContext()).setSortingOrder(album.getPath(), sortingOrder.getValue());
                album.setSortingOrder(sortingOrder);
                return true;

            case R.id.delete:
                showDeleteBottomSheet();
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
                        for (int i = 0; i < adapter.getSelectedCount(); i++) {
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

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogStyle());
                final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_affix, null);

                dialogLayout.findViewById(R.id.affix_title).setBackgroundColor(getPrimaryColor());
                ((CardView) dialogLayout.findViewById(R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

                //ITEMS
                final SwitchCompat swVertical = dialogLayout.findViewById(R.id.affix_vertical_switch);
                final SwitchCompat swSaveHere = dialogLayout.findViewById(R.id.save_here_switch);

                final LinearLayout llSwVertical = dialogLayout.findViewById(R.id.ll_affix_vertical);
                final LinearLayout llSwSaveHere = dialogLayout.findViewById(R.id.ll_affix_save_here);

                final RadioGroup radioFormatGroup = dialogLayout.findViewById(R.id.radio_format);

                final TextView txtQuality = dialogLayout.findViewById(R.id.affix_quality_title);
                final SeekBar seekQuality = dialogLayout.findViewById(R.id.seek_bar_quality);

                //region Example
                final LinearLayout llExample = dialogLayout.findViewById(R.id.affix_example);
                llExample.setBackgroundColor(getBackgroundColor());
                llExample.setVisibility(Prefs.getToggleValue(getContext().getString(R.string.preference_show_tips), true) ? View.VISIBLE : View.GONE);
                final LinearLayout llExampleH = dialogLayout.findViewById(R.id.affix_example_horizontal);
                //llExampleH.setBackgroundColor(getCardBackgroundColor());
                final LinearLayout llExampleV = dialogLayout.findViewById(R.id.affix_example_vertical);
                //llExampleV.setBackgroundColor(getCardBackgroundColor());


                //endregion

                //region THEME STUFF
                getThemeHelper().setScrollViewColor(dialogLayout.findViewById(R.id.affix_scrollView));

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
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_quality_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_format_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_vertical_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.save_here_icon)).setColor(color);

                //Example bg
                color = getCardBackgroundColor();
                dialogLayout.findViewById(R.id.affix_example_horizontal_txt1).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_horizontal_txt2).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_vertical_txt1).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_vertical_txt2).setBackgroundColor(color);

                seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
                seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));

                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_jpeg));
                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_png));
                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_webp));
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

    private void showDeleteBottomSheet() {
        ArrayList<Media> selected = adapter.getSelected();
        ArrayList<io.reactivex.Observable<Media>> sources = new ArrayList<>(selected.size());
        for (Media media : selected)
            sources.add(MediaHelper.deleteMedia(getContext().getApplicationContext(), media));

        ProgressBottomSheet<Media> bottomSheet = new ProgressBottomSheet.Builder<Media>(R.string.delete_bottom_sheet_title)
                .autoDismiss(false)
                .sources(sources)
                .listener(new ProgressBottomSheet.Listener<Media>() {
                    @Override
                    public void onCompleted() {
                        adapter.invalidateSelectedCount();
                    }

                    @Override
                    public void onProgress(Media item) {
                        adapter.removeSelectedMedia(item);
                    }
                })
                .build();

        bottomSheet.showNow(getChildFragmentManager(), null);
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
    public void onItemSelected(int position) {
        if (listener != null) listener.onMediaClick(RvMediaFragment.this.album, adapter.getMedia(), position);
    }

    @Override
    public void onSelectMode(boolean selectMode) {
        refresh.setEnabled(!selectMode);
        updateToolbar();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {
        getEditModeListener().onItemsSelected(selectionCount, totalCount);
    }

    @Override
    public boolean clearSelected() {
        return adapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }

    @Override
    public void updateMedia(ArrayList<Media> media) {
        if (adapter != null) {
            adapter.setMedia(media);
        }
    }
}
