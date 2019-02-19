package dtss.golemnews;

import android.graphics.Bitmap;

public interface IFeedLoadHandler {
    void FeedItemListLoaded(GolemFeedLoadTask.GolemFeedLoadTaskResult result, GolemFeed feed);
    void FeedPreviewImageLoaded(GolemFeedItem sender, Bitmap image);

}
