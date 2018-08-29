package pascal.golemnews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GolemFeedItem {

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
        try{

            URL url = new URL(imageLink);
            image = BitmapFactory.decodeStream(url.openStream());
        } catch (Exception ex){
            //Ignore
        }
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            this.pubDate = sdf.parse(pubDate);
        } catch (Exception ex){
            //ignore
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



    public GolemFeedItem(){

    }
}
