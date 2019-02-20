package dtss.golemnews;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;


public class ArticleActivity extends AppCompatActivity implements IFeedArticleLoadHandler {


    private GolemFeedItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null){
            item = MainActivity.feed.getItemByGuid((String) b.get("guid"));
        }

        LinearLayout root = findViewById(R.id.mainLayout);


        item.getArticle(this);

        //article = new GolemArticle(item,this);


        TextView articleTitle = findViewById(R.id.articleTitle);
        articleTitle.setText(item.getTitle());

        TextView articleDesc = findViewById(R.id.articleDescription);
        articleDesc.setText(item.getDescription());


        //article.get();
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
        bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void ArticleImageLoaded(GolemFeedItem item, Bitmap image) {
        if (image!=null){
            ImageView articlePictureView = findViewById(R.id.previewImage);
            articlePictureView.setImageBitmap(image);
            articlePictureView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void ArticleVideoFound(GolemFeedItem sender, String embedUrl) {
        Toast t = Toast.makeText(getApplicationContext(),embedUrl,Toast.LENGTH_SHORT);
        t.show();

        final LinearLayout layout = findViewById(R.id.mainLayout);



        final WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        final LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800);

        web.loadUrl(embedUrl);
        web.getSettings().setJavaScriptEnabled(true);
        //web.setInitialScale(1);
        //web.getSettings().setUseWideViewPort(true);
        //web.getSettings().setLoadWithOverviewMode(true);
        layout.addView(web,webViewParams);

        web.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                return false;
            }
        });
        //web.setLayoutParams(webViewParams);

    }
}
