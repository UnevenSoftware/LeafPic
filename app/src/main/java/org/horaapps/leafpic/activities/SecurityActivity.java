package org.horaapps.leafpic.activities;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.util.FingerprintHandler;
import org.horaapps.leafpic.util.Security;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedIcon;

/**
 * Created by dnld on 22/05/16.
 */
public class SecurityActivity extends ThemedActivity {

    private Toolbar toolbar;

    private LinearLayout llroot;
    private SwitchCompat swActiveSecurity;
    private SwitchCompat swApplySecurityDelete;
    private SwitchCompat swApplySecurityHidden;
    private SwitchCompat swFingerPrint;
    private LinearLayout llFingerprint;
    private FingerprintHandler fingerprintHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        llroot = (LinearLayout) findViewById(R.id.root);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        swActiveSecurity = (SwitchCompat) findViewById(R.id.active_security_switch);
        swApplySecurityDelete = (SwitchCompat) findViewById(R.id.security_body_apply_delete_switch);
        swApplySecurityHidden = (SwitchCompat) findViewById(R.id.security_body_apply_hidden_switch);
        swFingerPrint = (SwitchCompat) findViewById(R.id.active_security_fingerprint_switch);
        llFingerprint = (LinearLayout) findViewById(R.id.ll_active_security_fingerprint);

