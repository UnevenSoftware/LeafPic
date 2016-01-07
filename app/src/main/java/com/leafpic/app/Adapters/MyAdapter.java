package com.leafpic.app.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.leafpic.app.R;

/**
 * Created by dnld on 1/7/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


    private String mNavTitles[];
    private int mIcons[];

    public MyAdapter(String Titles[], int Icons[]) {
        mNavTitles = Titles;
        mIcons = Icons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {

        holder.textView.setText(mNavTitles[position]);
        holder.imageView.setImageResource(R.mipmap.ic_local_storage);
    }


    @Override
    public int getItemCount() {
        return mNavTitles.length;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.rowText);
            imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
        }
    }
}



