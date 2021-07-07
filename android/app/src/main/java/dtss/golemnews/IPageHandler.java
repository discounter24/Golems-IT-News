package dtss.golemnews;

import java.util.LinkedList;

@SuppressWarnings("unused")
interface IPageHandler {

    void onHtmlReceived(GolemArticlePage sender, String text);
    void onTextReceived(GolemArticlePage sender, String text);
    void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images);
    void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos);

}
