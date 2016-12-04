package org.horaapps.leafpic.activities.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.horaapps.leafpic.MyApplication;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.model.Album;
import org.horaapps.leafpic.model.HandlingAlbums;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ContentHelper;

/**
 * Created by dnld on 03/08/16.
 */

public abstract class SharedMediaActivity extends ThemedActivity {

    private int REQUEST_CODE_SD_CARD_PERMISSIONS = 42;

    public HandlingAlbums getAlbums() {
        return ((MyApplication) getApplicationContext()).getAlbums();
    }

    public Album getAlbum() {
        return ((MyApplication) getApplicationContext()).getAlbum();
    }

    public void requestSdCardPermissions() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, getDialogStyle());
        AlertDialogsHelper.getTextDialog(this, dialogBuilder,
                R.string.sd_card_write_permission_title, R.string.sd_card_permissions_message);

        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_SD_CARD_PERMISSIONS);
            }
        });
        dialogBuilder.show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SD_CARD_PERMISSIONS) {
                Uri treeUri = resultData.getData();
                // Persist URI in shared preference so that you can use it later.
                ContentHelper.saveSdCardInfo(getApplicationContext(), treeUri);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Toast.makeText(this, R.string.got_permission_wr_sdcard, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
