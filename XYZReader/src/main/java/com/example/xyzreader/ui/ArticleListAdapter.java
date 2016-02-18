package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.widget.DynamicHeightNetworkImageView;
import com.example.xyzreader.utils.GlideUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by alexander on 09.02.16.
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {

    private Context context;
    private ArticlesAdapterClickHandler clickHandler;
    private Cursor cursor;

    public ArticleListAdapter(Context context, ArticlesAdapterClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(view);
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
        holder.subtitleView.setText(context.getString(R.string.details_subtitle,
                DateUtils.getRelativeTimeSpanString(
                        cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL),
                cursor.getString(ArticleLoader.Query.AUTHOR)));

        Glide.with(holder.thumbnailView.getContext())
                .load(cursor.getString(ArticleLoader.Query.THUMB_URL))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        final Bitmap bitmap = GlideUtils.getBitmap(resource);
                        if (bitmap == null) {
                            return false;
                        }
                        Palette.from(bitmap)
                                .clearFilters()
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        holder.applyPalette(palette.getVibrantSwatch());
                                    }
                                });
                        return false;
                    }
                })
                .into(holder.thumbnailView);
        holder.thumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        holder.thumbnailView.setTag(context.getString(R.string.article_image_transition_name) + getItemId(position));
        ViewCompat.setTransitionName(holder.thumbnailView, context.getString(R.string.article_image_transition_name) + getItemId(position));

    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.article_bkg) LinearLayout containerView;
        @Bind(R.id.thumbnail) DynamicHeightNetworkImageView thumbnailView;
        @Bind(R.id.article_title) TextView titleView;
        @Bind(R.id.article_subtitle) TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            final int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            clickHandler.onClick(this);
        }

        public void applyPalette(Palette.Swatch paletteSwatch) {
            if (paletteSwatch != null) {
                containerView.setBackgroundColor(paletteSwatch.getRgb());
                titleView.setTextColor(paletteSwatch.getTitleTextColor());
                subtitleView.setTextColor(paletteSwatch.getBodyTextColor());
            }
        }
    }

    public interface ArticlesAdapterClickHandler {
        void onClick(ViewHolder viewHolder);
    }

}
