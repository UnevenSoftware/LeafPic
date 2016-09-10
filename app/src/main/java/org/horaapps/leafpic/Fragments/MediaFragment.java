package org.horaapps.leafpic.Fragments;

import android.support.v4.app.Fragment;

import org.horaapps.leafpic.MyApplication;
import org.horaapps.leafpic.data.Media;

/**
 * Created by dnld on 10/09/16.
 */

public class MediaFragment extends Fragment {

  public Media getMedia() {
    return ((MyApplication) getActivity().getApplicationContext()).getAlbum().getCurrentMedia();
  }
}
