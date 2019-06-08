package dtss.golemnews;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;

import dtss.golemnews.utils.ImageUtil;

public class GolemArticlePage {
    private final String link;

    private boolean chaptersLoaded;
    private boolean videosLoaded;
    private boolean imagesLoaded;

    private String html;

    private LinkedList<String> chapters;
    private LinkedList<String> videos;
    private LinkedList<GolemImage> images;


    public GolemArticlePage(String link){
        this.link = link;
        chapters = new LinkedList<>();
        videos = new LinkedList<>();
        images = new LinkedList<>();
        html = "";
        imagesLoaded = false;
        videosLoaded = false;
        chaptersLoaded = false;
    }


    public void requestText(IPageHandler handler){
        if (chaptersLoaded){
            String text = "";
            for(String chapter : getChapers()){
                text += chapter + "\n\n";
            }
            handler.onTextReceived(this,text);
        } else {
            GolemArticlePageTextLoadTask task = new GolemArticlePageTextLoadTask(handler);
            task.execute(this);
            chaptersLoaded = true;
        }
    }

    public void requestImages(IPageHandler handler) {
        if (imagesLoaded){
            handler.onImagesReceived(this, images);
        } else {
            GolemArticlePageImageLoadTask task = new GolemArticlePageImageLoadTask(handler);
            task.execute(this);
            imagesLoaded = true;
        }
    }

    public void requestVideos(IPageHandler handler)  {
        if (videosLoaded){
            handler.onVideosReceived(this, videos);
        } else {
            GolemArticlePageVideoLoadTask task = new GolemArticlePageVideoLoadTask(handler);
            task.execute(this);
            videosLoaded = true;
        }

    }


    public String getLink(){ return link; }


    public void addImage(GolemImage image){
        this.images.add(image);
    }

    public void addChapter(String chapter){
        this.chapters.add(chapter);
    }

    public void addVideo(String videoLink){
        this.videos.add(videoLink);
    }


    public String getHtml(){
        return html;
    }

    public void setHtml(String html){
        this.html = html;
    }

    public LinkedList<String> getChapers(){
        return chapters;
    }




    private class GolemArticlePageImageLoadTask extends AsyncTask<GolemArticlePage, Void, Void> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private LinkedList<GolemImage> images;


        public GolemArticlePageImageLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            images = new LinkedList<>();
        }

        protected Void doInBackground(GolemArticlePage... articles) {
            this.page = articles[0];
            try {
                Document d;
                if (page.getHtml().isEmpty()){
                    d = Jsoup.connect(page.getLink()).get();
                    page.setHtml(d.html());
                } else {
                    d = Jsoup.parse(page.getHtml());
                }

                Elements heroes = d.getElementsByClass("hero");
                Element hero = heroes.first();
                if (hero != null){
                    Elements images = hero.getElementsByTag("img");
                    Element imageElement = images.first();
                    if (imageElement != null){
                        String imageLink = imageElement.attr("src");

                        Bitmap image = ImageUtil.loadImage(imageLink);
                        String description = imageElement.attr("alt");
                        String author = "";
                        for(Element authorElement : hero.getElementsByClass("big-image-lic")){
                            author = authorElement.text();
                        }

                        GolemImage gi = new GolemImage(imageLink,author,description,image);
                        this.images.add(gi);
                    }
                }

            } catch (IOException ex) {
                //NoInternet
            } catch (Exception ex) {
                //Unknown
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            if (articleHandler != null){
                for (GolemImage  i : images) {
                    page.addImage(i);
                }
                articleHandler.onImagesReceived(page, images);
            }
        }
    }

    private class GolemArticlePageTextLoadTask extends AsyncTask<GolemArticlePage, Void, String> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private LinkedList<String> chapters;


        public GolemArticlePageTextLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            chapters = new LinkedList<>();
        }

        protected String doInBackground(GolemArticlePage... articles) {

            this.page = articles[0];
            String text = "";

            try {
                Document d;
                if (page.getHtml().isEmpty()){
                    d = Jsoup.connect(page.getLink()).get();
                    html = d.html();
                } else {
                    d = Jsoup.parse(page.getHtml());
                }

                int part  = 1;
                Element element = d.getElementById("gpar" + part);


                while(element != null){

                    chapters.add(element.text());

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
            if (articleHandler != null){
                String text = "";
                for (String  c : chapters) {
                    page.addChapter(c);
                    text += c + "\n\n";
                }

                articleHandler.onTextReceived(page, text);
            }
        }
    }

    private class GolemArticlePageVideoLoadTask extends AsyncTask<GolemArticlePage, Void, Void> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private LinkedList<String> videos;

        public GolemArticlePageVideoLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            videos = new LinkedList<>();
        }

        protected Void doInBackground(GolemArticlePage... articles) {

            this.page = articles[0];

            try {
                Document d;
                if (page.getHtml().isEmpty()){
                    d = Jsoup.connect(page.getLink()).get();
                    html = d.html();
                } else {
                    d = Jsoup.parse(page.getHtml());
                }


                Elements elements = d.getElementsByClass("gvideofig");

                for (Element videoElement : elements){
                    try{
                        String id = videoElement.id().substring(7);
                        videos.add("https://video.golem.de/amp-iframe.php?fileid=" + id);
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
            if (articleHandler != null){
                for (String video : videos) {
                    page.addVideo(video);
                }
                articleHandler.onVideosReceived(page, videos);
            }
        }



    }

}
