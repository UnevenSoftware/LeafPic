package org.horaapps.leafpic.timeline;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.timeline.data.TimelineHeaderModel;
import org.horaapps.leafpic.views.SquareRelativeLayout;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedViewHolder;
import org.horaapps.liz.ui.ThemedIcon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Class for holding the RecyclerView ViewHolders to be used in Timeline.
 */
public class ViewHolder {

    static abstract class TimelineViewHolder extends ThemedViewHolder {

        TimelineViewHolder(View view) {
            super(view);
        }
    }

    /**
     * ViewHolder for the Timeline headers
     */
    protected static class TimelineHeaderViewHolder extends TimelineViewHolder {

        @BindView(R.id.timeline_container_header) TextView headerText;

        TimelineHeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(@NonNull TimelineHeaderModel timelineHeaderModel) {
            headerText.setText(timelineHeaderModel.getHeaderText());
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            headerText.setTextColor(themeHelper.getTextColor());
        }
    }

    /**
     * ViewHolder for the Media items.
     */
    protected static class TimelineMediaViewHolder extends TimelineViewHolder {

        @BindView(R.id.photo_preview) ImageView imageView;
        @BindView(R.id.photo_path) TextView path;
        @BindView(R.id.gif_icon) ThemedIcon gifIcon;
        @BindView(R.id.icon) ThemedIcon icon;
        @BindView(R.id.media_card_layout) SquareRelativeLayout layout;

        private Drawable placeholderImage;

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            icon.setColor(Color.WHITE);
        }

        TimelineMediaViewHolder(View view, @NonNull Drawable placeholder) {
            super(view);
            ButterKnife.bind(this, view);
            this.placeholderImage = placeholder;
        }

        void bind(@NonNull Media mediaItem, boolean isSelected) {
            // TODO: Refactor this logic!
            icon.setVisibility(View.GONE);
            gifIcon.setVisibility(mediaItem.isGif() ? View.VISIBLE : View.GONE);

            RequestOptions options = new RequestOptions()
                    .signature(mediaItem.getSignature())
                    .format(DecodeFormat.PREFER_RGB_565)
                    .centerCrop()
                    .placeholder(placeholderImage)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(imageView.getContext())
                    .load(mediaItem.getUri())
                    .apply(options)
                    .thumbnail(0.5f)
                    .into(imageView);

            if (mediaItem.isVideo()) {
                icon.setIcon(GoogleMaterial.Icon.gmd_play_circle_filled);
                icon.setVisibility(View.VISIBLE);
                path.setVisibility(View.VISIBLE);
                path.setText(mediaItem.getName());
                icon.animate().alpha(1).setDuration(250);
                path.animate().alpha(1).setDuration(250);
            } else {
                icon.setVisibility(View.GONE);
                path.setVisibility(View.GONE);

                icon.animate().alpha(0).setDuration(250);
                path.animate().alpha(0).setDuration(250);
            }

            if (isSelected) {
                icon.setIcon(CommunityMaterial.Icon.cmd_check);
                icon.setVisibility(View.VISIBLE);
                imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
                layout.setPadding(15, 15, 15, 15);
                //ANIMS
                icon.animate().alpha(1).setDuration(250);
            } else {
                imageView.clearColorFilter();
                layout.setPadding(0, 0, 0, 0);
            }
        }
    }
}
