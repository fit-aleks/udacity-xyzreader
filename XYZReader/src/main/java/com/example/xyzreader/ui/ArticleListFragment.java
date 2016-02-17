package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.util.List;
import java.util.Map;

/**
 * Created by alexander on 15.02.16.
 */
public class ArticleListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View rootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ArticleListAdapter adapter;

    private Bundle reenterState;

    private void setSharedElementCallback() {
        ActivityCompat.setExitSharedElementCallback(getActivity(), new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (reenterState != null) {
                    final int currentPosition = reenterState.getInt(ArticleListActivity.EXTRA_CURRENT_POSITION);
                    final int startingPosition = reenterState.getInt(ArticleListActivity.EXTRA_STARTING_POSITION);
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
                    final View navigationBar = rootView.findViewById(android.R.id.navigationBarBackground);
                    final View statusBar = rootView.findViewById(android.R.id.statusBarBackground);
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
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArticleListFragment, 0, 0);
        boolean holdForTransition = a.getBoolean(R.styleable.ArticleListFragment_sharedElementTransitions, false);
        a.recycle();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_articles, container, false);
        setSharedElementCallback();


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        adapter = new ArticleListAdapter(getContext(), new ArticleListAdapter.ArticlesAdapterClickHandler() {
            @Override
            public void onClick(ArticleListAdapter.ViewHolder viewHolder) {
                final Intent intent = new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(adapter.getItemId(viewHolder.getAdapterPosition())));
                ActivityOptionsCompat activityOptions =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                viewHolder.thumbnailView,
                                ViewCompat.getTransitionName(viewHolder.thumbnailView));
                ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
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

        return rootView;
    }

    private void refresh() {
        getContext().startService(new Intent(getContext(), UpdaterService.class));
        mSwipeRefreshLayout.setRefreshing(true);
    }

    public void onActivityReenter(Intent data) {
        reenterState = new Bundle(data.getExtras());
        int currentPosition = reenterState.getInt(ArticleListActivity.EXTRA_CURRENT_POSITION);
        int startingPosition = reenterState.getInt(ArticleListActivity.EXTRA_STARTING_POSITION);
        if (startingPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        ActivityCompat.postponeEnterTransition(getActivity());
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                ActivityCompat.startPostponedEnterTransition(getActivity());
                return true;
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(getContext());
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
