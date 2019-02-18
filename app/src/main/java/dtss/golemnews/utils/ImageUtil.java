package dtss.golemnews.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

public class ImageUtil {

    public static AsyncTask<Void,Void,Void> loadImageAsync(final String ID, final String link, final IImageLoadHandler handler){
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                //Log.d("IMAGE_LOADER",ID + link);
                Bitmap image = loadImage(link);
                handler.ImageLoaded(ID,image);
                return null;
            }
        };
    }


    public static Bitmap loadImage(final String link){
        try {
            URL url = new URL(link);
            InputStream stream = url.openStream();
            Bitmap image = BitmapFactory.decodeStream(stream);
            stream.close();
            return image;
        } catch (Exception ex){
            return null;
        }

    }

}
