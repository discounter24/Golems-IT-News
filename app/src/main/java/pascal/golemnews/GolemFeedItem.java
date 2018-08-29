package pascal.golemnews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GolemFeedItem {

    private Handler imageLoadedHandler;
    private String title;
    private String link;
    private String description;
    private String imageLink;
    private Date pubDate;
    private String guid;
    private String content;

    public Bitmap getImage() {
        return image;
    }

    private Bitmap image;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String search = "<a href=";
        if (description.contains(search)){
            this.description = description.substring(0, description.indexOf(search)-1);
            this.description.trim();
        }
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
        loadImage(imageLink).execute();
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            this.pubDate = sdf.parse(pubDate);
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public AsyncTask<Void,Void,Void> loadImage(final String link){
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    URL url = new URL(link);
                    image = BitmapFactory.decodeStream(url.openStream());
                    Message m = new Message();
                    m.what=2;
                    m.obj = this;
                    imageLoadedHandler.sendMessage(m);
                } catch (Exception ex){
                    //ignore
                }
                return null;
            }
        };
    }



    public GolemFeedItem(Handler imageLoadedHandler){
        this.imageLoadedHandler = imageLoadedHandler;
    }
}
