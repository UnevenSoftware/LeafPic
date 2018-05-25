package org.horaapps.leafpic.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemedAdapter;

import java.util.ArrayList;

/**
 * Created by dnld on 04/03/18.
 */

public class ContributorsAdapter extends ThemedAdapter<ContributorViewHolder> {

    private ContactListener listener;
    private ArrayList<Contributor> contributors;

    ContributorsAdapter(Context context, ArrayList<Contributor> contributors, ContactListener listener) {
        super(context);
        this.contributors = contributors;
        this.listener = listener;
    }

    @Override
    public ContributorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContributorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_about_contributor, parent, false));
    }

    @Override
    public void onBindViewHolder(ContributorViewHolder holder, int position) {
        Contributor contributor = contributors.get(position);
        holder.load(contributor, listener);
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return contributors.size();
    }

}
