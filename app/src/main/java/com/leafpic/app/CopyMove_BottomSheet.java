package com.leafpic.app;

/**
 * Created by Jibo on 18/04/2016.
 */

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.leafpic.app.Base.Album;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

public class CopyMove_BottomSheet extends BottomSheetDialogFragment {


    RecyclerView mRecyclerView;
    TextView Title;
    LinearLayout background;
    ArrayList<Album> albumArrayList;
    SharedPreferences SP;

    public void setAlbumArrayList(ArrayList<Album> albumArrayList){ this.albumArrayList = albumArrayList; }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        //dialog.setTitle("pick an album:");
        View contentView = View.inflate(getContext(), R.layout.copy_move_bottom_sheet, null);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rv_modal_dialog_albums);
        mRecyclerView.setAdapter(new BottomSheetAlbumsAdapter(albumArrayList));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
        mRecyclerView.setLayoutManager(new GridLayoutManager(dialog.getContext(), 1));

        /**SET UP DIALOG THEME**/
        SP = PreferenceManager.getDefaultSharedPreferences(dialog.getContext());

        Title=(TextView) contentView.findViewById(R.id.bottom_sheet_title);
        Title.setText("Copy To");
        Title.setBackgroundColor(SP.getInt("accent_color",
                ContextCompat.getColor(dialog.getContext(), R.color.md_light_blue_500)));
        Title.setTextColor(ContextCompat.getColor(dialog.getContext(),R.color.md_white_1000));

        background = (LinearLayout) contentView.findViewById(R.id.ll_album_modal_dialog);
        background.setBackgroundColor(ContextCompat.getColor(dialog.getContext(),
                SP.getBoolean("set_dark_theme", false)
                        ? R.color.md_dark_cards
                        : R.color.md_light_cards));

        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    private class BottomSheetAlbumsAdapter extends RecyclerView.Adapter<BottomSheetAlbumsAdapter.ViewHolder> {

        ArrayList<Album> albums;

        public BottomSheetAlbumsAdapter(ArrayList<Album> ph){
            albums = ph;
        }

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String ID = v.getTag().toString();
                //Log.wtf("asd",ID);
            }
        };
        public BottomSheetAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.copy_move_bottom_sheet_item, parent, false);
            v.findViewById(R.id.ll_album_bottom_sheet_item).setOnClickListener(listener);
            return new ViewHolder(
                    MaterialRippleLayout.on(v)
                            .rippleOverlay(true)
                            .rippleAlpha(0.2f)
                            .rippleColor(0xFF585858)
                            .rippleHover(true)
                            .rippleDuration(1)
                            .create()
            );
        }

        @Override
        public void onBindViewHolder(final BottomSheetAlbumsAdapter.ViewHolder holder, final int position) {
            final Album a = albums.get(position);
            holder.album_name.setText(a.DisplayName);
            holder.album_media_count.setText(a.getImagesCount()+ a.getContentDescdription(getDialog().getContext()));
            holder.album_name.setTag(position);

            /**SET LAYOUT THEME**/
            SP = PreferenceManager.getDefaultSharedPreferences(getDialog().getContext());
            //getDialog().getContext()
            int textColor= ContextCompat.getColor(getDialog().getContext(),  SP.getBoolean("set_dark_theme", false)
                    ? R.color.md_grey_200
                    : R.color.md_grey_800);
            int subtextColor= ContextCompat.getColor(getDialog().getContext(),  SP.getBoolean("set_dark_theme", false)
                    ? R.color.md_grey_400
                    : R.color.md_grey_600);

            holder.album_name.setTextColor(textColor);

            String hexAccentColor = String.format("#%06X", (0xFFFFFF & SP.getInt("accent_color",
                    ContextCompat.getColor(getDialog().getContext(), R.color.md_light_blue_500))));

            holder.album_media_count.setText(Html.fromHtml("<b><font color='" + hexAccentColor + "'>"
                    + a.getImagesCount() + "</font></b>" + "<font " + "color='" + subtextColor + "'> "
                    + a.getContentDescdription(getDialog().getContext()) + "</font>"));

            holder.imgFolder.setColor(
                    ContextCompat.getColor(getDialog().getContext(),
                            SP.getBoolean("set_dark_theme", false)
                                    ? R.color.md_dark_primary_icon
                                    : R.color.md_light_primary_icon));
        }

        public int getItemCount() {
            return albums.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView album_name;
            TextView album_media_count;
            IconicsImageView imgFolder;
            public ViewHolder(View itemView) {
                super(itemView);
                album_name = (TextView) itemView.findViewById(R.id.Bottom_Sheet_Title_Item);
                album_media_count = (TextView) itemView.findViewById(R.id.Bottom_Sheet_Count_Item);
                imgFolder = (IconicsImageView) itemView.findViewById(R.id.bottom_sheet_folder_icon);
            }
        }
    }
}

