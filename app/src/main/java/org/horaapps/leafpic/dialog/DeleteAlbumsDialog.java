package org.horaapps.leafpic.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;

import java.util.ArrayList;

import horaapps.org.liz.ThemeHelper;

/**
 * Created by dnld on 6/29/17.
 */

public class DeleteAlbumsDialog extends DialogFragment {

    ThemeHelper t;
    TextView dialogTitle;
    TextView dialogMessage;

    ArrayList<Album> albums;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        t = ThemeHelper.getInstanceLoaded(getActivity());
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(org.horaapps.leafpic.R.layout.dialog_text, container, false);

        dialogTitle = view.findViewById(R.id.text_dialog_title);
        dialogMessage = view.findViewById(R.id.text_dialog_message);

        ((CardView) view.findViewById(R.id.message_card)).setCardBackgroundColor(t.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(t.getPrimaryColor());

        //dialogMessage.setText(Message);
        dialogMessage.setTextColor(t.getTextColor());
        //builder.setView(view);

        return view;
    }

    public void setTitle(String title) {
        dialogTitle.setText(title);
    }

    /*@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        public static AlertDialog getTextDialog(ThemedActivity activity, @StringRes int title, @StringRes int Message){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity,activity.getDialogStyle());
            View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

            TextView dialogTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_title);
            TextView dialogMessage = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_message);

            ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
            dialogTitle.setBackgroundColor(activity.getPrimaryColor());
            dialogTitle.setText(title);
            dialogMessage.setText(Message);
            dialogMessage.setTextColor(activity.getTextColor());
            builder.setView(dialogLayout);
            return builder.create();
        }
    }*/
}
