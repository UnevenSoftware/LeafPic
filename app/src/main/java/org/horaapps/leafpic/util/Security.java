package org.horaapps.leafpic.util;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;

import java.security.MessageDigest;

/**
 * Created by Jibo on 06/05/2016.
 */
public class Security {

    public static void setPasswordOnDelete(Context context, boolean passwordOnDelete) {
        PreferenceUtil.getInstance(context).putBoolean("password_on_delete", passwordOnDelete);
    }

    public static void setPasswordOnHidden(Context context, boolean passwordOnHidden) {
        PreferenceUtil.getInstance(context).putBoolean("password_on_hidden", passwordOnHidden);
    }

    public static boolean isPasswordSet(Context context) {
        return PreferenceUtil.getInstance(context).getString("password_hash", null) != null;
    }

    public static boolean isPasswordOnHidden(Context context) {
        PreferenceUtil SP = PreferenceUtil.getInstance(context);
        return SP.getString("password_hash", null) != null && SP.getBoolean("password_on_hidden", false);
    }

    public static boolean isPasswordOnDelete(Context context) {
        PreferenceUtil SP = PreferenceUtil.getInstance(context);
        return SP.getString("password_hash", null) != null && SP.getBoolean("password_on_delete", false);
    }

    private static boolean checkPassword(Context context, String pass){
        return sha256(pass).equals(PreferenceUtil.getInstance(context).getString("password_hash", null));
    }

    public static boolean setPassword(Context context, String newValue) {
        return PreferenceUtil.getInstance(context).putString("password_hash", sha256(newValue));
    }

    public static boolean clearPassword(Context context) {
        return PreferenceUtil.getInstance(context).putString("password_hash", null);
    }

    public static void askPassword(final ThemedActivity activity, final PasswordInterface passwordInterface) {
        AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder (activity, activity.getDialogStyle());

        final View PasswordDialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_password, null);
        final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_title);
        final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_card);
        final EditText editTextPassword = (EditText) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_edittxt);

        passwordDialogTitle.setBackgroundColor(activity.getPrimaryColor());
        passwordDialogCard.setBackgroundColor(activity.getCardBackgroundColor());
        ThemeHelper.setCursorDrawableColor(editTextPassword, activity.getTextColor());
        editTextPassword.getBackground().mutate().setColorFilter(activity.getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editTextPassword.setTextColor(activity.getTextColor());
        passwordDialogBuilder.setView(PasswordDialogLayout);

        passwordDialogBuilder.setPositiveButton(activity.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NOTE: set this empty, later will be overwrite to avoid the dismiss
            }
        });

        passwordDialogBuilder.setNegativeButton(activity.getString(R.string.cancel).toUpperCase(), null);

        final AlertDialog passwordDialog = passwordDialogBuilder.create();
        passwordDialog.show();

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPassword(activity, editTextPassword.getText().toString())){
                    passwordDialog.dismiss();
                    passwordInterface.onSuccess();
                } else {
                    editTextPassword.getText().clear();
                    editTextPassword.requestFocus();
                    passwordInterface.onError();
                }
            }
        });
    }

    private static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){ throw new RuntimeException(ex); }
    }

    public interface PasswordInterface {
        void onSuccess();
        void onError();
    }
}
