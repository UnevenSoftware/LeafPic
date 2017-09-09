package org.horaapps.leafpic.util;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;

import java.security.MessageDigest;

/**
 * Created by Jibo on 06/05/2016.
 */
public class Security {

    public static void setPasswordOnDelete(Context context, boolean passwordOnDelete) {
        Hawk.put("password_on_delete", passwordOnDelete);
    }

    public static void setPasswordOnHidden(Context context, boolean passwordOnHidden) {
        Hawk.put("password_on_hidden", passwordOnHidden);
    }

    public static void setFingerprintUnlock(Context context, boolean passwordOnHidden) {
        Hawk.put("fingerprint_security", passwordOnHidden);
    }

    public static boolean isPasswordSet(Context context) {
        return Hawk.get("password_hash", null) != null;
    }

    public static boolean isPasswordOnHidden(Context context) {
        return Hawk.get("password_hash", null) != null && Hawk.get("password_on_hidden", false);
    }

    public static boolean isFingerprintUsed(Context context) {
        return Hawk.get("fingerprint_security", false);
    }

    public static boolean isPasswordOnDelete(Context context) {
        return Hawk.get("password_hash", null) != null && Hawk.get("password_on_delete", false);
    }

    private static boolean checkPassword(Context context, String pass) {
        return sha256(pass).equals(Hawk.get("password_hash", null));
    }

    public static boolean setPassword(Context context, String newValue) {
        return Hawk.put("password_hash", sha256(newValue));
    }

    public static boolean clearPassword(Context context) {
        return Hawk.delete("password_hash");
    }

    public static void askPassword(final ThemedActivity activity, final PasswordInterface passwordInterface) {
        AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            FingerprintHandler fingerprintHandler = new FingerprintHandler(activity);
        } else {

        }

        final View PasswordDialogLayout = activity.getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_password, null);
        final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_title);
        final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_card);
        final EditText editTextPassword = (EditText) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_edittxt);

        passwordDialogTitle.setBackgroundColor(activity.getPrimaryColor());
        passwordDialogCard.setBackgroundColor(activity.getCardBackgroundColor());
        ThemeHelper.setCursorColor(editTextPassword, activity.getTextColor());
        editTextPassword.getBackground().mutate().setColorFilter(activity.getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editTextPassword.setTextColor(activity.getTextColor());


        passwordDialogBuilder.setView(PasswordDialogLayout);

        passwordDialogBuilder.setPositiveButton(activity.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NOTE: set this empty, later will be overwrite to avoid the dismiss
            }
        });

        passwordDialogBuilder.setNegativeButton(activity.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard(activity, editTextPassword.getWindowToken());
            }
        });

        final AlertDialog passwordDialog = passwordDialogBuilder.create();
        passwordDialog.show();
        showKeyboard(activity);
        editTextPassword.requestFocus();

        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPassword(activity, editTextPassword.getText().toString())) {
                    hideKeyboard(activity, editTextPassword.getWindowToken());
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public class AskFingerprint implements FingerprintHandler.OnFingerprintResult {

        AlertDialog passwordDialog;
        FingerprintInterface fingerprintInterface;

        public AskFingerprint() {
        }

        public void ask(final ThemedActivity activity, final FingerprintInterface fingerprintInterface) {
            AlertDialog.Builder passwordDialogBuilder = new AlertDialog.Builder(activity, activity.getDialogStyle());

            this.fingerprintInterface = fingerprintInterface;
            FingerprintHandler fingerprintHandler = new FingerprintHandler(activity);
            fingerprintHandler.setOnFingerprintResult(this);
            fingerprintHandler.startAuth();

            final View PasswordDialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_fingerprint, null);
            final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_title);
            final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_card);
            final LinearLayout llUsePsw = (LinearLayout) PasswordDialogLayout.findViewById(R.id.ll_use_psw);
            final ThemedIcon fingerprintIcon = (ThemedIcon) PasswordDialogLayout.findViewById(R.id.fingerprint_icon);

            fingerprintIcon.setColor(activity.getIconColor());

            passwordDialogTitle.setBackgroundColor(activity.getPrimaryColor());
            passwordDialogCard.setBackgroundColor(activity.getCardBackgroundColor());

            passwordDialogBuilder.setView(PasswordDialogLayout);


            passwordDialogBuilder.setNegativeButton(activity.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // NOTE: set this empty, later will be overwrite to avoid the dismiss
                }
            });


            passwordDialog = passwordDialogBuilder.create();
            passwordDialog.show();

            llUsePsw.setOnClickListener(view -> {
                passwordDialog.dismiss();
                fingerprintInterface.onUsePsw();
            });

        }

        @Override
        public void onSuccess() {
            passwordDialog.dismiss();
            fingerprintInterface.onSuccess();
        }

        @Override
        public void onError() {
            fingerprintInterface.onError();

        }
    }


    private static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private static void hideKeyboard(Context context, IBinder token) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public interface PasswordInterface {
        void onSuccess();

        void onError();
    }

    public interface FingerprintInterface {
        void onSuccess();

        void onError();

        void onUsePsw();
    }
}
