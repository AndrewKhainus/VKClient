package com.radomar.vkclient.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.radomar.vkclient.R;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Created by Radomar on 20.01.2016
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private Cursor mCursor;

    private static final int NEWS_VIEW = 1;
    private static final int PROGRESS_VIEW = 2;

    public CustomRecyclerAdapter(Context context, @Nullable Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder vh;
        if (viewType == NEWS_VIEW) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.news_card_view, parent, false);
            vh = new CustomViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.progress_card_view, parent, false);
            vh = new ProgressViewHolder(itemView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CustomViewHolder) {
            ((CustomViewHolder) holder).onBind(position);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 1 : mCursor.getCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mCursor == null) {
            return PROGRESS_VIEW;
        }
        if (position == mCursor.getCount()) {
            return PROGRESS_VIEW;
        }
        return NEWS_VIEW;
    }

    public void setCursor(Cursor mCursor) {
        this.mCursor = mCursor;
    }


    /**
     View Holder
     */
    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView mAuthorName;
        private TextView mUnixTime;
        private TextView mNewsText;

        private ImageView mAuthorPhoto;
        private ImageView mNewsPhoto;

        public CustomViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);
        }

        public void onBind(int position) {
            mCursor.moveToPosition(position);
            Log.d(getClass().getSimpleName(), "Position: " + position+". Cursor position = " + mCursor.getPosition());
//   Set text
            mNewsText.setText(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.NEWS_TEXT)));

//   Date from unix time
//   TODO change date style
            Date date = new Date(Long.parseLong(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.PUBLISH_TIME)))*1000);
            mUnixTime.setText(date.toString());

//   Download image
            String url = mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.IMAGE_URL));
            if (url != null) {
                Picasso.with(mContext)
                        .load(url)
                        .into(mNewsPhoto);
            }
//   user name and photo
            mAuthorName.setText(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.AUTHOR_NAME)));

            url = mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.PHOTO_URL));
            if (url != null) {
                Picasso.with(mContext)
                        .load(url)
                        .into(mAuthorPhoto);
            }

        }

        private void findViews(View view) {
            mNewsText = (TextView) view.findViewById(R.id.tvNewsText_NCV);
            mUnixTime = (TextView) view.findViewById(R.id.tvUnixTime_NCV);
            mAuthorName = (TextView) view.findViewById(R.id.tvAuthorName_NCV);

            mAuthorPhoto = (ImageView) view.findViewById(R.id.ivAuthorPhoto_NCV);
            mNewsPhoto = (ImageView) view.findViewById(R.id.ivNewsPhoto_NCV);
        }

    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar mProgressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);
        }

        private void findViews(View view) {
            mProgressBar = (ProgressBar) view.findViewById(R.id.pbProgress_PCV);
        }
    }
}