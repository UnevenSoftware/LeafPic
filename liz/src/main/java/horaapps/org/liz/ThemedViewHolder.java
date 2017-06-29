package horaapps.org.liz;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dnld on 08/04/17.
 */

public abstract class ThemedViewHolder extends RecyclerView.ViewHolder implements Themed {

    public ThemedViewHolder(View view) {
        super(view);
    }

}