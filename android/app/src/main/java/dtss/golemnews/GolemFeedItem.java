package dtss.golemnews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class GolemFeedItem implements IPageHandler {

    private final IFeedLoadHandler feedLoadHandler;

    private String title;
    private String link;
    private String description;

    private String previewImageLink;
    private Bitmap previewImage;

    private Date pubDate;
    private String guid;
    private GolemArticle article;

    //private final LinkedList<String> videos = new LinkedList<>();

    //private final LinkedList<IPageHandler> waitingImageHandlers = new LinkedList<>();
    //private final LinkedList<IPageHandler> waitingTextHandlers = new LinkedList<>();
    //private final LinkedList<IPageHandler> waitingVideoHandlers = new LinkedList<>();


    public Bitmap getPreviewImage() {
        return previewImage;
    }

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
            this.description = this.description.trim();
        }
    }

    public String getPreviewImageLink() {
        return previewImageLink;
    }

    public void setPreviewImageLink(String previewImageLink) {
        this.previewImageLink = previewImageLink;
        loadImage(previewImageLink).execute();
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

    public GolemArticle getArticle(){
        if (article == null){
            article = new GolemArticle(this);
        }
        return article;
    }


    private AsyncTask<Void,Void,Bitmap> loadImage(final String link){
        final GolemFeedItem sender = this;

        return new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                try{
                    URL url = new URL(link);
                    previewImage = BitmapFactory.decodeStream(url.openStream());
                    return previewImage;
                } catch (Exception ex){
                    return null;
                }
            }

            protected void onPostExecute(Bitmap image){
                if (feedLoadHandler!=null){
                    feedLoadHandler.FeedPreviewImageLoaded(sender, image);
                }
            }
        };
    }



    public GolemFeedItem(IFeedLoadHandler feedLoadHandler){
        this.feedLoadHandler = feedLoadHandler;
    }


    @Override
    public void onTextReceived(GolemArticlePage sender, String text) {

    }

    @Override
    public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {

    }

    @Override
    public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {

    }
}
