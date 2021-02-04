package dtss.golemnews;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

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


// Input: ArticleLink, Nichts, Output: ArtikelText
public class GolemFullTextParser extends AsyncTask<String, Void, String> {

    public static final String awsLambdaParserFunction = "https://edki1d06pe.execute-api.eu-central-1.amazonaws.com/default/golemFunction";


    @Override
    protected String doInBackground(String... strings) {
        try {
            String rss = getFullRss();
            LinkedList x = GolemFullTextParser.getItemsFromRSS(rss);

            return "okay";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }


    public static  LinkedList<Pair<String,String>> getItemsFromRSS(String rss) throws IOException {
        LinkedList<Pair<String,String>> Items = new LinkedList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(rss));

            String tag = "";
            String lastText = "";
            String first = "";
            String second = "";

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                if (parser.getName() != null){
                    tag = parser.getName();
                }


               switch (event) {

                   case XmlPullParser.TEXT:
                       lastText = parser.getText();
                       break;
                   case XmlPullParser.END_TAG:
                       if (tag.equalsIgnoreCase("entry")) {
                           Items.add(new Pair<String, String>(first,second));
                       } else if (tag.equalsIgnoreCase("id")){
                           first = lastText;
                       } else if (tag.equalsIgnoreCase("summary")) {
                           second = lastText;
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

    public static String getFullRss() throws IOException {
        URL feed_url = new URL(awsLambdaParserFunction);
        InputStream stream = feed_url.openStream();
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.ISO_8859_1);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder rss = new StringBuilder();
        String line = "";
        do {
            try {
                line = reader.readLine();
                if (line != null) rss.append(line).append("\n");
            } catch (IOException ex) {
                Log.e("Feed loading error", ex.toString());
            }
        } while (line != null);
        return rss.toString();
    }


}
