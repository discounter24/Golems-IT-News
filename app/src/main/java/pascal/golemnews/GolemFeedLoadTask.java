package pascal.golemnews;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import java.util.LinkedList;

public class GolemFeedLoadTask extends AsyncTask<Void, Void, Boolean> {


    public Handler updateHandler;

    public GolemFeedLoadTask(Handler updateHandler) {
        this.updateHandler = updateHandler;
    }

    protected Boolean doInBackground(Void... params) {

        GolemFeedLoadTaskResult result = new GolemFeedLoadTaskResult();

        try {
            String rss = getRSS("https://rss.golem.de/rss.php?feed=RSS2.0");
            if (rss != null) {
                result.FeedItems = getItemsFromRSS(rss);
            }
        } catch (IOException ex) {
            result.Type = GolemFeedLoadTaskResultType.InternetError;
            ex.printStackTrace();
        } catch (Exception ex) {
            result.Type = result.Type = GolemFeedLoadTaskResultType.UnknownError;
            ex.printStackTrace();
        }

        Message message = new Message();
        message.obj = result;
        message.what = 1;

        updateHandler.sendMessage(message);

        return true;
    }


    private LinkedList<GolemFeedItem> getItemsFromRSS(String rss) throws IOException {
        LinkedList<GolemFeedItem> Items = new LinkedList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(rss));

            GolemFeedItem item = new GolemFeedItem(updateHandler);
            String tag = "";
            String text = "";

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                tag = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase("item")) {
                            item = new GolemFeedItem(updateHandler);
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
                            item.setImageLink(text.substring(text.indexOf("src='") + 11, text.indexOf("jpg") + 3));
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

}

