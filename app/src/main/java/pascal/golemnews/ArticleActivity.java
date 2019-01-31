package pascal.golemnews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArticleActivity extends AppCompatActivity {


    private String title;
    private String description;
    private String article_link;
    private String image_link;
    private String pubDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        if (b != null){
            title = (String) b.get("title");
            description = (String) b.get("description");
            article_link = (String) b.get("article_link");
            image_link = (String) b.get("image_link");
            pubDate = (String) b.get("pubDate");
        }

        LinearLayout root = findViewById(R.id.mainLayout);

        //Snackbar bar = Snackbar.make(root,article_link, Snackbar.LENGTH_LONG);
        //bar.show();

        GolemFeedItem item = new GolemFeedItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setLink(article_link);
        item.setImageLink(image_link);
        item.setPubDate(pubDate);

        GolemArticle article = new GolemArticle(item);


        TextView articleTitle = findViewById(R.id.articleTitle);
        articleTitle.setText(title);

        TextView articleDesc = findViewById(R.id.articleDescription);
        articleDesc.setText(description);


        article.receive(ArticleReceivedHandler);
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article_link));
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private Handler ArticleReceivedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TextView articleText = findViewById(R.id.articleText);
                    articleText.setText((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


}