        initUi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintHandler = new FingerprintHandler(this, null);
            if(fingerprintHandler.isFingerprintSupported()){
                llFingerprint.setVisibility(View.VISIBLE);
            }else{
                llFingerprint.setVisibility(View.GONE);
            }
        }else{
            llFingerprint.setVisibility(View.GONE);
        }

        /** - SWITCHES - **/
        /** - ACTIVE SECURITY - **/
        swActiveSecurity.setChecked(Security.isPasswordSet());
        swActiveSecurity.setClickable(false);
        findViewById(R.id.ll_active_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swActiveSecurity.setChecked(!swActiveSecurity.isChecked());
                setSwitchColor(getAccentColor(), swActiveSecurity);
                if (swActiveSecurity.isChecked()) setPasswordDialog();
                else{
                    Security.clearPassword();
                    swApplySecurityHidden.setChecked(false);
                    swApplySecurityDelete.setChecked(false);
                    swFingerPrint.setChecked(false);
                    Security.setFingerprintUnlock(swFingerPrint.isChecked());
                    Security.setPasswordOnDelete(swApplySecurityDelete.isChecked());
                    Security.setPasswordOnHidden(swApplySecurityHidden.isChecked());
                    setSwitchColor(getAccentColor(), swApplySecurityHidden);
                    setSwitchColor(getAccentColor(), swApplySecurityDelete);
                    setSwitchColor(getAccentColor(), swFingerPrint);

                }
                toggleEnabledChild(swActiveSecurity.isChecked());
            }
        });

        /** - ACTIVE SECURITY ON HIDDEN FOLDER - **/
        swApplySecurityHidden.setChecked(Hawk.get("password_on_hidden", false));
        swApplySecurityHidden.setClickable(false);
        findViewById(R.id.ll_security_body_apply_hidden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplySecurityHidden.setChecked(!swApplySecurityHidden.isChecked());
                Security.setPasswordOnHidden(swApplySecurityHidden.isChecked());
                setSwitchColor(getAccentColor(), swApplySecurityHidden);
            }
        });

        /**ACTIVE SECURITY ON DELETE ACTION**/
        swApplySecurityDelete.setChecked(Hawk.get("password_on_delete", false));
        swApplySecurityDelete.setClickable(false);
        findViewById(R.id.ll_security_body_apply_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplySecurityDelete.setChecked(!swApplySecurityDelete.isChecked());
                Security.setPasswordOnDelete(swApplySecurityDelete.isChecked());
                setSwitchColor(getAccentColor(), swApplySecurityDelete);
            }
        });

        /** - FINGERPRINT - **/
        swFingerPrint.setChecked(Hawk.get("fingerprint_security", false));
        swFingerPrint.setClickable(false);
        findViewById(R.id.ll_active_security_fingerprint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swFingerPrint.setChecked(!swFingerPrint.isChecked());
                Security.setFingerprintUnlock(swFingerPrint.isChecked());
                setSwitchColor(getAccentColor(), swFingerPrint);
            }
        });
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setPasswordDialog() {

        final AlertDialog.Builder passwordDialog = new AlertDialog.Builder(SecurityActivity.this, getDialogStyle());
        final View PasswordDialogLayout = getLayoutInflater().inflate(org.horaapps.leafpic.R.layout.dialog_set_password, null);
        final TextView passwordDialogTitle = (TextView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_title);
        final CardView passwordDialogCard = (CardView) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_dialog_card);
        final EditText editTextPassword = (EditText) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.password_edittxt);
        final EditText editTextConfirmPassword = (EditText) PasswordDialogLayout.findViewById(org.horaapps.leafpic.R.id.confirm_password_edittxt);

        passwordDialogTitle.setBackgroundColor(getPrimaryColor());
        passwordDialogCard.setBackgroundColor(getCardBackgroundColor());

        editTextPassword.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editTextPassword.setTextColor(getTextColor());
        editTextPassword.setHintTextColor(getSubTextColor());
        ThemeHelper.setCursorColor(editTextPassword, getTextColor());
        editTextConfirmPassword.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editTextConfirmPassword.setTextColor(getTextColor());
        editTextConfirmPassword.setHintTextColor(getSubTextColor());
        ThemeHelper.setCursorColor(editTextConfirmPassword, getTextColor());
        passwordDialog.setView(PasswordDialogLayout);

        AlertDialog dialog = passwordDialog.create();
        dialog.setCancelable(false);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(org.horaapps.leafpic.R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toggleResetSecurity();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(org.horaapps.leafpic.R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTextPassword.length() > 3) {
                    if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
                        if (Security.setPassword(editTextPassword.getText().toString())) {
                            swActiveSecurity.setChecked(true);
                            toggleEnabledChild(true);
                            Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.remember_password_message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SecurityActivity.this, R.string.error_contact_developer, Toast.LENGTH_SHORT).show();
                            toggleResetSecurity();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.password_dont_match, Toast.LENGTH_SHORT).show();
                        toggleResetSecurity();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.error_password_length, Toast.LENGTH_SHORT).show();
                    toggleResetSecurity();
                }
            }
        });
        dialog.show();
    }

    private void toggleResetSecurity (){
        swActiveSecurity.setChecked(false);
        setSwitchColor(getAccentColor(), swActiveSecurity);
        toggleEnabledChild(swActiveSecurity.isChecked());
        Security.clearPassword();
    }

    private void toggleEnabledChild(boolean enable) {
        findViewById(R.id.ll_security_body_apply_hidden).setEnabled(enable);
        findViewById(R.id.ll_security_body_apply_delete).setClickable(enable);
        findViewById(R.id.ll_active_security_fingerprint).setClickable(enable);

        if(enable){
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon)).setColor(getIconColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title)).setTextColor(getTextColor());
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon)).setColor(getIconColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title)).setTextColor(getTextColor());
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_icon)).setColor(getIconColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_item_title)).setTextColor(getTextColor());
        } else {
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon)).setColor(getSubTextColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title)).setTextColor(getSubTextColor());
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon)).setColor(getSubTextColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title)).setTextColor(getSubTextColor());
            ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_icon)).setColor(getSubTextColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_item_title)).setTextColor(getSubTextColor());
        }
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        setRecentApp(getString(R.string.security));
        toolbar.setBackgroundColor(getPrimaryColor());

        setSwitchColor(getAccentColor(), swActiveSecurity, swApplySecurityHidden, swApplySecurityDelete, swFingerPrint);
        toggleEnabledChild(swActiveSecurity.isChecked());

        setStatusBarColor();
        setNavBarColor();

        llroot.setBackgroundColor(getBackgroundColor());
        ((CardView) findViewById(org.horaapps.leafpic.R.id.security_dialog_card)).setCardBackgroundColor(getCardBackgroundColor());

        /** ICONS **/
        int color = getIconColor();
        ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.active_security_icon)).setColor(color);
        ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon)).setColor(color);
        ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon)).setColor(color);
        ((ThemedIcon) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_icon)).setColor(color);

        /** TEXTVIEWS **/
        color = getTextColor();
        ((TextView) findViewById(org.horaapps.leafpic.R.id.active_security_item_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_on)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title)).setTextColor(color);
        ((TextView) findViewById(org.horaapps.leafpic.R.id.active_security_fingerprint_item_title)).setTextColor(color);

    }
}
