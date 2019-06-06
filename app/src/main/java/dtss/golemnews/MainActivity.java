package dtss.golemnews;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import dtss.golemnews.utils.ThemeUtils;

public class MainActivity extends AppCompatActivity implements IFeedLoadHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private CustomFeedAdapter adapter;
    public static GolemFeed feed;

    private ListView listView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ThemeUtils.sharedPreferences == null){
            ThemeUtils.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        ThemeUtils.updateTheme(this);
        ThemeUtils.sharedPreferences.registerOnSharedPreferenceChangeListener(this);


        ThemeUtils.updateTheme(this);
        ThemeUtils.sharedPreferences.registerOnSharedPreferenceChangeListener(this);


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
                b.putString("guid",clicked.getGuid());
                intent.putExtras(b);
                startActivity(intent);
            }
        });


        feed = new GolemFeed(this);


        adapter = new CustomFeedAdapter(this,  new GolemFeedItem[]{},new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeRight(MotionEvent e1, MotionEvent e2) {


                Rect rect = new Rect();
                int childCount = listView.getChildCount();
                int[] listViewCoords = new int[2];
                listView.getLocationOnScreen(listViewCoords);
                int x = (int) e1.getRawX() - listViewCoords[0];
                int y = (int) e1.getRawY() - listViewCoords[1];
                View touchedView = null;
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = listView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        touchedView = child; // This is your down view
                        break;
                    }
                }

                touchedView.callOnClick();

            }

        });
        listView.setAdapter(adapter);


        reload();
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

    private void reload(){
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        feed.updateListItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.Reload:
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (ThemeUtils.isSystemControlled()){
            recreate();
        }
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


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("appThemePref")) {
            recreate();
        }
    }

}
