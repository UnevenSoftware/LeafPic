package org.horaapps.leafpic.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import org.horaapps.leafpic.Activities.base.ThemedActivity;
import org.horaapps.leafpic.util.PreferenceUtil;
import org.horaapps.leafpic.util.SecurityHelper;

/**
 * Created by dnld on 22/05/16.
 */
public class SecurityActivity extends ThemedActivity {

    private Toolbar toolbar;
    private LinearLayout llbody;
    private LinearLayout llroot;
    private PreferenceUtil SP;
    private SecurityHelper securityObj;
    private SwitchCompat swActiveSecurity;
    private SwitchCompat swApplySecurityDelete;
    private SwitchCompat swApplySecurityHidden;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.horaapps.leafpic.R.layout.activity_security_layout);
        SP = PreferenceUtil.getInstance(getApplicationContext());
        securityObj = new SecurityHelper(SecurityActivity.this);
        toolbar = (Toolbar) findViewById(org.horaapps.leafpic.R.id.toolbar);
        llbody = (LinearLayout) findViewById(org.horaapps.leafpic.R.id.ll_security_dialog_body);
        llroot = (LinearLayout) findViewById(org.horaapps.leafpic.R.id.root);

        swApplySecurityDelete = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.security_body_apply_delete_switch);
        swActiveSecurity = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.active_security_switch);
        swApplySecurityHidden = (SwitchCompat) findViewById(org.horaapps.leafpic.R.id.security_body_apply_hidden_switch);

        /** - SWITCHES - **/
        /** - ACTIVE SECURITY - **/
        swActiveSecurity.setChecked(securityObj.isActiveSecurity());
        swActiveSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.getEditor();

                securityObj.updateSecuritySetting();
                updateSwitchColor(swActiveSecurity, getAccentColor());
                llbody.setEnabled(swActiveSecurity.isChecked());
                if (isChecked)
                    setPasswordDialog();
                else {
                    editor.putString(getString(org.horaapps.leafpic.R.string.preference_password_value),"");
                    editor.putBoolean(getString(org.horaapps.leafpic.R.string.preference_use_password), false);
                    editor.commit();
                    toggleEnabledChild(false);
                }
            }
        });
        updateSwitchColor(swActiveSecurity, getAccentColor());
        llbody.setEnabled(swActiveSecurity.isChecked());

        /** - ACTIVE SECURITY ON HIDDEN FOLDER - **/
        swApplySecurityHidden.setChecked(securityObj.isPasswordOnHidden());
        swApplySecurityHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.putBoolean(getString(org.horaapps.leafpic.R.string.preference_use_password_on_hidden), isChecked);
                securityObj.updateSecuritySetting();
                updateSwitchColor(swApplySecurityHidden, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityHidden, getAccentColor());

        /**ACTIVE SECURITY ON DELETE ACTION**/
        swApplySecurityDelete.setChecked(securityObj.isPasswordOnDelete());
        swApplySecurityDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.putBoolean(getString(org.horaapps.leafpic.R.string.preference_use_password_on_delete), isChecked);
                securityObj.updateSecuritySetting();
                updateSwitchColor(swApplySecurityDelete, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityDelete, getAccentColor());
        setupUI();
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

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(org.horaapps.leafpic.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                swActiveSecurity.setChecked(false);
                SP.putBoolean(getString(org.horaapps.leafpic.R.string.preference_use_password), false);
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(org.horaapps.leafpic.R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean changed = false;

                if (editTextPassword.length() > 3) {
                    if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
                        SP.putString(getString(org.horaapps.leafpic.R.string.preference_password_value), editTextPassword.getText().toString());
                        securityObj.updateSecuritySetting();
                        Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.remember_password_message, Toast.LENGTH_SHORT).show();
                        changed = true;
                    } else
                        Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.password_dont_match, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), org.horaapps.leafpic.R.string.error_password_length, Toast.LENGTH_SHORT).show();

                swActiveSecurity.setChecked(changed);
                SP.putBoolean(getString(org.horaapps.leafpic.R.string.preference_use_password), changed);
                toggleEnabledChild(changed);
            }
        });

        dialog.show();
    }

    private void toggleEnabledChild(boolean enable) {
       swApplySecurityDelete.setEnabled(enable);
        swApplySecurityHidden.setEnabled(enable);
    }

    private void setupUI() {
        setStatusBarColor();
        setNavBarColor();
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
        toolbar.setTitle(getString(org.horaapps.leafpic.R.string.about));

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

        /*ICONS*/
        int color = getIconColor();
        imgActiveSecurity.setColor(color);
        imgApplySecurityHidden.setColor(color);
        imgApplySecurityDelete.setColor(color);

        /*TEXTVIEWS*/
        color=getTextColor();
        txtActiveSecurity.setTextColor(color);
        txtApplySecurity.setTextColor(color);
        txtApplySecurityHidden.setTextColor(color);
        txtApplySecurityDelete.setTextColor(color);
    }
}
