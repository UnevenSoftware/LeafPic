package com.horaapps.leafpic;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.horaapps.leafpic.Views.ThemedActivity;
import com.horaapps.leafpic.utils.SecurityHelper;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

/**
 * Created by dnld on 22/05/16.
 */
public class SecurityActivity extends ThemedActivity {
    Toolbar toolbar;
    LinearLayout llbody;
    LinearLayout llroot;
    SharedPreferences SP;
    SecurityHelper securityObj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_layout);
        SP = PreferenceManager.getDefaultSharedPreferences(SecurityActivity.this);
        securityObj = new SecurityHelper(SecurityActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        llbody = (LinearLayout) findViewById(R.id.ll_security_dialog_body);
        llroot = (LinearLayout) findViewById(R.id.root);


        final SwitchCompat swApplySecurityDelete = (SwitchCompat) findViewById(R.id
                .security_body_apply_delete_switch);
        final SwitchCompat swActiveSecurity = (SwitchCompat) findViewById(R.id.active_security_switch);
        final SwitchCompat swApplySecurityHidden = (SwitchCompat) findViewById(R.id.security_body_apply_hidden_switch);

        /** - SWITCHS - **/
        /** - ACTIVE SECURITY - **/
        swActiveSecurity.setChecked(securityObj.isActiveSecurity());
        swActiveSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("active_security", isChecked);
                editor.apply();


                securityObj.updateSecuritySetting();
                updateSwitchColor(swActiveSecurity, getAccentColor());
                llbody.setEnabled(swActiveSecurity.isChecked());
                //llbody.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        updateSwitchColor(swActiveSecurity, getAccentColor());
        llbody.setEnabled(swActiveSecurity.isChecked());
        //llbody.setVisibility(swActiveSecurity.isChecked() ? View.VISIBLE : View.GONE);

        /** - ACTIVE SECURITY ON HIDDEN FOLDER - **/
        swApplySecurityHidden.setChecked(securityObj.isPasswordOnHidden());
        swApplySecurityHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("password_on_hidden", isChecked);
                editor.apply();

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
                SharedPreferences.Editor editor = SP.edit();
                editor.putBoolean("password_on_delete", isChecked);
                editor.apply();

                securityObj.updateSecuritySetting();
                updateSwitchColor(swApplySecurityDelete, getAccentColor());
            }
        });
        updateSwitchColor(swApplySecurityDelete, getAccentColor());


        EditText eTxtPasswordSecurity = (EditText) findViewById(R.id.security_password_edittxt);

        setupUI();
    }

    public void setupUI() {
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
        toolbar.setTitle(getString(R.string.about));

        IconicsImageView imgActiveSecurity = (IconicsImageView) findViewById(R.id.active_security_icon);
        TextView txtActiveSecurity = (TextView) findViewById(R.id.active_security_item_title);
        TextView txtApplySecurity = (TextView) findViewById(R.id.security_body_apply_on);
        IconicsImageView imgApplySecurityHidden = (IconicsImageView) findViewById(R.id.security_body_apply_hidden_icon);
        TextView txtApplySecurityHidden = (TextView) findViewById(R.id.security_body_apply_hidden_title);
        IconicsImageView imgApplySecurityDelete = (IconicsImageView) findViewById(R.id.security_body_apply_delete_icon);
        TextView txtApplySecurityDelete = (TextView) findViewById(R.id.security_body_apply_delete_title);

        llroot.setBackgroundColor(getBackgroundColor());

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
