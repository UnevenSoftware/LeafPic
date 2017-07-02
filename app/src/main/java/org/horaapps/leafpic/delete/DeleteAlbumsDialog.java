package org.horaapps.leafpic.delete;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.filter.ImageFileFilter;

import java.io.File;
import java.util.ArrayList;

import horaapps.org.liz.ThemeHelper;
import horaapps.org.liz.ThemedAlertDialogBuilder;

/**
 * Created by dnld on 6/29/17.
 */

public class DeleteAlbumsDialog extends DialogFragment {

    ThemeHelper t;
    TextView dialogTitle;
    TextView fileName;
    ProgressBar progress;

    FolderAdapter adapter;

    ArrayList<Folder> albums = new ArrayList<>();

    AsyncTask<String, Integer, Boolean> deleteTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        t = ThemeHelper.getInstanceLoaded(getActivity());

        ArrayList<Album> asd = getArguments().getParcelableArrayList("albums");

        if (asd != null)
            for (Album album : asd) albums.add(new Folder(album.getPath(), album.getCount()));
        setCancelable(false);
    }

    public void setTitle(String title) {
        dialogTitle.setText(title);
    }


    class DeleteAlbums extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            progress.setIndeterminate(false);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... arg0) {

            for (int i = 0; i < albums.size(); i++) {
                Folder al = albums.get(i);
                File dir = new File(al.getPath());
                File[] files = dir.listFiles(new ImageFileFilter(Hawk.get("", true)));
                if (files != null && files.length > 0) {
                    final int fini = i;
                    al.setProgress(0);
                    al.setCount(files.length);
                    if (isCancelled()) break;

                    try {
                        getActivity().runOnUiThread(() -> {
                            progress.setMax(files.length);
                            progress.setProgress(0);
                            adapter.notifyItemChanged(fini);
                        });
                    } catch (NullPointerException ig) {
                        Log.wtf("asd", ig);
                    }


                    for (int j = 0; j < files.length; j++) {
                        final int finJ = j;
                        //ContentHelper.deleteFile(getContext(), files[j]);
                        if (isCancelled()) break;

                        try {
                            Thread.sleep(100);
                            Log.wtf("asd", files[finJ].getPath());
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }


                        al.setProgress(j + 1);

                        try {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyItemChanged(fini);
                                progress.setProgress(finJ + 1);
                                fileName.setText(files[finJ].getPath());
                            });
                        } catch (NullPointerException ignored) {
                        }
                    }

                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {

        }
    }

    @Override
    public void onStop() {
        Log.wtf("asd", "stop");
        if (deleteTask != null && deleteTask.getStatus() != AsyncTask.Status.FINISHED)
            deleteTask.cancel(true);
        super.onStop();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ThemedAlertDialogBuilder builder = new ThemedAlertDialogBuilder(getActivity(), ThemeHelper.getInstanceLoaded(getContext()));


        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_delete_album_progress, null, false);

        RecyclerView rv = view.findViewById(R.id.rv_folders);
        adapter = new FolderAdapter(getContext(), albums);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        dialogTitle = view.findViewById(R.id.text_dialog_title);

        fileName = view.findViewById(R.id.file_name);
        progress = view.findViewById(R.id.progress_bar);

        ((CardView) view.findViewById(R.id.message_card)).setCardBackgroundColor(t.getCardBackgroundColor());
        dialogTitle.setBackgroundColor(t.getPrimaryColor());

        progress.setMax(80);
        progress.setProgress(50);

        builder.setView(view);

        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            Toast.makeText(getContext(), "No Way", Toast.LENGTH_SHORT).show();
            dialogInterface.dismiss();
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        deleteTask = new DeleteAlbums().execute();
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
