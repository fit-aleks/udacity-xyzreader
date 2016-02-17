package com.example.xyzreader.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.xyzreader.R;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity  {
    static final String EXTRA_CURRENT_POSITION = "current_position";
    static final String EXTRA_STARTING_POSITION = "starting_position";

    private static final String DETAIL_FRAGMENT_TAG = "detail_tag";

    private boolean twoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.article_detail_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                ArticleDetailFragment detailFragment = new ArticleDetailFragment();
                if (contentUri != null) {
                    Bundle args = new Bundle();
                    args.putBoolean(ArticleDetailFragment.DETAIL_TRANSITION_ANIMATION, false);
                    detailFragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.article_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (!twoPane) {
            ArticleListFragment fragment = (ArticleListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_list);
            fragment.onActivityReenter(data);
        }

    }



}
