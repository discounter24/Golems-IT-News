package dtss.golemnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Objects;

import dtss.golemnews.utils.VideoEnabledWebChromeClient;
import dtss.golemnews.utils.VideoEnabledWebView;


public class ArticleActivity extends AppCompatActivity implements IFeedArticleLoadHandler, SharedPreferences.OnSharedPreferenceChangeListener {


    private GolemFeedItem item;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        updateTheme(sharedPreferences);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null){
            item = MainActivity.feed.getItemByGuid((String) b.get("guid"));
        } else {
            finish();
        }


        item.getArticle(this);


        TextView articleTitle = findViewById(R.id.articleTitle);
        articleTitle.setText(item.getTitle());
        setTitle(item.getTitle());

        TextView articleDesc = findViewById(R.id.articleDescription);
        articleDesc.setText(item.getDescription());


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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void ArticleTextReceived(GolemFeedItem item, String text) {
        TextView articleText = findViewById(R.id.articleText);
        articleText.setText(text);
        ProgressBar bar = findViewById(R.id.progressBar);
        bar.setVisibility(View.GONE);
    }

    @Override
    public void ArticleImagesLoaded(GolemFeedItem item, LinkedList<GolemArticle.GolemImage> images) {
        if (!images.isEmpty()){
            GolemArticle.GolemImage previewImage = images.get(0);
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
    public void ArticleVideoFound(GolemFeedItem sender, String embedUrl) {
        //Toast t = Toast.makeText(getApplicationContext(),embedUrl,Toast.LENGTH_SHORT);
        //t.show();


        final LinearLayout layout = findViewById(R.id.mainLayout);

        TextView textView = new TextView(this);
        textView.setTextSize(17);
        textView.setPadding(5,0,5,2);
        textView.setText("Videos zum aktuellen Artikel:");
        layout.addView(textView);



        VideoEnabledWebView videoView = new VideoEnabledWebView(this);
        int height = (int)(getWindowManager().getDefaultDisplay().getWidth() * (9f/16));
        LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        webViewParams.bottomMargin = 20;

        final ViewGroup videoLayout = findViewById(R.id.videoLayout);
        final ViewGroup nonVideoLayout = findViewById(R.id.articleLayout);

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
                    getSupportActionBar().hide();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    getSupportActionBar().show();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
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
        videoView.loadUrl(embedUrl);


        layout.addView(videoView,webViewParams);



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
    }


    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("appThemePref")) {
            updateTheme(sharedPreferences);
        }
    }

    public void updateTheme(SharedPreferences sharedPreferences){
        String appTheme = sharedPreferences.getString("appThemePref", "system");
        //Toast.makeText(this,appTheme,Toast.LENGTH_LONG);
        switch (appTheme){
            case "system":
                break;
            case "dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "light":
                setTheme(R.style.AppTheme);
                break;
        }
    }
}
