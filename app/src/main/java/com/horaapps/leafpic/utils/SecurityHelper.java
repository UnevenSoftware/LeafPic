package com.horaapps.leafpic.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.horaapps.leafpic.R;
import com.horaapps.leafpic.SecurityActivity;
import com.horaapps.leafpic.Views.ThemedActivity;

/**
 * Created by Jibo on 06/05/2016.
 */
public class SecurityHelper {
    SharedPreferences SP;
    boolean activeSecurity;
    boolean passwordOnDelete;
    boolean passwordOnHidden;
    String passwordValue;

    Context context;
    public SecurityHelper(Context c){
        this.context = c;
        updateSecuritySetting();
    }

    public boolean isActiveSecurity(){return activeSecurity;}
    public boolean isPasswordOnHidden(){return passwordOnHidden;}
    public boolean isPasswordOnDelete(){return passwordOnDelete;}

    public boolean checkPassword(String pass){
        return (isActiveSecurity() && pass.equals(passwordValue));
    }

    public void updateSecuritySetting(){
        this.SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.activeSecurity = SP.getBoolean("active_security", false);
        this.passwordOnDelete = SP.getBoolean("password_on_delete", false);
        this.passwordOnHidden = SP.getBoolean("password_on_hidden", true);
        this.passwordValue = SP.getString("password_value", "");
    }

    public EditText getInsertPasswordDialog(final ThemedActivity activity, AlertDialog.Builder passwordDialog){

        final View PasswordDialogLayout = activity.getLayoutInflater().inflate(R.layout
                .password_dialog, null);
        final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(R.id.password_dialog_title);
        final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(R.id.password_dialog_card);
        final EditText editxtPassword = (EditText) PasswordDialogLayout.findViewById(R.id.password_edittxt);

        passwordDialogTitle.setBackgroundColor(activity.getPrimaryColor());
        passwordDialogCard.setBackgroundColor(activity.getCardBackgroundColor());
        ThemedActivity.setCursorDrawableColor(editxtPassword, activity.getTextColor());
        editxtPassword.getBackground().mutate().setColorFilter(activity.getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editxtPassword.setTextColor(activity.getTextColor());

        passwordDialog.setView(PasswordDialogLayout);

        return editxtPassword;
    }
}
