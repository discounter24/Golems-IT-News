package dtss.golemnews;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.json.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dtss.golemnews.utils.AWSUtil;
import dtss.golemnews.utils.IOUtils;
import dtss.golemnews.utils.ImageUtil;

public class GolemScript implements IOUtils.ReadFileHandler, IGolemScriptHandler {



    public enum GolemScriptDataType {
        TITLE,
        URL,
        DESCRIPTION,
        HTML,
        CHAPTERS,
        IMAGES,
        VIDEO_LINKS,
        PREVIEW_IMAGE_LINK,
        DATE,
        PAGES
    }

    private String title = null;
    private String url = null;
    private String description = null;
    private String date = null;

    private String previewImageLink = null;
    private String html = null;
    private LinkedList<String> chapters = null;
    private LinkedList<GolemImage> images = new LinkedList<>();
    private LinkedList<String> videoLinks = null;

    private ArrayList<GolemScript> pages = null;
    private boolean pagesLoaded = false;

    private final Context context;

    private boolean cacheLoaded = false;

    public GolemScript(Context context, String url){
        this(context,url, null);
    }

    public GolemScript(Context context, String url, ArrayList<GolemScript> pages){
        this.context = context;

        if (url != null){
            setUrl(url);
        }
        this.pages = pages;
    }

    private void addPage(String url){
        GolemScript page = new GolemScript(context, url, pages);
        page.setDate(this.date);
        page.setDescription(this.description);
        page.setPreviewImageLink(this.previewImageLink);
        page.setTitle(this.title);
        pages.add(page);
    }


    public int getPageId(){
        if (pages != null) {
            for (int i=0; i<pages.size(); i++){
                String selfLink = url;
                String pageLink = (String) pages.get(i).request(GolemScriptDataType.URL);
                if (selfLink.equalsIgnoreCase(pageLink)){
                    return i;
                }
            }
        }
        return 0;
    }

    
    public void save(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("url", url);
            obj.put("title", title);
            obj.put("description", description);
            obj.put("previewImageUrl", previewImageLink);
            obj.put("html", html);
            obj.put("date", date);
            obj.put("chapters", new JSONArray(chapters));


            if (images != null){
                JSONArray imageArray = new JSONArray();
                for (GolemImage image : images) {
                    JSONObject ji = new JSONObject();
                    obj.put("link", image.getLink());
                    obj.put("author", image.getAuthor());
                    obj.put("description",image.getDescription());
                    imageArray.put(ji);
                }

                obj.put("images", imageArray);
            }

            obj.put("videoLinks", new JSONArray(videoLinks));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sha1 = IOUtils.getSha1(url);
        new IOUtils.WriteFileTask(context.getCacheDir(),sha1,obj.toString()).execute();
    }

    @Override
    public void dataReceived(GolemScriptDataType type, Object data) {
        switch (type){
            case URL:
                this.url = (String) data;
                break;
            case HTML:
                this.html = (String) data;
                break;
            case TITLE:
                this.title = (String) data;
                break;
            case DATE:
                this.date = (String) data;
                break;
            case CHAPTERS:
                this.chapters = (LinkedList<String>) data;
                break;
            case DESCRIPTION:
                this.description = (String) data;
                break;
            case IMAGES:
                this.images = (LinkedList<GolemImage>) data;
                break;
            case VIDEO_LINKS:
                this.videoLinks = (LinkedList<String>) data;
                break;
            case PREVIEW_IMAGE_LINK:
                this.previewImageLink = (String) data;
                break;

            default:
                break;
        }

        save();
    }

    @Override
    public void cacheLoaded(GolemScript sender) {
        cacheLoaded = true;
    }

