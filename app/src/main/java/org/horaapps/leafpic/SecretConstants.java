package org.horaapps.leafpic;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dnld on 31/07/16.
 */
public class SecretConstants {

    private static String base64EncodedPublicKey;

    public static String MAP_BOX_TOKEN = "pk.eyJ1IjoiZG5sZCIsImEiOiJjaXJycmVham4wMGRsaGpuaHQ4Y3Fhb2MzIn0.kUn2aNbfpS3-wDJ-s0DLFw";

    public static String getBase64EncodedPublicKey(Context context) {
        if (base64EncodedPublicKey == null) {
            InputStream input;
            try {
                input = context.getAssets().open("secretconstants.properties");
                Properties properties = new Properties();
                properties.load(input);
                base64EncodedPublicKey = properties.getProperty("gplaykey");
            } catch (IOException e) {
                // file not found
                base64EncodedPublicKey = "";
            }
        }
        return base64EncodedPublicKey;
    }
}
