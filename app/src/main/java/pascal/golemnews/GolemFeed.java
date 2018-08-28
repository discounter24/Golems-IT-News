package pascal.golemnews;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
