package com.leafpic.app.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.leafpic.app.R;
import com.leafpic.app.utils.ColorPalette;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

/**
 * Created by Jibo on 04/04/2016.
 */
public class ExcludedAlbumsAdapter extends RecyclerView.Adapter<ExcludedAlbumsAdapter.ViewHolder> {

    ArrayList<String> AlbumPath;
    SharedPreferences SP;

    public ExcludedAlbumsAdapter(ArrayList<String> ph, Context ctx){
        AlbumPath = ph;
        SP = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    public ExcludedAlbumsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.excluded_item, parent, false);
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
    public void onBindViewHolder(final ExcludedAlbumsAdapter.ViewHolder holder, int position) {
        String Path = AlbumPath.get(position);
        Context c = holder.album_path.getContext();//picture

        holder.album_path.setText(Path);
        holder.album_name.setText(Path);

        /**SET LAYOUT THEME**/
        int color = SP.getBoolean("set_dark_theme", true)
                ? ColorPalette.getLightBackgroundColor(c)
                : ColorPalette.getDarkBackgroundColor(c);
        holder.imgFolder.setColor(color);
        holder.imgUnExclude.setColor(color);
        holder.album_name.setTextColor(color);

        color = ContextCompat.getColor(c, SP.getBoolean("set_dark_theme", true)
                ? R.color.md_grey_400
                : R.color.md_grey_600);
        holder.album_path.setTextColor(color);

        color = ContextCompat.getColor(c, SP.getBoolean("set_dark_theme", true)
                ? R.color.md_dark_cards
                : R.color.md_light_cards);
        holder.card_layout.setBackgroundColor(color);
    }

    public int getItemCount() {
        return AlbumPath.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_layout;
        IconicsImageView imgUnExclude;
        IconicsImageView imgFolder;
        TextView album_name;
        TextView album_path;

        public ViewHolder(View itemView) {
            super(itemView);
            card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_excluded);
            imgUnExclude = (IconicsImageView) itemView.findViewById(R.id.UnExclude_icon);
            imgFolder = (IconicsImageView) itemView.findViewById(R.id.folder_icon);
            album_name = (TextView) itemView.findViewById(R.id.Excluded_Title_Item);
            album_path = (TextView) itemView.findViewById(R.id.Excluded_Path_Item);
        }
    }


}
