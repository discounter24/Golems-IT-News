package dtss.golemnews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class GolemFeedItem implements IFeedArticleLoadHandler {

    private IFeedLoadHandler feedLoadHandler;

    private String title;
    private String link;
    private String description;

    private String previewImageLink;
    private Bitmap previewImage;

    private Date pubDate;
    private String guid;
    private GolemArticle article;



    private final LinkedList<String> videos = new LinkedList<>();

    private final LinkedList<IFeedArticleLoadHandler> waitingImageHandlers = new LinkedList<>();
    private final LinkedList<IFeedArticleLoadHandler> waitingTextHandlers = new LinkedList<>();
    private final LinkedList<IFeedArticleLoadHandler> waitingVideoHandlers = new LinkedList<>();


    public Bitmap getPreviewImage() {
        return previewImage;
    }

    public LinkedList<String> getVideos() {  return videos;  }

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

    public void getArticle(IFeedArticleLoadHandler articleHandler) {



        if (article == null || article.getText().isEmpty() || article.PreviewImage == null) {

            if (waitingImageHandlers.size() == 0 ) {
                waitingImageHandlers.add(articleHandler);
                waitingTextHandlers.add(articleHandler);
                waitingVideoHandlers.add(articleHandler);

                GolemArticle tmp = new GolemArticle(this, this);
                tmp.get();
                this.article = tmp;

            } else {
                waitingImageHandlers.add(articleHandler);
                waitingTextHandlers.add(articleHandler);
                waitingVideoHandlers.add(articleHandler);
            }

        } else {
            if (articleHandler!= null) {
                articleHandler.ArticleImageLoaded(this, article.PreviewImage);
                articleHandler.ArticleTextReceived(this, article.getText());

                for (String video : videos){
                    articleHandler.ArticleVideoFound(this, video);
                }
            }
        }
    }



    @Override
    public void ArticleTextReceived(GolemFeedItem item, String text) {
        for(IFeedArticleLoadHandler articleHandler : waitingTextHandlers){
            if (articleHandler != null){
                articleHandler.ArticleTextReceived(this,article.getText());
            }
        }
        waitingTextHandlers.clear();
    }

    @Override
    public void ArticleImageLoaded(GolemFeedItem item, Bitmap image) {
        for(IFeedArticleLoadHandler articleHandler : waitingImageHandlers){
            if (articleHandler != null){
                articleHandler.ArticleImageLoaded(this,article.PreviewImage);
            }
        }
        waitingImageHandlers.clear();
    }

    @Override
    public void ArticleVideoFound(GolemFeedItem sender, String embedUrl) {
        videos.add(embedUrl);
        for(IFeedArticleLoadHandler articleHandler : waitingVideoHandlers){
            if (articleHandler != null){
                articleHandler.ArticleVideoFound(this,embedUrl);
            }
        }
        waitingVideoHandlers.clear();
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


}
