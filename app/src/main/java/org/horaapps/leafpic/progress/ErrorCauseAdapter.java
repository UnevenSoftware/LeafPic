package org.horaapps.leafpic.progress;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemedAdapter;

import java.util.ArrayList;

public class ErrorCauseAdapter extends ThemedAdapter<ErrorCauseViewHolder> {

    private ArrayList<ErrorCause> errors;

    public ErrorCauseAdapter(Context context) {
        super(context);
    }

    public void setErrors(ArrayList<ErrorCause> errors) {
        this.errors = errors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ErrorCauseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ErrorCauseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_error_cause, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorCauseViewHolder holder, int position) {
        holder.load(errors.get(position));
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return errors.size();
    }
}
