package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.widget.DrawInsetsFrameLayout;
import com.example.xyzreader.utils.ColorUtils;
import com.example.xyzreader.utils.GlideUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String DETAIL_TRANSITION_ANIMATION = "transition_animation";
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private boolean transitionAnimation;

    private View mRootView;
    @Bind(R.id.photo) ImageView mPhotoView;
    @Bind(R.id.article_body) TextView bodyView;
    @Bind(R.id.article_subtitle) TextView subtitle;
    @Bind(R.id.toolbar) Toolbar toolbar;
    private TextView titleView;
    private CollapsingToolbarLayout collapsingToolbar;
//    private DrawInsetsFrameLayout drawInsetsFrameLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, boolean transitionAnimation) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();

        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putBoolean(DETAIL_TRANSITION_ANIMATION, transitionAnimation);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mItemId = arguments.getLong(ARG_ITEM_ID, 0);
            transitionAnimation = getArguments().getBoolean(DETAIL_TRANSITION_ANIMATION, false);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);
        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        collapsingToolbar = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        Timber.tag(ArticleDetailFragment.class.getSimpleName());

        ViewCompat.setTransitionName(mPhotoView, getString(R.string.article_image_transition_name) + mItemId);
        bodyView.setMovementMethod(new LinkMovementMethod());
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        return mRootView;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.share_fab)
    public void shareFabPressed() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            } else {
                titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            }


            subtitle.setText(getString(R.string.details_subtitle,
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL),
                    mCursor.getString(ArticleLoader.Query.AUTHOR)));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
            Glide.with(this)
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .priority(Priority.IMMEDIATE)
                    .listener(photoLoadListener)
                    .into(mPhotoView);
            if (transitionAnimation) {
                startPostponedEnterTransition();
            }
        }
    }

    private void startPostponedEnterTransition() {
        if (transitionAnimation) {
            mPhotoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                    ((AppCompatActivity) getActivity()).supportStartPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Timber.e("Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();

        final AppCompatActivity activity = (AppCompatActivity)getActivity();
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            final ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on the screen.
     */
    @Nullable
    ImageView getPhotoImage() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mPhotoView)) {
            return mPhotoView;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }

    private RequestListener<String, GlideDrawable> photoLoadListener = new RequestListener<String, GlideDrawable>() {

        @Override
        public boolean onResourceReady(final GlideDrawable resource, String model,
                                       Target<GlideDrawable> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            if (bitmap == null) {
                return false;
            }
            Palette.from(bitmap)
                    .clearFilters()/* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                return;
                            }
                            final Palette.Swatch mostPopular = ColorUtils.getMostPopulousSwatch(palette);
                            if (collapsingToolbar != null) {
                                // phone version
                                final int lightness = ColorUtils.isDark(palette);
                                final boolean isDark = lightness == ColorUtils.IS_DARK;

                                int statusBarColor = getActivity().getWindow().getStatusBarColor();

                                if (mostPopular != null &&
                                        (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                    collapsingToolbar.setContentScrimColor(mostPopular.getRgb());
                                    // TODO: make status bar light on android 6.0
                                    statusBarColor = ColorUtils.scrimify(mostPopular.getRgb(), isDark, SCRIM_ADJUSTMENT);
                                }
                                if (statusBarColor != getActivity().getWindow().getStatusBarColor()) {
                                    collapsingToolbar.setStatusBarScrimColor(statusBarColor);
                                }
                            } else {
                                if (mostPopular != null) {
                                    final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(mostPopular.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
                                    colorizeBackButton(colorFilter);
                                    getActivity().getWindow().setStatusBarColor(mostPopular.getRgb());
                                }

                            }

                        }
                    });
            return false;
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

    };

//    private void updateStatusBar() {
//        if (collapsingToolbar != null) {
//            return;
//        }
//        drawInsetsFrameLayout.setInsetBackground(new ColorDrawable(0));
//    }

    private void colorizeBackButton(final PorterDuffColorFilter colorFilter) {
        for (int i =0; i< toolbar.getChildCount(); ++i) {
            final View view = toolbar.getChildAt(i);
            if (view instanceof ImageButton) {
                ((ImageButton) view).getDrawable().setColorFilter(colorFilter);
                break;
            }
        }
    }
}
