package com.leafpic.app.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leafpic.app.R;

/**
 * Created by Jibo on 21/02/2016.
 */
public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {
    private int layout_ID;
    private String Theme;
    private String PrimaryColor;
    private String AccentColor;

    boolean selected = false;
    private View.OnClickListener mOnClickListener;

    //COSTRUTTORE
    public ThemeAdapter(int id, String ac, String pc, String thm) {
        layout_ID = id;
        Theme = thm;
        PrimaryColor = pc;
        AccentColor = ac;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout_ID, parent, false);
        v.setOnClickListener(mOnClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ThemeAdapter.ViewHolder holder, int position) {

        //Photo f = photos.get(position);
        //Glide.clear(holder.imageView);//fix corruption
        /*
        if (f.isGif()) {
            Ion.with(holder.imageView.getContext())
                    .load(f.Path)
                    .intoImageView(holder.imageView);
            holder.gifIcon.setVisibility(View.VISIBLE);
        } else {
            holder.gifIcon.setVisibility(View.INVISIBLE);
            Glide.with(holder.imageView.getContext())
                    .load(f.Path)
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.ic_empty)
                    .into(holder.imageView);

        }

        holder.path.setTag(f.Path);

        if (f.isSelected()) {
            holder.selectHolder.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.imageView.setPadding(15,15,15,15);
        } else {
            holder.selectHolder.setVisibility(View.INVISIBLE);
            holder.imageView.clearColorFilter();
            holder.imageView.setPadding(0,0,0,0);
        }
        */
    }

    @Override
    public int getItemCount() {
        return 5;
        /*photos.size()*/
    }

    public void setOnClickListener(View.OnClickListener lis) {
        mOnClickListener = lis;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView navIcon, menuIcon, selectHolder;
        LinearLayout card_layout;

        public ViewHolder(View itemView) {
            super(itemView);
            //imageView = (ImageView) itemView.findViewById(R.id.photo_preview);
            selectHolder = (ImageView) itemView.findViewById(R.id.selected_icon);
            menuIcon = (ImageView) itemView.findViewById(R.id.menu_icon);
            navIcon = (ImageView) itemView.findViewById(R.id.nav_icon);
            card_layout = (LinearLayout) itemView.findViewById(R.id.linear_card_text);
        }
    }
}
