package org.horaapps.leafpic.settings;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;

import horaapps.org.liz.ThemedActivity;

/**
 * Created by dnld on 12/9/16.
 */

public class GeneralSetting extends ThemedSetting {

    public GeneralSetting(ThemedActivity activity) {
        super(activity);
    }

    public void editNumberOfColumns(){
        AlertDialog.Builder multiColumnDialogBuilder = new AlertDialog.Builder(getActivity(), getActivity().getDialogStyle());
        View dialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_multi_column, null);

        ((TextView) dialogLayout.findViewById(R.id.folders_title)).setTextColor(getActivity().getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.media_title)).setTextColor(getActivity().getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.folders_title_landscape)).setTextColor(getActivity().getTextColor());
        ((TextView) dialogLayout.findViewById(R.id.media_title_landscape)).setTextColor(getActivity().getTextColor());
        ((CardView) dialogLayout.findViewById(R.id.multi_column_card)).setCardBackgroundColor(getActivity().getCardBackgroundColor());

        dialogLayout.findViewById(R.id.multi_column_title).setBackgroundColor(getActivity().getPrimaryColor());
        final TextView nColFolders = (TextView) dialogLayout.findViewById(R.id.n_columns_folders);
        final TextView nColMedia = (TextView) dialogLayout.findViewById(R.id.n_columns_media);
        final TextView nColFoldersL = (TextView) dialogLayout.findViewById(R.id.n_columns_folders_landscape);
        final TextView nColMediaL = (TextView) dialogLayout.findViewById(R.id.n_columns_media_landscape);

        nColFolders.setTextColor(getActivity().getSubTextColor());
        nColMedia.setTextColor(getActivity().getSubTextColor());
        nColFoldersL.setTextColor(getActivity().getSubTextColor());
        nColMediaL.setTextColor(getActivity().getSubTextColor());

        SeekBar barFolders = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_folders);
        SeekBar barMedia = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_media);
        SeekBar barFoldersL = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_folders_landscape);
        SeekBar barMediaL = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_media_landscape);

        getActivity().themeSeekBar(barFolders);
        getActivity().themeSeekBar(barMedia);
        getActivity().themeSeekBar(barFoldersL);
        getActivity().themeSeekBar(barMediaL);

        ///PORTRAIT
        nColFolders.setText(String.valueOf(Hawk.get("n_columns_folders", 2)));
        nColMedia.setText(String.valueOf(Hawk.get("n_columns_media", 3)));
        barFolders.setProgress(Hawk.get("n_columns_folders", 2) - 1);
        barMedia.setProgress(Hawk.get("n_columns_media", 3) - 2);
        barFolders.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColFolders.setText(String.valueOf(i+1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        barMedia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColMedia.setText(String.valueOf(i+2));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ///LANDSCAPE
        nColFoldersL.setText(String.valueOf(Hawk.get("n_columns_folders_landscape", 3)));
        nColMediaL.setText(String.valueOf(Hawk.get("n_columns_media_landscape", 4)));
        barFoldersL.setProgress(Hawk.get("n_columns_folders_landscape", 3) - 2);
        barMediaL.setProgress(Hawk.get("n_columns_media_landscape", 4) - 3);
        barFoldersL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColFoldersL.setText(String.valueOf(i+2));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        barMediaL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColMediaL.setText(String.valueOf(i+3));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        multiColumnDialogBuilder.setPositiveButton(getActivity().getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int nFolders = Integer.parseInt(nColFolders.getText().toString());
                int nMedia = Integer.parseInt(nColMedia.getText().toString());
                int nFoldersL = Integer.parseInt(nColFoldersL.getText().toString());
                int nMediaL = Integer.parseInt(nColMediaL.getText().toString());
                Hawk.put("n_columns_folders", nFolders);
                Hawk.put("n_columns_media", nMedia);
                Hawk.put("n_columns_folders_landscape", nFoldersL);
                Hawk.put("n_columns_media_landscape", nMediaL);
            }
        });
        multiColumnDialogBuilder.setNegativeButton(getActivity().getString(R.string.cancel).toUpperCase(), null);
        multiColumnDialogBuilder.setView(dialogLayout);
        multiColumnDialogBuilder.show();
    }
}
