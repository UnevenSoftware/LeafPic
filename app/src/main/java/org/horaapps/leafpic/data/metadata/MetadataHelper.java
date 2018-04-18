package org.horaapps.leafpic.data.metadata;

import android.content.Context;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dnld on 4/10/17.
 */

public class MetadataHelper {

    public MediaDetailsMap<String, String> getMainDetails(Context context, Media m){
        MediaDetailsMap<String, String> details = new MediaDetailsMap<>();
        details.put(context.getString(R.string.path), m.getDisplayPath());
        details.put(context.getString(R.string.type), m.getMimeType());
        if(m.getSize() != -1)
            details.put(context.getString(R.string.size), StringUtils.humanReadableByteCount(m.getSize(), true));
        // TODO should i add this always?
        details.put(context.getString(R.string.orientation), m.getOrientation() + "");
        try {
            MetaDataItem metadata = MetaDataItem.getMetadata(context, m.getUri());
            details.put(context.getString(R.string.resolution), metadata.getResolution());
            details.put(context.getString(R.string.date), SimpleDateFormat.getDateTimeInstance().format(new Date(m.getDateModified())));
            Date dateOriginal = metadata.getDateOriginal();
            if (dateOriginal != null )
                details.put(context.getString(R.string.date_taken), SimpleDateFormat.getDateTimeInstance().format(dateOriginal));

            String tmp;
            if ((tmp = metadata.getCameraInfo()) != null)
                details.put(context.getString(R.string.camera), tmp);
            if ((tmp = metadata.getExifInfo()) != null)
                details.put(context.getString(R.string.exif), tmp);
            GeoLocation location;
            if ((location = metadata.getLocation()) != null)
                details.put(context.getString(R.string.location), location.toDMSString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return details;
    }

    public MediaDetailsMap<String, String> getAllDetails(Context context, Media media) {
        MediaDetailsMap<String, String> data = new MediaDetailsMap<String, String>();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(context.getContentResolver().openInputStream(media.getUri()));
            for(Directory directory : metadata.getDirectories()) {

                for(Tag tag : directory.getTags()) {
                    data.put(tag.getTagName(), directory.getObject(tag.getTagType())+"");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
