package dtss.golemnews;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Predicate;

class GolemArticle{
    private final GolemFeedItem item;

    private boolean pagesSearchCompleted;

    public LinkedList<GolemArticlePage> pages;
    private String html;

    public GolemArticle(GolemFeedItem item) {
        this.item = item;
        this.pages = new LinkedList<>();
        this.pages.add(new GolemArticlePage(item.getLink()));
        this.html = "";
        this.pagesSearchCompleted = false;
    }



    public LinkedList<GolemArticlePage> getPages(){
        return pages;
    }

    public String getMainLink(){
        return item.getLink();
    }

    public String getHtml(){
        return html;
    }

    private void setHtml(String html){
        this.html = html;
    }

    public void loadAll(final ArticleFullyLoadedHandler handler){
        searchArticlePages(new IArticlePageFound() {

            @Override
            public void onArticlePageFound(GolemArticlePage page) {

            }

            @Override
            public void onPageSearchComplete() {

                waitFor = 3 * getPages().size();
                for (GolemArticlePage page : getPages()) {
                    IPageHandler handler = new IPageHandler() {
                        @Override
                        public void onTextReceived(GolemArticlePage sender, String text) {
                            loadComplete();
                        }

                        @Override
                        public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {
                            loadComplete();
                        }

                        @Override
                        public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {
                            loadComplete();
                        }
                    };

                    page.requestVideos(handler);
                    page.requestText(handler);
                    page.requestImages(handler);
                }

            }

            int waitFor;
            private void loadComplete(){
                waitFor--;
                if (waitFor == 0){
                    handler.onArticleLoaded();
                }
            }
        });
    }



    public void searchArticlePages(final IArticlePageFound pageFindHandler){
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

    public interface IArticlePageFound{
        void onArticlePageFound(GolemArticlePage page);
        void onPageSearchComplete();
    }




    private class ResolveArticlePagesTask extends AsyncTask<GolemArticle, Void, Void> {

        private final IArticlePageFound articlePageFound;
        private GolemArticle article;


        public ResolveArticlePagesTask(IArticlePageFound articleHandler){
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

                while(element != null){
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
                        break;
                    }
                }

            } catch (IOException ex) {

            } catch (Exception ex) {

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            articlePageFound.onPageSearchComplete();
        }
    }



}
