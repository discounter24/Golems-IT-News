package dtss.golemnews;

import java.util.LinkedList;

@SuppressWarnings("unused")
interface IFeedArticleLoadHandler {

    void ArticleTextReceived(GolemFeedItem sender, String text);
    void ArticleImagesLoaded(GolemFeedItem sender, LinkedList<GolemArticle.GolemImage> images);
    void ArticleVideoFound(GolemFeedItem sender, String embedUrl);

}
