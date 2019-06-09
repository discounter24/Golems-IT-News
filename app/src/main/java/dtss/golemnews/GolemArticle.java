package dtss.golemnews;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;

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
                    d = Jsoup.connect(article.getMainLink()).get();
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
