package dtss.golemnews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

import dtss.golemnews.utils.ThemeUtils;

public class MainActivity extends AppCompatActivity implements IFeedLoadHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    public final boolean PRELOAD_BACKGROUND = true;

    private CustomFeedAdapter adapter;
    public static GolemFeed feed;

    private ListView listView;

    private ProgressBar progressBar;
    private ProgressBar progressBarArticle;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView loadingStateRSS;
    private TextView loadingStateArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        if (ThemeUtils.sharedPreferences == null){
            ThemeUtils.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        ThemeUtils.updateTheme(this);
        ThemeUtils.sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DiskCache.openCache(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setLogo(R.mipmap.ic_launcher_round);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });

        progressBar = findViewById(R.id.AnimationLoader);
        loadingStateRSS = findViewById(R.id.loadingStateRSS);

        loadingStateArticle = findViewById(R.id.loadingStateArticle);
        progressBarArticle = findViewById(R.id.articlePreloadProgress);

        loadingStateArticle.setVisibility(View.GONE);
        progressBarArticle.setVisibility(View.GONE);

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

                Objects.requireNonNull(touchedView).callOnClick();

            }

            @Override
            void onSwipeLeft(MotionEvent e1, MotionEvent e2) {

            }

            @Override
            void onSwipeTop(MotionEvent e1, MotionEvent e2) {

            }

            @Override
            void onSwipeBottom(MotionEvent e1, MotionEvent e2) {

            }

        });
        listView.setAdapter(adapter);


        reload();
    }




    @Override
    public void onResume() {
        //ThemeUtils.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        //ThemeUtils.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void reload(){
        listView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        loadingStateRSS.setVisibility(View.VISIBLE);

        loadingStateRSS.setText(R.string.loadRSS);

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
    public void FeedItemListLoaded(GolemFeedLoadTask.GolemFeedLoadTaskResult result, final GolemFeed feed) {
        DiskCache.getInstance().clearCache(feed);
        //DiskCache.getInstance().clearCache();

        adapter.data = feed.getFeedItems().toArray(new GolemFeedItem[0]);
        adapter.notifyDataSetChanged();

        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        loadingStateRSS.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);


        try{
            String preloadSetting = ThemeUtils.sharedPreferences.getString("appPreloadPref","wifi");

            if ((preloadSetting.equalsIgnoreCase("wifi") && checkWifiOnAndConnected()) || preloadSetting.equalsIgnoreCase("always")){
                preload();
            } else {
                loadingStateArticle.setVisibility(View.GONE);
                progressBarArticle.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Log.d("MAIN_ACTIVITY",ex.toString());
        }
    }



    private void preload(){

            final ProgressBar bar = findViewById(R.id.articlePreloadProgress);
            final TextView loadingStateArtikel = findViewById(R.id.loadingStateArticle);


            GolemArticle.ArticleFullyLoadedHandler loadHandler = new GolemArticle.ArticleFullyLoadedHandler() {
                int loadedArticles = 0;

                @Override
                public void onArticleLoaded() {
                    loadedArticles++;

                    try{
                        if (android.os.Build.VERSION.SDK_INT >= 24) {
                            bar.setMax(feed.getFeedItems().size());
                            bar.setProgress(loadedArticles,true);
                        } else {
                            bar.setMax(feed.getFeedItems().size());
                            bar.setProgress(loadedArticles);
                        }
                    } catch (Exception ex){
                        Log.d("ProgressBar",ex.toString());
                    }


                    loadingStateArtikel.setText(String.format(getResources().getString(R.string.loadArticles),loadedArticles,feed.getFeedItems().size()));
                    if (loadedArticles == feed.getFeedItems().size()){
                        bar.setVisibility(View.GONE);
                        loadingStateArtikel.setVisibility(View.GONE);
                    } else {
                        try{
                            feed.getFeedItems().get(loadedArticles).getArticle().loadAll(this);
                        } catch (Exception ex){
                            Log.d("Preload","Cant't load next article.\n" + ex.toString());
                        }

                    }
                }
            };
            bar.setMax(feed.getFeedItems().size());

            if (!PRELOAD_BACKGROUND){
                bar.setVisibility(View.VISIBLE);
                loadingStateArtikel.setVisibility(View.VISIBLE);
            }


            loadingStateArtikel.setText(String.format(getResources().getString(R.string.loadArticles),0,feed.getFeedItems().size()));
            feed.getFeedItems().get(0).getArticle().loadAll(loadHandler);



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



    private boolean checkWifiOnAndConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnectedOrConnecting() &&  activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception ex) {
            return false;
        }
    }

}
