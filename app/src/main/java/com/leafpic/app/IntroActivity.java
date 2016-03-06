package com.leafpic.app;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by Jibo on 29/01/2016.
 */
public class IntroActivity extends AppIntro {
    int slide=1;

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("Welcome To LeafPic!", "ColourFull Material Design Gallery!",
                R.drawable.leafpic_big, Color.parseColor("#009688")));

        addSlide(AppIntroFragment.newInstance("Leaf Pic", Html.fromHtml("<b>DONY GHEEEY </b><br><i>la descrizione itallica spifina!!!</i>"),
                R.mipmap.ic_launcher, Color.parseColor("#607D8B")));

        setBarColor(Color.parseColor("#00796B"));
        setSeparatorColor(Color.parseColor("#009688"));
        showSkipButton(true);

        BitmapDrawable drawable = ((BitmapDrawable) getDrawable(R.mipmap.ic_launcher));
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), drawable.getBitmap(), Color.parseColor("#00796B")));

    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, AlbumsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        System.exit(1);
        loadMainActivity();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        System.exit(1);
        loadMainActivity();
    }

    @Override
    public void onSlideChanged() {
        if (slide==1) {
            slide=2;
            setSeparatorColor(Color.parseColor("#00796B"));
            setBarColor(Color.parseColor("#00796B"));
        } else{
            slide=1;
            setSeparatorColor(Color.parseColor("#607D8B"));
            setBarColor(Color.parseColor("#455A64"));
        }
    }

    public void getStarted(View v) {
        loadMainActivity();
    }
}