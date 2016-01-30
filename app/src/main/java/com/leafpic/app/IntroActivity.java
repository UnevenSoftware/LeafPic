package com.leafpic.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by Jibo on 29/01/2016.
 */
public class IntroActivity extends AppIntro {
    int slide=1;

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("LeafPic", "The Faster Gallery for Your Android :)",
                R.mipmap.ic_launcher, Color.parseColor("#009688")));

        addSlide(AppIntroFragment.newInstance("Leaf Pic", Html.fromHtml("<b>DONY GHEEEY </b><br><i>la descrizione itallica spifina!!!</i>"),
                R.mipmap.ic_launcher, Color.parseColor("#607D8B")));

        setBarColor(Color.parseColor("#00796B"));
        setSeparatorColor(Color.parseColor("#009688"));
        showSkipButton(true);
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, AlbumsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        loadMainActivity();
        Toast.makeText(getApplicationContext(), getString(R.string.skip), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
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