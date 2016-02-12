package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    static final String EXTRA_CURRENT_POSITION = "current_position";
    static final String EXTRA_STARTING_POSITION = "starting_position";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ArticleListAdapter adapter;

    private Bundle reenterState;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setSharedElementCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (reenterState != null) {
                    final int currentPosition = reenterState.getInt(EXTRA_CURRENT_POSITION);
                    final int startingPosition = reenterState.getInt(EXTRA_STARTING_POSITION);
                    if (startingPosition != currentPosition) {
                        // if startingPosition != currentPosition user swiped to different page
                        // in DetailsActivity. So it is needed to update the shared element
                        final String newTransitionName = getString(R.string.article_image_transition_name) + adapter.getItemId(currentPosition);
                        final View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);
                        if (newSharedElement != null) {
                            names.clear();
                            names.add(newTransitionName);
                            sharedElements.clear();
                            sharedElements.put(newTransitionName, newSharedElement);
                        }
                    }

                    reenterState = null;
                } else {
                    // current activity is exiting
                    final View navigationBar = findViewById(android.R.id.navigationBarBackground);
                    final View statusBar = findViewById(android.R.id.statusBarBackground);
                    if (navigationBar != null) {
                        names.add(ViewCompat.getTransitionName(navigationBar));
                        sharedElements.put(ViewCompat.getTransitionName(navigationBar), navigationBar);
                    }
                    if (statusBar != null) {
                        names.add(ViewCompat.getTransitionName(statusBar));
                        sharedElements.put(ViewCompat.getTransitionName(statusBar), statusBar);
                    }
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        setSharedElementCallback();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new ArticleListAdapter(this, new ArticleListAdapter.ArticlesAdapterClickHandler() {
            @Override
            public void onClick(ArticleListAdapter.ViewHolder viewHolder) {
                final Intent intent = new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(adapter.getItemId(viewHolder.getAdapterPosition())));
                ActivityOptionsCompat activityOptions =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(ArticleListActivity.this,
                                viewHolder.thumbnailView,
                                ViewCompat.getTransitionName(viewHolder.thumbnailView));

                startActivity(intent, activityOptions.toBundle());
            }
        });
        mRecyclerView.setAdapter(adapter);
        final int columnCount = getResources().getInteger(R.integer.list_column_count);
        final StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        reenterState = new Bundle(data.getExtras());
        int currentPosition = reenterState.getInt(EXTRA_CURRENT_POSITION);
        int startingPosition = reenterState.getInt(EXTRA_STARTING_POSITION);
        if (startingPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        supportPostponeEnterTransition();
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setAdapter(null);
    }

}
