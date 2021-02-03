package dtss.golemnews;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class GolemArticle{
    private final GolemFeedItem item;

    private boolean pagesSearchCompleted;
    private boolean loading;
    private boolean fullyLoaded;

    private final LinkedList<GolemArticlePage> pages;
    private final LinkedList<ArticleFullyLoadedHandler> waitingForLoaded;

    private String html;

    public GolemArticle(GolemFeedItem item) {
        this.item = item;
        this.pages = new LinkedList<>();
        this.pages.add(new GolemArticlePage(item.getLink()));
        this.html = "";
        this.pagesSearchCompleted = false;
        this.fullyLoaded = false;
        this.loading = false;
        this.waitingForLoaded = new LinkedList<>();
    }



    public LinkedList<GolemArticlePage> getPages(){
        return pages;
    }

    private String getMainLink(){
        return item.getLink();
    }

    private String getHtml(){
        return html;
    }

    private void setHtml(String html){
        this.html = html;
    }

    public void loadAll(final ArticleFullyLoadedHandler handler){
        if (fullyLoaded){
            handler.onArticleLoaded();
        } else {
            waitingForLoaded.add(handler);
            if (!loading){
                loading = true;
                searchArticlePages(new IArticlePageFound() {

                    @Override
                    public void onArticlePageFound(GolemArticlePage page) {

                    }

                    @Override
                    public void onPageSearchComplete() {

                        waitFor = getPages().size();
                        for (GolemArticlePage page : getPages()) {
                            IPageHandler handler = new IPageHandler() {

                                int waitFor = 3;
                                @Override
                                public void onTextReceived(GolemArticlePage sender, String text) {
                                    loaded();
                                }

                                @Override
                                public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {
                                    loaded();
                                }

                                @Override
                                public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {
                                    loaded();
                                }

                                private void loaded(){
                                    waitFor--;
                                    if (waitFor==0){
                                        onArticleLoaded();
                                    }
                                }
                            };

                            page.requestVideos(handler);
                            page.requestText(handler);
                            page.requestImages(handler);
                        }

                    }

                    int waitFor;
                    private void onArticleLoaded(){
                        waitFor--;
                        if (waitFor == 0){
                            fullyLoaded = true;
                            loading = false;

                            for(ArticleFullyLoadedHandler h : waitingForLoaded){
                                h.onArticleLoaded();
                            }

                            DiskCache.getInstance().save(GolemArticle.this);

                        }
                    }
                });
            }


        }



    }

    private void searchArticlePages(final IArticlePageFound pageFindHandler){
        if (!pagesSearchCompleted){
            ResolveArticlePagesTask task = new ResolveArticlePagesTask(new IArticlePageFound() {
                @Override
                public void onArticlePageFound(GolemArticlePage page) {
                    pages.add(page);
                    pageFindHandler.onArticlePageFound(page);
                }

                @Override
                public void onPageSearchComplete() {
                    pagesSearchCompleted=true;
                    pageFindHandler.onPageSearchComplete();
                }
            });
            task.execute(this);
        } else {
            pageFindHandler.onPageSearchComplete();
        }
    }


    public interface ArticleFullyLoadedHandler{
        void onArticleLoaded();
    }

    interface IArticlePageFound{
        void onArticlePageFound(GolemArticlePage page);
        void onPageSearchComplete();
    }




    private class ResolveArticlePagesTask extends AsyncTask<GolemArticle, Void, Void> {

        private final IArticlePageFound articlePageFound;
        private GolemArticle article;


        ResolveArticlePagesTask(IArticlePageFound articleHandler){
            this.articlePageFound = articleHandler;
        }

        protected Void doInBackground(GolemArticle... articles) {
            this.article = articles[0];

            try {
                Document d;
                if (article.getHtml().isEmpty()){
                    //d = Jsoup.connect(article.getMainLink()).get();
                    //article.setHtml(d.html());


                    Map<String,String> cookies = new HashMap<>();
                    cookies.put("_sp_enable_dfp_personalized_ads","false");
                    cookies.put("_sp_v1_consent","1!1:1:-1:-1:-1:-1");
                    cookies.put("_sp_v1_csv","null");
                    cookies.put("_sp_v1_data","2:274572:1612386409:0:3:0:3:0:0:_:-1");
                    cookies.put("_sp_v1_lt","1:");
                    cookies.put("_sp_v1_opt","1:login|true:last_id|11:");
                    cookies.put("_sp_v1_ss","1:H4sIAAAAAAAAAItWqo5RKimOUbKKBjLyQAyD2lidGKVUEDOvNCcHyC4BK6iurVWKBQAW54XRMAAAAA%3D%3D");
                    cookies.put("_sp_v1_uid","1:449:622e9028-5093-4c08-90b4-4c9bd3ba2ad1");
                    cookies.put("a2a_consumerId","94c17a8e-cb0b-40bc-9f3f-906d7329db24");
                    cookies.put("golem_c20date","1612386411");
                    cookies.put("golem_consent20","cmp|210127");
                    cookies.put("golem_lp","wzijmip4xsag4n14n4q1jkxq57mk4qtg");
                    cookies.put("golem_referer","https%3A%2F%2Fwww.google.com%2F");
                    cookies.put("Golem_rngnaf","true");
                    cookies.put("golem_testcookie","1");
                    //cookies.put("golem_viewauto","desktop%3Afirefox_84_0");
                    cookies.put("ima_data_8caf03772d2e0ce26fd3cd41a1358210dca9a6a8","Vl%2BGhIYxw%2BVZRv5DGaTJ5w%3D%3DogHFGMCG0Zc4ZkIB0AsgNjqRWHO2oMH9LG46RbpU4azbYKvykFLDJLP5sHro3FpRHmhsMjHj%2B4JURrt1O8CV5inWAYqrFUe98X%2FXqIkr1L7BbHM7PwOfUKqxcubMEizt%2FNC2KRxW4NQCLeSwBMjC5NxL8EhujQ1PJeQwV6ZjbQ81EWPLUUMzz4zdw4bEgRjWrAFXUyN9Op8wmD5%2FB5LJjy3YkneGHYrCwXEpXjUbMqpgN1TC31nVxpAyu293sMDcB%2B7EME6IXoyiUsvTdMAX26%2FemKDbxjIUWDUM2RizMndpbHABQvHP90qAAkgfCZpW2Tr%2BfiKgsLCbB22hXKFf4uDMztlKdp%2FOoJvNZXLmeWyYgOj4QbtUMEKnQr%2BOOAE5ce1EV%2FqqANC%2F97MzluEAXw%3D%3D");
                    cookies.put("ima_data_checksum_8caf03772d2e0ce26fd3cd41a1358210dca9a6a8","124c67bb1119a019808b750b4330ed5892b63610");
                    cookies.put("xdefccpm","no");
                    cookies.put("xdefcc","G421224b019a87bd1c854e42f56d6ce4c3");


                    d = Jsoup.connect(article.getMainLink()).cookies(cookies).get();
                    article.setHtml(d.html());

                } else {
                    d = Jsoup.parse(article.getHtml());
                }


                Element element = d.getElementById("list-jtoc");

                if(element != null){

                    if (element.hasClass("list-pages")){
                        Elements listPages = element.getElementsByTag("a");

                        for(Element elem : listPages){
                            if (elem.tagName().equalsIgnoreCase("a")){
                                String link = "https://www.golem.de" + elem.attr("href");
                                boolean exists = false;
                                for (GolemArticlePage existing : article.getPages()){
                                    if (existing.getLink().equalsIgnoreCase(link)){
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists){
                                    GolemArticlePage page = new GolemArticlePage(link);
                                    articlePageFound.onArticlePageFound(page);
                                }

                            }
                        }
                    }
                }

            } catch (IOException ex) {
                return null;
            } catch (Exception ex) {
                return null;
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            articlePageFound.onPageSearchComplete();
        }
    }



}
