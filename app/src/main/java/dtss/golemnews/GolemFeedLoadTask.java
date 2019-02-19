package dtss.golemnews;

import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

public class GolemFeedLoadTask extends AsyncTask<GolemFeed, Void, HashMap<GolemFeed, GolemFeedLoadTask.GolemFeedLoadTaskResult>> {
    private IFeedLoadHandler handler;


    public GolemFeedLoadTask(IFeedLoadHandler handler) {
        this.handler = handler;
    }

    protected HashMap<GolemFeed, GolemFeedLoadTaskResult> doInBackground(GolemFeed... feeds) {

        HashMap<GolemFeed, GolemFeedLoadTaskResult> results = new HashMap<>();

        for (GolemFeed feed : feeds ) {
            GolemFeedLoadTaskResult result;

            try {
                String rss = getRSS(feed.getFeedUrl());
                if (rss != null) {
                    LinkedList<GolemFeedItem> items = getItemsFromRSS(rss);
                    feed.addItems(items);
                }
                result = GolemFeedLoadTaskResult.OK;
            } catch (IOException ex) {
                result = GolemFeedLoadTaskResult.InternetError;
                ex.printStackTrace();
            } catch (Exception ex) {
                result  = GolemFeedLoadTaskResult.UnknownError;
                ex.printStackTrace();
            }

            results.put(feed,result);
        }
        return results;
    }

    protected void onPostExecute(HashMap<GolemFeed, GolemFeedLoadTaskResult> results){
        for (GolemFeed feed : results.keySet()) {
            handler.FeedItemListLoaded(results.get(feed),feed);
        }
    }


    private LinkedList<GolemFeedItem> getItemsFromRSS(String rss) throws IOException {
        LinkedList<GolemFeedItem> Items = new LinkedList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(rss));

            GolemFeedItem item = new GolemFeedItem(handler);
            String tag = "";
            String text = "";

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                tag = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase("item")) {
                            item = new GolemFeedItem(handler);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tag.equalsIgnoreCase("item")) {
                            Items.add(item);
                        } else if (tag.equalsIgnoreCase("title")) {
                            item.setTitle(text);
                        } else if (tag.equalsIgnoreCase("link")) {
                            item.setLink(text);
                        } else if (tag.equalsIgnoreCase("description")) {
                            item.setDescription(text);
                        } else if (tag.equalsIgnoreCase("pubDate")) {
                            item.setPubDate(text);
                        } else if (tag.equalsIgnoreCase("guid")) {
                            item.setGuid(text);
                        } else if (tag.equalsIgnoreCase("encoded")) {
                            item.setPreviewImageLink(text.substring(text.indexOf("src='") + 11, text.indexOf("jpg") + 3));
                        }

                        break;
                    default:
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException ex) {
            Log.e("Feed loading error", ex.toString());
        }
        return Items;
    }

    private String getRSS(String url) throws IOException {
        URL feed_url = new URL(url);
        InputStream stream = feed_url.openStream();
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.ISO_8859_1);
        BufferedReader reader = new BufferedReader(streamReader);
        String rss = "";
        String line = "";
        do {
            try {
                line = reader.readLine();
                if (line != null) rss += line + "\n";
            } catch (IOException ex) {
                Log.e("Feed loading error", ex.toString());
            }
        } while (line != null);
        return rss;
    }




    public enum GolemFeedLoadTaskResult {
        OK, InternetError, UnknownError
    }

}

