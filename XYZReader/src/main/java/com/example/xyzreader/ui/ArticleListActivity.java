package com.example.xyzreader.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements ArticleListFragment.Callback {
    static final String EXTRA_CURRENT_POSITION = "current_position";
    static final String EXTRA_STARTING_POSITION = "starting_position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        ArticleListFragment fragment = (ArticleListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_list);
        fragment.onActivityReenter(data);
    }

    @Override
    public void onItemSelected(long itemId, ArticleListAdapter.ViewHolder viewHolder) {
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                ItemsContract.Items.buildItemUri(itemId));
        ActivityOptionsCompat activityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        viewHolder.thumbnailView,
                        ViewCompat.getTransitionName(viewHolder.thumbnailView));
        ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
    }
}
