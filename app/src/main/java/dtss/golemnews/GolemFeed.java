package dtss.golemnews;
import android.os.Handler;

import java.util.LinkedList;

public class GolemFeed {


    private Handler feedUpdater;
    private LinkedList<GolemFeedItem> Items;
    private String rss;



    public GolemFeed(Handler feedUpdater){
        Items = new LinkedList();
        this.feedUpdater = feedUpdater;
    }



    public void Load(){
        GolemFeedLoadTask task = new GolemFeedLoadTask(this.feedUpdater);
        task.execute();
    }





}
