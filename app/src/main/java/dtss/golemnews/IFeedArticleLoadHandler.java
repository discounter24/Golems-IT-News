package dtss.golemnews;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
interface IFeedArticleLoadHandler {

    void ArticleTextReceived(GolemFeedItem sender, String text);
    void ArticleImageLoaded(GolemFeedItem sender, Bitmap image);
    void ArticleVideoFound(GolemFeedItem sender, String embedUrl);

}
