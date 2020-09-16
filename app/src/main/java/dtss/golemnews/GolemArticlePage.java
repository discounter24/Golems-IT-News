package dtss.golemnews;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import dtss.golemnews.utils.ImageUtil;

class GolemArticlePage {
    private final String link;

    private boolean chaptersLoaded;
    private boolean videosLoaded;
    private boolean imagesLoaded;

    private String html;

    private final LinkedList<String> chapters;
    private final LinkedList<String> videos;
    private final LinkedList<GolemImage> images;


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


    public void requestText(final IPageHandler handler){
        if (chaptersLoaded){
            StringBuilder text = new StringBuilder();
            for(String chapter : getChapers()){
                text.append(chapter).append("\n\n");
            }
            handler.onTextReceived(this, text.toString());
        } else {


            DiskCache.getInstance().requestText(link, new DiskCache.ICacheAnswerHandler() {
                @Override
                public void onCacheAnswer(Object cacheObject, boolean found) {
                    if (found){
                        if (cacheObject instanceof LinkedList){
                            @SuppressWarnings("unchecked") LinkedList<String> chapters = (LinkedList<String>) cacheObject;
                            GolemArticlePage.this.chapters.addAll(chapters);
                            chaptersLoaded = true;
                            requestText(handler);
                        }

                    } else {
                        GolemArticlePageTextLoadTask task = new GolemArticlePageTextLoadTask(handler);
                        task.execute(GolemArticlePage.this);
                        chaptersLoaded=true;
                    }
                }


            });

        }
    }

    public void requestImages(final IPageHandler handler) {
        if (imagesLoaded){
            handler.onImagesReceived(this, images);
        } else {
            DiskCache.getInstance().requestImage(link, new DiskCache.ICacheAnswerHandler() {
                @Override
                public void onCacheAnswer(Object cacheObject, boolean found) {

                    if (found){
                        if (cacheObject instanceof LinkedList){
                            @SuppressWarnings("unchecked") LinkedList<GolemImage> images = (LinkedList<GolemImage>)cacheObject;
                            GolemArticlePage.this.images.addAll(images);
                            imagesLoaded = true;
                            requestImages(handler);
                        }

                    } else {
                        GolemArticlePageImageLoadTask task = new GolemArticlePageImageLoadTask(handler);
                        task.execute(GolemArticlePage.this);
                        imagesLoaded = true;
                    }
                }

            });



        }
    }

    public void requestVideos(final IPageHandler handler)  {
        if (videosLoaded){
            handler.onVideosReceived(this, videos);
        } else {
            DiskCache.getInstance().requestVideo(link, new DiskCache.ICacheAnswerHandler() {
                @Override
                public void onCacheAnswer(Object cacheObject, boolean found) {
                    if (found){
                        if (cacheObject instanceof LinkedList){
                            @SuppressWarnings("unchecked") LinkedList<String> videos = (LinkedList<String>)cacheObject;
                            GolemArticlePage.this.videos.addAll(videos);
                            videosLoaded = true;
                            requestVideos(handler);
                        }
                    } else {
                        GolemArticlePageVideoLoadTask task = new GolemArticlePageVideoLoadTask(handler);
                        task.execute(GolemArticlePage.this);
                        videosLoaded = true;
                    }
                }
            });


        }

    }


    public String getLink(){ return link; }


    private void addImage(GolemImage image){
        this.images.add(image);
    }

    private void addChapter(String chapter){
        this.chapters.add(chapter);
    }

    private void addVideo(String videoLink){
        this.videos.add(videoLink);
    }


    private String getHtml(){
        return html;
    }

    private void setHtml(String html){
        this.html = html;
    }

    public LinkedList<String> getChapers(){
        return chapters;
    }

