package dtss.golemnews;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements IFeedLoadHandler {

    private CustomFeedAdapter adapter;
    public static GolemFeed feed;

    private ListView listView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setLogo(R.mipmap.ic_launcher_round);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feed.updateListItems();
            }
        });

        progressBar = findViewById((R.id.AnimationLoader));

        listView = findViewById(R.id.FeedView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                GolemFeedItem clicked = adapter.data[position];

                Bundle b = new Bundle();
                /*b.putString("title",clicked.getTitle());
                b.putString("description",clicked.getDescription());
                b.putString("article_link",clicked.getLink());
                b.putString("image_link",clicked.getPreviewImageLink());
                b.putString("pubDate",clicked.getPubDate().toString());
                */
                b.putString("guid",clicked.getGuid());
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        feed = new GolemFeed(this);


        adapter = new CustomFeedAdapter(this,  new GolemFeedItem[]{});
        listView.setAdapter(adapter);

        reload();
    }


    private void reload(){
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        feed.updateListItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reload:
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void FeedItemListLoaded(GolemFeedLoadTask.GolemFeedLoadTaskResult result, GolemFeed feed) {
        adapter.data = feed.getFeedItems().toArray(new GolemFeedItem[feed.getFeedItems().size()]);
        adapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void FeedPreviewImageLoaded(GolemFeedItem sender, Bitmap image) {
        adapter.notifyDataSetChanged();
    }

}
