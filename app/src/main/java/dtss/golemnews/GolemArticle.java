package dtss.golemnews;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.HashMap;

import dtss.golemnews.utils.IImageLoadHandler;
import dtss.golemnews.utils.ImageUtil;

public class GolemArticle implements IImageLoadHandler{


    public GolemFeedItem item;
    public IImageLoadHandler imageLoadHandler;
    private Handler receivedHandler;
    public HashMap<String,Bitmap> HeroImages;


    public GolemArticle(GolemFeedItem item, IImageLoadHandler imageLoadHandler) {
        this.HeroImages = new HashMap<String,Bitmap>();
        this.item = item;
        this.imageLoadHandler = imageLoadHandler;
    }


    public void receive(Handler handler){
        this.receivedHandler = handler;
        new ArticleParseTask().execute(this);
    }

    public void ImageLoaded(String ID, Bitmap image){
        if (ID.equalsIgnoreCase("previewImage")){
            HeroImages.put(ID,image);
            imageLoadHandler.ImageLoaded(ID,image);
        }
    }


    private class ArticleParseTask extends AsyncTask<GolemArticle, Void, String> {

        protected String doInBackground(GolemArticle... articles) {
            String text = "";

            try {
                Document d = Jsoup.connect(item.getLink()).get();

                int part  = 1;
                Element element = d.getElementById("gpar" + part);


                while(element != null){

                    text += element.text() + "\n\n";

                    part++;
                    element = d.getElementById("gpar" + part);
                }

                Elements heroes = d.getElementsByClass("hero");
                Element hero = heroes.first();
                if (hero != null){
                    Elements images = hero.getElementsByTag("img");
                    Element image = images.first();
                    if (image != null){
                        String imageLink = image.attr("src");
                        Bitmap map = ImageUtil.loadImage(imageLink);
                        articles[0].ImageLoaded("previewImage", map);
                    }
                }

            } catch (IOException ex) {
                text = "IO_ERROR\n";
                text += ex.toString();
            } catch (Exception ex) {
                text = "UNKNOWN_ERROR\n";
                text += ex.toString();
            }

            return text;
        }



        protected void onPostExecute(String result) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = result;
            receivedHandler.sendMessage(msg);
        }

    }


}
