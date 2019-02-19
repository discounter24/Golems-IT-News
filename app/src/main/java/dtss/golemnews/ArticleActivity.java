package dtss.golemnews;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ArticleActivity extends AppCompatActivity implements IFeedArticleLoadHandler {


    private GolemFeedItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null){
            item = MainActivity.feed.getItemByGuid((String) b.get("guid"));
        }

        LinearLayout root = (LinearLayout) findViewById(R.id.mainLayout);


        item.getArticle(this);

        //article = new GolemArticle(item,this);


        TextView articleTitle = (TextView) findViewById(R.id.articleTitle);
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
}
