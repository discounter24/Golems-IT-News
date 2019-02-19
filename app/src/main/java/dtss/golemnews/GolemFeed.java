package dtss.golemnews;


import java.util.LinkedList;

public class GolemFeed {


    private final String feedUrl = "https://rss.golem.de/rss.php?feed=RSS2.0";
    private LinkedList<GolemFeedItem> Items;
    private IFeedLoadHandler FeedItemsUpdatedCallbackHandler;


    public GolemFeed(IFeedLoadHandler feedHandler){
        this.Items = new LinkedList<>();
        this.FeedItemsUpdatedCallbackHandler = feedHandler;
    }

    public LinkedList<GolemFeedItem> getFeedItems(){
        return Items;
    }

    public void setFeedItems(LinkedList<GolemFeedItem> items){
        this.Items = items;
    }

    public void addItems(LinkedList<GolemFeedItem> items){
        for(GolemFeedItem newItem : items){
            Boolean existing = false;
            for(GolemFeedItem existingItem : this.Items){
                if (newItem.getLink().equals(existingItem.getLink())){
                    existing = true;
                    break;
                }
            }
            if (!existing){
                this.Items.add(newItem);
            }
            //Preload:
            //newItem.getArticle(null);
        }
    }

    public String getFeedUrl(){
        return feedUrl;
    }


    public void updateListItems(){
        GolemFeedLoadTask task = new GolemFeedLoadTask(FeedItemsUpdatedCallbackHandler);
        task.execute(this);
    }

    public GolemFeedItem getItemByGuid(String guid){
        for(GolemFeedItem item : Items){
            if (item.getGuid().equals(guid)){
                return item;
            }
        }
        return null;
    }


}
