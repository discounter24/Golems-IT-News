package pascal.golemnews;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private GolemFeedAdapter adapter;
    private GolemFeed feed;

    private ListView listView;


    private Handler GolemFeedUpdate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GolemFeedLoadTaskResult result = (GolemFeedLoadTaskResult) msg.obj;
                    GolemFeedItem[] values = (GolemFeedItem[]) result.FeedItems.toArray(new GolemFeedItem[result.FeedItems.size()]);
                    adapter.data = values;
                    adapter.notifyDataSetChanged();
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher_round);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        listView = (ListView) findViewById(R.id.FeedView);
        feed = new GolemFeed(GolemFeedUpdate);

        adapter = new GolemFeedAdapter(this,  new GolemFeedItem[]{});
        listView.setAdapter(adapter);
        feed.Load();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.reload:
                feed.Load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
