package dtss.golemnews;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
interface IFeedArticleLoadHandler {

    void ArticleTextReceived(GolemFeedItem item, String text);
    void ArticleImageLoaded(GolemFeedItem item, Bitmap image);

}
