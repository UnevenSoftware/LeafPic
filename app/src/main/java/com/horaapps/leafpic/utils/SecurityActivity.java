package com.horaapps.leafpic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Jibo on 06/05/2016.
 */
public class SecurityActivity {
    SharedPreferences SP;
    boolean activeSecurity;
    boolean passwordOnDelete;
    boolean passwordOnHidden;
    String passwordValue;

    Context context;
    public SecurityActivity(Context c){
        this.context = c;
        updateSecuritySetting();
    }

    public boolean isActiveSecurity(){return activeSecurity;}
    public boolean isPasswordOnHidden(){return passwordOnHidden;}
    public boolean isPasswordOnDelete(){return passwordOnDelete;}

    public boolean checkPassword(String pass){
        if (isActiveSecurity() && pass.equals(passwordValue)){
            return true;
        } else
            return false;
    }

    public void updateSecuritySetting(){
        this.SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.activeSecurity = SP.getBoolean("active_security", false);
        this.passwordOnDelete = SP.getBoolean("password_on_delete", false);
        this.passwordOnHidden = SP.getBoolean("password_on_hidden", true);
        this.passwordValue = SP.getString("password_value", "");
    }



}
