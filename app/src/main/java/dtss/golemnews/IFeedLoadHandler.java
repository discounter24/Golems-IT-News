package dtss.golemnews;

import android.graphics.Bitmap;

interface IFeedLoadHandler {
    void FeedItemListLoaded(GolemFeedLoadTask.GolemFeedLoadTaskResult result, GolemFeed feed);
    void FeedPreviewImageLoaded(GolemFeedItem sender, Bitmap image);
}
