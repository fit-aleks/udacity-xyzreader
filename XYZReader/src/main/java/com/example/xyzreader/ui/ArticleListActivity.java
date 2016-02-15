package com.example.xyzreader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

    private boolean dualPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if (!dualPane) {
            ArticleListFragment fragment = (ArticleListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_list);
            fragment.onActivityReenter(data);
        }

    }



}
