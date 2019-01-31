package dtss.golemnews;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;

public class GolemArticle {


    private GolemFeedItem item;
    private Handler receivedHandler;


    public GolemArticle(GolemFeedItem item) {
        this.item = item;
    }



    public void receive(Handler handler){
        this.receivedHandler = handler;
        new ArticleParseTask().execute(item);
    }


    private class ArticleParseTask extends AsyncTask<GolemFeedItem, Void, String> {

        protected String doInBackground(GolemFeedItem... items) {
            String text = "";

            try {
                Document d = Jsoup.connect(item.getLink()).get();

                int part  = 1;
                Element element = d.getElementById("gpar" + part);

                while(element != null){

                    text += element.text() + "\n\n";

                    part++;
                    element = d.getElementById("gpar" + part);
                }
            } catch (IOException ex) {
                text = "IO_ERROR\n";
                text += ex.toString();
            } catch (Exception ex) {
                text = "UNKNOWN_ERROR\n";
                text += ex.toString();
            }

            return text;
        }

        protected void onPostExecute(String result) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = result;
            receivedHandler.sendMessage(msg);
        }

    }


}
