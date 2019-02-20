package dtss.golemnews;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import dtss.golemnews.utils.ImageUtil;

class GolemArticle{


    private final GolemFeedItem item;
    private final IFeedArticleLoadHandler articleHandler;
    public Bitmap PreviewImage;
    public String Text;
    private HashMap<String,Bitmap> HeroImages;


    public GolemArticle(GolemFeedItem item, IFeedArticleLoadHandler articleHandler) {
        this.HeroImages = new HashMap<>();
        this.item = item;
        this.articleHandler = articleHandler;
    }


    public void get(){
        new ArticleParseHeroImagesTask(articleHandler).execute(this);
        new ArticleParseTextTask(articleHandler).execute(this);
    }

    private class ArticleParseHeroImagesTask extends AsyncTask<GolemArticle, Void, Bitmap> {

        private final IFeedArticleLoadHandler articleHandler;
        private GolemArticle article;

        ArticleParseHeroImagesTask(IFeedArticleLoadHandler articleHandler){
            this.articleHandler = articleHandler;
        }

        protected Bitmap doInBackground(GolemArticle... articles) {
            this.article = articles[0];
            try {
                Document d = Jsoup.connect(article.item.getLink()).get();

                Elements heroes = d.getElementsByClass("hero");
                Element hero = heroes.first();
                if (hero != null){
                    Elements images = hero.getElementsByTag("img");
                    Element image = images.first();
                    if (image != null){
                        String imageLink = image.attr("src");
                        return ImageUtil.loadImage(imageLink);
                    }
                }

            } catch (IOException ex) {
                //NoInternet
            } catch (Exception ex) {
                //Unknown
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            article.PreviewImage = result;
            if (articleHandler != null){
                articleHandler.ArticleImageLoaded(article.item,result);
            }
        }
    }

    private class ArticleParseTextTask extends AsyncTask<GolemArticle, Void, String> {

        private final IFeedArticleLoadHandler articleHandler;
        private GolemArticle article;

        ArticleParseTextTask(IFeedArticleLoadHandler articleHandler){
            this.articleHandler = articleHandler;
        }

        protected String doInBackground(GolemArticle... articles) {
            String text = "";
            this.article = articles[0];

            try {
                Document d = Jsoup.connect(article.item.getLink()).get();

                int part  = 1;
                Element element = d.getElementById("gpar" + part);


                while(element != null){

                    text += element.text() + "\n\n";

                    part++;
                    element = d.getElementById("gpar" + part);
                }

            } catch (IOException ex) {

                text = "Can't connect to golem.de\n\n";
                text += ex.toString();
            } catch (Exception ex) {
                text = "UNKNOWN_ERROR\n";
                text += ex.toString();
            }

            return text;
        }

        protected void onPostExecute(String result) {
            article.Text = result;
            if (articleHandler != null) {
                articleHandler.ArticleTextReceived(article.item,result);
            }
        }
    }


}
