package pascal.golemnews;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
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

public class MainActivity extends AppCompatActivity {

    private GolemFeedAdapter adapter;
    private GolemFeed feed;

    private ListView listView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler GolemFeedUpdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GolemFeedLoadTaskResult result = (GolemFeedLoadTaskResult) msg.obj;
                    GolemFeedItem[] values = (GolemFeedItem[]) result.FeedItems.toArray(new GolemFeedItem[result.FeedItems.size()]);
                    adapter.data = values;
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher_round);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feed.Load();
            }
        });

        progressBar = (ProgressBar) findViewById((R.id.AnimationLoader));

        listView = (ListView) findViewById(R.id.FeedView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                //startActivity(intent);
            }
        });

        feed = new GolemFeed(GolemFeedUpdate);

        adapter = new GolemFeedAdapter(this,  new GolemFeedItem[]{});
        listView.setAdapter(adapter);

        reload();
    }


    public void reload(){
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        feed.Load();
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


}
