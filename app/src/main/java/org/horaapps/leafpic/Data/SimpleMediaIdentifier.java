package org.horaapps.leafpic.Data;

/**
 * Created by brand on 8/12/2016.
 */
public class SimpleMediaIdentifier {
    String albumPath;
    long albumId;

    public String getMediaPath() {
        return mediaPath;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public long getAlbumId() {
        return albumId;
    }

    String mediaPath;

    public SimpleMediaIdentifier(String albumPath, long albumId, String mediaPath) {
        this.albumPath = albumPath;
        this.albumId = albumId;
        this.mediaPath = mediaPath;
    }
}
