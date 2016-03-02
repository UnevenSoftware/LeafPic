package com.leafpic.app;

import android.os.Bundle;

import com.leafpic.app.Views.ThemedActivity;

/**
 * Created by Jibo on 02/03/2016.
 */
public class SettingTry extends ThemedActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
    }
}
