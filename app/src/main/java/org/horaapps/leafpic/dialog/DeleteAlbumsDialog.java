package org.horaapps.leafpic.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.filter.ImageFileFilter;

import java.io.File;
import java.util.ArrayList;

import horaapps.org.liz.ThemeHelper;

/**
 * Created by dnld on 6/29/17.
 */

public class DeleteAlbumsDialog extends DialogFragment {

    ThemeHelper t;
    TextView dialogTitle;
    TextView folderName;
    TextView fileName;
    ProgressBar progress;

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
        View view = inflater.inflate(R.layout.dialog_delete_album_progress, container, false);

        dialogTitle = view.findViewById(R.id.text_dialog_title);
        folderName = view.findViewById(R.id.folder_name);
        fileName = view.findViewById(R.id.file_name);
        progress = view.findViewById(R.id.progress_bar);

        ((CardView) view.findViewById(R.id.message_card)).setCardBackgroundColor(t.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(t.getPrimaryColor());

        //folderName.setText(Message);
        folderName.setTextColor(t.getTextColor());
        //builder.setView(view);

        return view;
    }

    public void setTitle(String title) {
        dialogTitle.setText(title);
    }


    class DeleteAlbums extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... arg0) {

            for (Album album : albums) {
                getActivity().runOnUiThread(() -> folderName.setText(album.getName()));
                File dir = new File(album.getPath());
                File[] files = dir.listFiles(new ImageFileFilter(Hawk.get("", true)));
                if (files != null && files.length > 0) {

                }

            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    /*@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        public static AlertDialog getTextDialog(ThemedActivity activity, @StringRes int title, @StringRes int Message){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity,activity.getDialogStyle());
            View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_delete_album_progress, null);

            TextView dialogTitle = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_title);
            TextView folderName = (TextView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.text_dialog_message);

            ((CardView) dialogLayout.findViewById(org.horaapps.leafpic.R.id.message_card)).setCardBackgroundColor(activity.getCardBackgroundColor());
            dialogTitle.setBackgroundColor(activity.getPrimaryColor());
            dialogTitle.setText(title);
            folderName.setText(Message);
            folderName.setTextColor(activity.getTextColor());
            builder.setView(dialogLayout);
            return builder.create();
        }
    }*/
}
