package org.horaapps.leafpic.data.filter;

import org.horaapps.leafpic.data.Media;

/**
 * Created by dnld on 4/10/17.
 */

public interface IMediaFilter {
    boolean accept(Media media);
}