    // Called when cache file read
    @Override
    public void onFinish(String content, boolean found) {
        if (!found) return;

        JSONObject obj = null;
        try {
            obj = new JSONObject(content);
            url = obj.getString("url");
            title = obj.getString("title");
            description = obj.getString("description");
            previewImageLink = obj.getString("previewImageUrl");

            try {
                html = obj.getString("html");
            } catch (JSONException ex) {
                // Miese Zeiten
            }


            try {
                chapters = new LinkedList<String>();
                JSONArray arr = obj.getJSONArray("chapters");
                for (int i = 0; i < arr.length(); i++){
                    chapters.add(arr.getString(i));
                }
            } catch (JSONException ex) {
                // Miese Zeiten
            }

            try {
                images = new LinkedList<GolemImage>();
                JSONArray arr = obj.getJSONArray("images");
                for (int i = 0; i < arr.length(); i++){
                    JSONObject imgObj = arr.getJSONObject(i);
                    Bitmap bm = ImageUtil.loadImage(imgObj.getString("link"));
                    images.add(new GolemImage(imgObj.getString("link"),imgObj.getString("author"),imgObj.getString("description"),bm));
                }
            } catch (JSONException ex) {
                // Miese Zeiten
            }

            try {
                videoLinks = new LinkedList<String>();
                JSONArray arr = obj.getJSONArray("videoLinks");
                for (int i = 0; i < arr.length(); i++){
                    videoLinks.add(arr.getString(i));
                }
            } catch (JSONException ex) {
                // Miese Zeiten
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.cacheLoaded(this);
    }


    public void requestAsync(final GolemScriptDataType type, IGolemScriptHandler handler) throws InterruptedException {

        final Object[] result = new Object[1];

        final Semaphore s = new Semaphore(1);
        s.acquire();
        Runnable asyncTask = new Runnable() {
            @Override
            public void run() {
                result[0] = request(type);
                s.release(1);
            }
        };

        new Thread(asyncTask).start();
        s.acquire();

        handler.dataReceived(type,result[0]);
    }

    public Object request(GolemScriptDataType type){
        switch(type){

            case VIDEO_LINKS:
                if (videoLinks == null) {
                    videoLinks = new LinkedList<>();
                    try {
                        Document d = Jsoup.parse((String) request(GolemScriptDataType.HTML));
                        Elements elements = d.getElementsByClass("gvideofig");
                        for (Element videoElement : elements){
                            try{
                                String id = videoElement.id().substring(7);
                                videoLinks.add("https://video.golem.de/amp-iframe.php?fileid=" + id);
                            } catch (Exception ex){
                                //Invalid video tag
                            }
                        }

                    }
                    catch (Exception ex)
                    {
                        return new LinkedList<String>();
                    }
                }
                return videoLinks;
            case IMAGES:
                if (this.images.size() == 0) {
                    try {
                        Document d = Jsoup.parse((String) request(GolemScriptDataType.HTML));

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

                    } catch (Exception ex) {
                        Log.d("ImageLoadException",ex.toString());
                        //Unknown
                    }

                }
                return this.images;

            case CHAPTERS:
                if (chapters == null || chapters.size() == 0){
                    try {
                        chapters = new LinkedList<>();
                        Document d = Jsoup.parse((String) request(GolemScriptDataType.HTML));
                        int part  = 1;
                        Element element = d.getElementById("gpar" + part);


                        while(element != null){
                            chapters.add(element.text());
                            part++;
                            element = d.getElementById("gpar" + part);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                return chapters;
            case HTML:
                if (html == null) {
                    try {
                        html = Jsoup.connect(AWSUtil.acceptCookieLink(url)).get().html();
                    } catch (IOException e) {
                        html = null;
                        return "Clould not load html.";
                    }
                }
                return html;

            case TITLE:
                return title;
            case DESCRIPTION:
                return description;
            case DATE:
                return date;
            case URL:
                return url;
            case PREVIEW_IMAGE_LINK:
                return previewImageLink;
            case PAGES:
                if (!pagesLoaded){
                    pages = new ArrayList<>();
                    pages.add(this);
                    try {
                        Document d = Jsoup.parse((String) request(GolemScriptDataType.HTML));

                        Element element = d.getElementById("list-jtoc");

                        if(element != null){

                            if (element.hasClass("list-pages")){
                                Elements listPages = element.getElementsByTag("a");

                                for(Element elem : listPages){
                                    if (elem.tagName().equalsIgnoreCase("a")){
                                        String link = "https://www.golem.de" + elem.attr("href");
                                        boolean exists = false;

                                        for (GolemScript existing : this.pages){
                                            if (((String) existing.request(GolemScriptDataType.URL)).equalsIgnoreCase(link)){
                                                exists = true;
                                                break;
                                            }
                                        }
                                        if (!exists){
                                            addPage(link);
                                        }

                                    }
                                }
                            }
                        }
                        pagesLoaded = true;
                        return pages;
                    } catch (Exception ex) {
                        pagesLoaded = true;
                        return pages;
                    }
                } else {
                    return pages;
                }

            default:
                return null;

        }
    }


    public String getText() {
        LinkedList<String> chapters = (LinkedList<String>)  request(GolemScriptDataType.CHAPTERS);
        StringBuilder text = new StringBuilder();
        for (String chapter : chapters){
            text.append(chapter).append("\n\n");
        }
        return text.toString();
    }

    public String getTitle(){
        return (String) request(GolemScriptDataType.TITLE);
    }

    public String getDescription(){
        return (String) request(GolemScriptDataType.DESCRIPTION);
    }

    public LinkedList<String> getLinks(){
        return (LinkedList<String>) request(GolemScriptDataType.VIDEO_LINKS);
    }

    public String getDate(){
        return (String) request(GolemScriptDataType.DATE);
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
        String sha1 = IOUtils.getSha1(url);
        new IOUtils.ReadTask(context.getCacheDir(),sha1,this).execute();
    }

    public void setDescription(String description) {
        String search = "<a href=";
        if (description.contains(search)){
            this.description = description.substring(0, description.indexOf(search)-1);
            this.description = this.description.trim();
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPreviewImageLink(String previewImageLink) {
        this.previewImageLink = previewImageLink;
    }

    public void waitForCache(final IGolemScriptHandler handler){

        final GolemScript script = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!cacheLoaded){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                handler.cacheLoaded(script);
            }
        }).start();


    }

}
