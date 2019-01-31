package pascal.golemnews;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

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

        LinearLayout root = findViewById(R.id.rootLayout);

        Snackbar bar = Snackbar.make(root,title, Snackbar.LENGTH_LONG);
        bar.show();
    }

}
