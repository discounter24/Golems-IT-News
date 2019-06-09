package dtss.golemnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Objects;

import dtss.golemnews.utils.ThemeUtils;
import dtss.golemnews.utils.VideoEnabledWebChromeClient;
import dtss.golemnews.utils.VideoEnabledWebView;


public class ArticleActivity extends AppCompatActivity implements IPageHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView articleDesc;
    private TextView articleTitle;
    private TextView articleText;


    private GolemFeedItem item;
    private int currentPageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtils.updateTheme(this);
        ThemeUtils.sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);


        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null){
            item = MainActivity.feed.getItemByGuid((String) b.get("guid"));
        } else {
            finish();
        }

        articleDesc = findViewById(R.id.articleDescription);
        articleTitle = findViewById(R.id.articleTitle);
        articleText = findViewById(R.id.articleText);

        articleTitle.setVisibility(View.GONE);
        articleDesc.setVisibility(View.GONE);
        articleText.setVisibility(View.GONE);


        TextView next = findViewById(R.id.bNext);
        TextView previous = findViewById(R.id.bPrevious);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousPage();
            }
        });



        item.getArticle().loadAll(new GolemArticle.ArticleFullyLoadedHandler() {
            @Override
            public void onArticleLoaded() {
                loadPage(0);
            }
        });
    }


    private void updateSiteNavigator(){
        RelativeLayout siteNavigator = findViewById(R.id.siteNavigator);

        TextView next = findViewById(R.id.bNext);
        TextView previous = findViewById(R.id.bPrevious);
        TextView currentPageView = findViewById(R.id.tvCurrentPage);

        if (hasPreviousPage() || hasNextPage()){
            String site = "Seite " + (this.currentPageIndex + 1) + "/" + item.getArticle().getPages().size();
            currentPageView.setText(site);
            currentPageView.setVisibility(View.VISIBLE);
            siteNavigator.setVisibility(View.VISIBLE);
        } else {
            siteNavigator.setVisibility(View.GONE);
        }

        if (hasNextPage()){
            next.setVisibility(View.VISIBLE);
        } else {
            next.setVisibility(View.INVISIBLE);
        }

        if (hasPreviousPage()){
            previous.setVisibility(View.VISIBLE);
        } else {
            previous.setVisibility(View.INVISIBLE);
        }


    }

    private void loadPage(int pageID){
        ProgressBar bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

        currentPageIndex = pageID;

        articleTitle.setVisibility(View.GONE);
        articleDesc.setVisibility(View.GONE);
        articleText.setVisibility(View.GONE);

        ScrollView scrollView = findViewById(R.id.articleLayout);
        scrollView.fullScroll(ScrollView.FOCUS_UP);

        if (pageID == 0){
            articleTitle.setText(item.getTitle());
            setTitle(item.getTitle());
            articleDesc.setText(item.getDescription());

            articleTitle.setVisibility(View.VISIBLE);
            articleDesc.setVisibility(View.VISIBLE);

            item.getArticle().getPages().get(pageID).requestImages(this);
        }


        item.getArticle().getPages().get(pageID).requestText(this);
        item.getArticle().getPages().get(pageID).requestVideos(this);


        updateSiteNavigator();
    }

    private boolean hasNextPage(){
        return currentPageIndex < item.getArticle().getPages().size()-1;
    }

    private boolean hasPreviousPage(){
        return currentPageIndex > 0;
    }

    private void nextPage(){
        if (hasNextPage()){
            loadPage(currentPageIndex +1);
        }
    }

    private void previousPage(){
        if (hasPreviousPage()){
            loadPage(currentPageIndex -1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.link:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.item.getLink()));
                startActivity(browserIntent);
                break;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBodyText = "Sieh dir mal diesen Artikel auf Golem.de an: " + this.item.getLink();
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTextReceived(GolemArticlePage sender, String text) {
        articleText.setVisibility(View.VISIBLE);
        articleText.setText(text);
        ProgressBar bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);
    }

    @Override
    public void onImagesReceived(GolemArticlePage sender, LinkedList<GolemImage> images) {
        if (!images.isEmpty()){
            GolemImage previewImage = images.get(0);
            ImageView articlePictureView = findViewById(R.id.previewImage);
            articlePictureView.setImageBitmap(previewImage.getImage());
            articlePictureView.setVisibility(View.VISIBLE);
            TextView subtitle = findViewById(R.id.previewImageSubtitle);
            subtitle.setTextSize(12);
            subtitle.setText(previewImage.getDescription() + " " + previewImage.getAuthor());
            subtitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVideosReceived(GolemArticlePage sender, LinkedList<String> videos) {
        final LinearLayout layout = findViewById(R.id.videoList);
        final ViewGroup videoLayout = findViewById(R.id.videoLayout);
        final ViewGroup nonVideoLayout = findViewById(R.id.articleLayout);

        layout.removeAllViews();

        if (videos.isEmpty()) return;

        TextView textView = new TextView(this);
        textView.setTextSize(17);
        textView.setPadding(5,0,5,2);
        textView.setText("Videos zum aktuellen Artikel:");


        layout.addView(textView);


        for(String video : videos){

            VideoEnabledWebView videoView = new VideoEnabledWebView(this);
            int height = (int)(getWindowManager().getDefaultDisplay().getWidth() * (9f/16));
            LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            webViewParams.bottomMargin = 20;


            View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

            final VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout,videoLayout,loadingView,videoView);

            webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
            {
                @Override
                public void toggledFullscreen(boolean fullscreen)
                {
                    // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                    if (fullscreen)
                    {
                        Objects.requireNonNull(getSupportActionBar()).hide();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

                        WindowManager.LayoutParams attrs = getWindow().getAttributes();
                        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        getWindow().setAttributes(attrs);
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                    else
                    {
                        Objects.requireNonNull(getSupportActionBar()).show();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        WindowManager.LayoutParams attrs = getWindow().getAttributes();
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        getWindow().setAttributes(attrs);
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }

                }
            });

            videoView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View arg0, MotionEvent arg1) {
                    return false;
                }
            });

            videoView.setWebChromeClient(webChromeClient);
            videoView.setWebViewClient(new InsideWebViewClient());

            videoView.getSettings().setJavaScriptEnabled(true);
            videoView.loadUrl(video);


            layout.addView(videoView,webViewParams);

        }

    }


    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (ThemeUtils.isSystemControlled()){
            recreate();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("appThemePref")) {
            recreate();
        }
    }
}
