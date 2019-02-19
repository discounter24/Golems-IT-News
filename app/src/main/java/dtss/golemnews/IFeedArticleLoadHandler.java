package dtss.golemnews;

import android.graphics.Bitmap;

public interface IFeedArticleLoadHandler {

    void ArticleTextReceived(GolemFeedItem item, String text);
    void ArticleImageLoaded(GolemFeedItem item, Bitmap image);

}
