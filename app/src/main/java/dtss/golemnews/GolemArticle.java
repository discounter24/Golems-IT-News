package dtss.golemnews;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import dtss.golemnews.utils.ImageUtil;

class GolemArticle{


    private final GolemFeedItem item;
    private final IFeedArticleLoadHandler articleHandler;
    public Bitmap PreviewImage;

    public void setHtml(String html) {  this.html = html; }

    private String html = "";


    private LinkedList<String> chapter = new LinkedList<>();

    public LinkedList<String> getVideos() { return videos; }

    private LinkedList<String> videos = new LinkedList<>();

    private HashMap<String,Bitmap> HeroImages = new HashMap<>();


    public GolemArticle(GolemFeedItem item, IFeedArticleLoadHandler articleHandler) {
        this.item = item;
        this.articleHandler = articleHandler;
    }

    public String getHtml() { return html; }
    public LinkedList<String> getChapter() { return chapter; }

    public String getText(){
        String text = "";
        for(String chapter : getChapter()){
            text += chapter + "\n\n";
        }
        return text;
    }

    public void get(){
        new ArticleParseHeroImagesTask(articleHandler).execute(this);
        new ArticleParseTextTask(articleHandler).execute(this);
        new ArticleParseVideosTask(articleHandler).execute(this);
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
                Document d;
                if (article.getHtml().isEmpty()){
                    d = Jsoup.connect(article.item.getLink()).get();
                    html = d.html();
                } else {
                    d = Jsoup.parse(article.getHtml());
                }

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

            this.article = articles[0];
            article.chapter.clear();
            String text = "";

            try {
                Document d;
                if (article.getHtml().isEmpty()){
                    d = Jsoup.connect(article.item.getLink()).get();
                    html = d.html();
                } else {
                    d = Jsoup.parse(article.getHtml());
                }

                int part  = 1;
                Element element = d.getElementById("gpar" + part);


                while(element != null){

                    chapter.add(element.text());

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
            if (articleHandler != null) {
                articleHandler.ArticleTextReceived(article.item,result);
            }
        }
    }

    private class ArticleParseVideosTask extends AsyncTask<GolemArticle, Void, Void> {

        private final IFeedArticleLoadHandler articleHandler;
        private GolemArticle article;

        ArticleParseVideosTask(IFeedArticleLoadHandler articleHandler){
            this.articleHandler = articleHandler;
        }

        protected Void doInBackground(GolemArticle... articles) {

            this.article = articles[0];

            try {
                Document d;
                if (article.getHtml().isEmpty()){
                    d = Jsoup.connect(article.item.getLink()).get();
                    html = d.html();
                } else {
                    d = Jsoup.parse(article.getHtml());
                }


                Elements elements = d.getElementsByClass("gvideofig");

                for (Element videoElement : elements){
                    try{
                        String id = videoElement.id().substring(7);
                        article.videos.add("https://video.golem.de/amp-iframe.php?fileid=" + id);
                    } catch (Exception ex){
                        //Invalid video tag
                    }
                }
            } catch (IOException ex) {
                //Internet failure
            } catch (Exception ex) {
                //Unexpected failure
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            if (articleHandler != null) {
                for(String video : article.videos){
                    articleHandler.ArticleVideoFound(article.item,video);
                }
            }
        }
    }


}