    private static Document getDocument(final GolemArticlePage page) throws IOException {
        Document d;
        if (page.getHtml().isEmpty()){


            Map<String,String> cookies = new HashMap<>();
            cookies.put("golem-cookie-accept","true");
            cookies.put("consentUUID","3931204b-5b91-4e97-aba7-a7c3c5aa119c");
            cookies.put("golem_consent20","cmp|200801");
            cookies.put("golem_first_popup","true");
            cookies.put("golem_lp","8uvudh3i753rvez5i3ipiz1wkmbgq9e0");
            cookies.put("euconsent-v2","CO5yGm4O5yGm4AGABCENA3CsAP_AAH_AAAYgGYtf_X9fb2_j-_5999t0eY1f9_63v-wzjgeNs-8NyZ_X_L4Xr2MyvB34pq4KmR4Eu3LBAQdlHGHcTQmQwIkVqTLsak2Mq7NKJ7JEilMbM2dYGG1vn8XT_ZCY70__f__7_3-_-___67YGXkEGAAKAAACCAAAEihEIAAIAxIAAAAAihEAoEkACRQADK4COQAAABAYgIQAAQAgIQYBAAAAAEkAQAgAQIBAARAIAAQADAEAACAAEFgBICAAACIEhAARABKBAQRAAQcgAQFQBBAChAgFDIDAAFAAVABDACYAFwARwApABlgDUAH4ARgAjgBSwCrgFbAN4AmIBNgCmwFogLzAYEAw8BkQiA8ACoAKwAXABDACkAGQAMsAagA2QB-AEYAJEAUsAp4BVwDWAHyAQ2Ah0BF4CRAE2AJ2AUOApEBcgDAgGEgMPAZOEgqAAIAAXABQAFQAMgAcAA8ACAAEQAKgAaAA8AB9AEMARAAmABPgCqAKwAWAAuABvADmAHoAQgAhoBEAESAI4ASwAmgBSgDDAGQAMsAagA2QBxADvAHsAPiAfYB-gEYAI0ARwAkQBKQClgFPAKuAXMAvwBhADFAGsANoAbgA3gBygD0AHyAQ2Ah0BFQCLwEiAJiATKAmwBOwChwFIgKbAVgAsUBbAC5AF3gLzAYEAwYBhIDDQGHgMiAZIAycBlwUACAokNAiABUAFYALgAhgBSADIAGWANIAagA2QB-AEFAIwAUsAp4BV4C0ALSAawA3gB8gENgIdAReAkQBNgCdgFDgKRAXIAwIBhIDDwGMAMnFQGQAKAAqACGAEwALgAjgBSADLAGoAPwAjABHAClgFXgLQAtIBvAEggJiATYApsBcgC8wGBAMPAZEOgrgALgAoACoAFwAMgAcABAACIAFQAMQAaAA8AB9AEMARAAmABPgCqAKwAWAAuABiADMAG8AOYAegBCACGAEQAI4ASwAmABNQCjAKUAWIAyABlQDRANIAagA2YBvgHAAOIAd4A9oB9gH6ARgAjgBKQCngFXALFAWgBaQC5gF5AL8AYQAxQBtADcAHKgOmA6gB6AENgIdAREAi8BIICRAE2AJ2AUPApoCmwFWALFgWwBbIC4AFyALtAXeAvMBhIDDQGHgMSAYwAx4BkgDJwGVAMuIQNAAFgAUAAyACIAFQAMQAhgBMACqAFwAMQAZgA3gB6AEcAKQAWIAygBqADfAHEAO8AfgBAwCMAEcAJSAUMAp4BV4C0ALSAXMAvwBhADFAG0AOoAegBIICRAE2QKaApsBYoC0YFsAW0AuABcgC7QGHgMSAZEAyclAtAAQAAsACgAFwAMgAcABiADwAIgATAAqgBcADEAGYANoAhABDQCIAIkAR0AowClAGUANIAbIA7wB-AEYAI4AU8Aq8BaAFpAMUAbgA5QB1AD5AIdAReAkQBNgCxQF2gLzAYeAyIBk5SCGAAuACgAKgAXAAyABwAEAAKgAYgA0AB5AEMARAAmABPACkAFUALAAXAAxABmADmAIQAQwAiIBRgFKALEAZUA0QDSAGyAOIAd8A-wD9AIwARwAlIBQwCrgFbALmAXkAwgBtADcAHKAOoAegBDoCLwEiAJsATsAoeBTQFNgK2AWKAtgBcAC5AF2gLzAYaAw8BiQDGAGRAMkAZOAy4AAA.YAAAAAAAAAAA");
            cookies.put("xdefcc","G4bd8021b1a662935d6dcf7c7be5321d35");
            cookies.put("Golem_rngnaf","true");


            d = Jsoup.connect(page.getLink()).cookies(cookies).get();
            page.setHtml(d.html());

        } else {
            d = Jsoup.parse(page.getHtml());
        }

        return d;
    }


    private static class GolemArticlePageImageLoadTask extends AsyncTask<GolemArticlePage, Void, Void> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private final LinkedList<GolemImage> images;


        GolemArticlePageImageLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            images = new LinkedList<>();
        }

        protected Void doInBackground(GolemArticlePage... articles) {


            this.page = articles[0];
            try {
                Document d = getDocument(page);

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



    private static class GolemArticlePageTextLoadTask extends AsyncTask<GolemArticlePage, Void, String> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private final LinkedList<String> chapters;


        GolemArticlePageTextLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            chapters = new LinkedList<>();
        }

        protected String doInBackground(GolemArticlePage... articles) {

            this.page = articles[0];
            String text = "";

            try {
                Document d = getDocument(page);

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
                StringBuilder text = new StringBuilder();
                for (String  c : chapters) {
                    page.addChapter(c);
                    text.append(c).append("\n\n");
                }

                articleHandler.onTextReceived(page, text.toString());
            }
        }
    }

    private static class GolemArticlePageVideoLoadTask extends AsyncTask<GolemArticlePage, Void, Void> {

        private final IPageHandler articleHandler;
        private GolemArticlePage page;
        private final LinkedList<String> videos;

        GolemArticlePageVideoLoadTask(IPageHandler articleHandler){
            this.articleHandler = articleHandler;
            videos = new LinkedList<>();
        }

        protected Void doInBackground(GolemArticlePage... articles) {

            this.page = articles[0];

            try {
                Document d = getDocument(page);

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
