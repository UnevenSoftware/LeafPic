package com.leafpic.app.Fragments;

/**
 * Created by Jibo on 18/04/2016.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.leafpic.app.Base.Album;
import com.leafpic.app.R;

import java.util.ArrayList;

public class CustomModalDialog extends BottomSheetDialogFragment {


    RecyclerView mRecyclerView;
    ArrayList<Album> albumArrayList;

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
        View contentView = View.inflate(getContext(), R.layout.album_modal_dialog, null);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.rv_modal_dialog_albums);
        mRecyclerView.setAdapter(new BottomSheetAlbumsAdapter(albumArrayList));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
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
                String ID = v.getTag().toString();
                Log.wtf("asd",ID);
            }
        };
        public BottomSheetAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_album_item, parent, false);
            v.findViewById(R.id.album_Title_Item).setOnClickListener(listener);
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
            holder.album_name.setTag(position);
        }

        public int getItemCount() {
            return albums.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView album_name;
            public ViewHolder(View itemView) {
                super(itemView);
                album_name = (TextView) itemView.findViewById(R.id.album_Title_Item);
            }
        }
    }
}

