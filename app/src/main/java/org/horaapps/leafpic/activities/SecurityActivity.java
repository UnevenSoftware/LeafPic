package org.horaapps.leafpic.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.activities.base.ThemedActivity;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.Security;

/**
 * Created by dnld on 22/05/16.
 */
public class SecurityActivity extends ThemedActivity {

    private Toolbar toolbar;
    private LinearLayout llbody;
    private LinearLayout llroot;
    private PreferenceUtil SP;
    private SwitchCompat swActiveSecurity;
    private SwitchCompat swApplySecurityDelete;
    private SwitchCompat swApplySecurityHidden;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_security);
        SP = PreferenceUtil.getInstance(getApplicationContext());
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
        llbody = (LinearLayout) findViewById(org.horaapps.leafpic.R.id.ll_security_dialog_body);
        llroot = (LinearLayout) findViewById(org.horaapps.leafpic.R.id.root);

        swActiveSecurity = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.active_security_switch);
        swApplySecurityDelete = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_switch);
        swApplySecurityHidden = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_switch);

        /** - SWITCHES - **/
        /** - ACTIVE SECURITY - **/
        swActiveSecurity.setChecked(Security.isPasswordSet(getApplicationContext()));
        swActiveSecurity.setClickable(false);
        findViewById(R.id.ll_active_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swActiveSecurity.setChecked(!swActiveSecurity.isChecked());
                updateSwitchColor(swActiveSecurity, getAccentColor());
                if (swActiveSecurity.isChecked()) setPasswordDialog();
                else Security.clearPassword(getApplicationContext());
                toggleEnabledChild(swActiveSecurity.isChecked());
            }
        });
        updateSwitchColor(swActiveSecurity, getAccentColor());

        /** - ACTIVE SECURITY ON HIDDEN FOLDER - **/
        swApplySecurityHidden.setChecked(SP.getBoolean("password_on_hidden", false));
        swApplySecurityHidden.setClickable(false);
        findViewById(R.id.ll_security_body_apply_hidden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplySecurityHidden.setChecked(!swApplySecurityHidden.isChecked());
                Security.setPasswordOnHidden(getApplicationContext(), swApplySecurityHidden.isChecked());
                updateSwitchColor(swApplySecurityHidden, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityHidden, getAccentColor());

        /**ACTIVE SECURITY ON DELETE ACTION**/
        swApplySecurityDelete.setChecked(SP.getBoolean("password_on_delete", false));
        swApplySecurityDelete.setClickable(false);
        findViewById(R.id.ll_security_body_apply_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swApplySecurityDelete.setChecked(!swApplySecurityDelete.isChecked());
                Security.setPasswordOnDelete(getApplicationContext(), swApplySecurityDelete.isChecked());
                updateSwitchColor(swApplySecurityDelete, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityDelete, getAccentColor());
        setupUI();
        toggleEnabledChild(swActiveSecurity.isChecked());
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
        setCursorDrawableColor(editTextPassword, getTextColor());
        editTextConfirmPassword.getBackground().mutate().setColorFilter(getTextColor(), PorterDuff.Mode.SRC_ATOP);
        editTextConfirmPassword.setTextColor(getTextColor());
        editTextConfirmPassword.setHintTextColor(getSubTextColor());
        setCursorDrawableColor(editTextConfirmPassword, getTextColor());
        passwordDialog.setView(PasswordDialogLayout);

        AlertDialog dialog = passwordDialog.create();
        dialog.setCancelable(false);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(org.horaapps.leafpic.R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                swActiveSecurity.setChecked(false);
                updateSwitchColor(swActiveSecurity, getAccentColor());
                toggleEnabledChild(swActiveSecurity.isChecked());
                Security.clearPassword(getApplicationContext());
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(org.horaapps.leafpic.R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editTextPassword.length() > 3) {
                    if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
                        if(Security.setPassword(getApplicationContext(), editTextPassword.getText().toString())) {
                            swActiveSecurity.setChecked(true);
                            toggleEnabledChild(true);
                            Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.remember_password_message, Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(SecurityActivity.this, R.string.error_contact_developer, Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.password_dont_match, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.error_password_length, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void toggleEnabledChild(boolean enable) {
        //swApplySecurityDelete.setEnabled(enable);
        //swApplySecurityHidden.setEnabled(enable);
        findViewById(R.id.ll_security_body_apply_hidden).setEnabled(enable);
        findViewById(R.id.ll_security_body_apply_delete).setClickable(enable);
        if(enable){
            ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon)).setColor(getIconColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title)).setTextColor(getTextColor());
            ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon)).setColor(getIconColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title)).setTextColor(getTextColor());
        }
        else{
            ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon)).setColor(getSubTextColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title)).setTextColor(getSubTextColor());
            ((IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon)).setColor(getSubTextColor());
            ((TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title)).setTextColor(getSubTextColor());
        }
    }

    private void setupUI() {
        setRecentApp(getString(R.string.security));
        toolbar.setBackgroundColor(getPrimaryColor());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_arrow_back)
                        .color(Color.WHITE)
                        .sizeDp(19));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(getString(R.string.security));

        setStatusBarColor();
        setNavBarColor();

        IconicsImageView imgActiveSecurity = (IconicsImageView) findViewById(org.horaapps.leafpic.R.id.active_security_icon);
        TextView txtActiveSecurity = (TextView) findViewById(org.horaapps.leafpic.R.id.active_security_item_title);
        TextView txtApplySecurity = (TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_on);
        IconicsImageView imgApplySecurityHidden = (IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_icon);
        TextView txtApplySecurityHidden = (TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_title);
        IconicsImageView imgApplySecurityDelete = (IconicsImageView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_icon);
        TextView txtApplySecurityDelete = (TextView) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_title);
        CardView securityDialogCard = (CardView) findViewById(org.horaapps.leafpic.R.id.security_dialog_card);
        llroot.setBackgroundColor(getBackgroundColor());
        securityDialogCard.setCardBackgroundColor(getCardBackgroundColor());

        /**ICONS**/
        int color = getIconColor();
        imgActiveSecurity.setColor(color);
        imgApplySecurityHidden.setColor(color);
        imgApplySecurityDelete.setColor(color);

        /**TEXTVIEWS**/
        color=getTextColor();
        txtActiveSecurity.setTextColor(color);
        txtApplySecurity.setTextColor(color);
        txtApplySecurityHidden.setTextColor(color);
        txtApplySecurityDelete.setTextColor(color);
    }
}
