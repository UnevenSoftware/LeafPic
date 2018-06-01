package org.horaapps.leafpic.interfaces;

import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;

import java.util.ArrayList;

public interface MediaClickListener {
    void onMediaClick(Album album, ArrayList<Media> media, int position);
}
