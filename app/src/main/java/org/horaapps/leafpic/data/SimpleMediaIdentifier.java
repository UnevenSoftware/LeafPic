package org.horaapps.leafpic.data;

/**
 * Created by brand on 8/12/2016.
 */
public class SimpleMediaIdentifier {

    private String albumPath;
    private long albumId;
    private String mediaPath;

    public String getMediaPath() {
        return mediaPath;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public long getAlbumId() {
        return albumId;
    }

    SimpleMediaIdentifier(String albumPath, long albumId, String mediaPath) {
        this.albumPath = albumPath;
        this.albumId = albumId;
        this.mediaPath = mediaPath;
    }
}
