package com.radomar.vkclient.adapters;

import android.app.Activity;
import android.content.Intent;
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

import com.radomar.vkclient.GoogleMapActivity;
import com.radomar.vkclient.R;
import com.radomar.vkclient.content_provider.NewsContentProvider;
import com.radomar.vkclient.global.Constants;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * Created by Radomar on 20.01.2016
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Cursor mCursor;

    private static final int NEWS_VIEW = 1;
    private static final int PROGRESS_VIEW = 2;

    public CustomRecyclerAdapter(Activity activity, @Nullable Cursor cursor) {
        mActivity = activity;
        mCursor = cursor;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder vh;
        if (viewType == NEWS_VIEW) {
            itemView = LayoutInflater.from(mActivity).inflate(R.layout.news_card_view, parent, false);
            vh = new CustomViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(mActivity).inflate(R.layout.progress_card_view, parent, false);
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
    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mAuthorName;
        private TextView mUnixTime;
        private TextView mNewsText;

        private ImageView mAuthorPhoto;
        private ImageView mNewsPhoto;

        String mLatitude;
        String mLongitude;


        public CustomViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);
            itemView.setOnClickListener(this);
        }

        public void onBind(int position) {
            mCursor.moveToPosition(position);
//   Set text
            mNewsText.setText(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.NEWS_TEXT)));

//   Date from unix time
            Date date = new Date(Long.parseLong(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.PUBLISH_TIME)))*1000);
            mUnixTime.setText(date.toString());

//   Download image
            String url = mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.IMAGE_URL));
            if (url != null) {
                Picasso.with(mActivity)
                        .load(url)
                        .into(mNewsPhoto);
            }
//   user name and photo
            mAuthorName.setText(mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.AUTHOR_NAME)));

            url = mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.PHOTO_URL));
            if (url != null) {
                Picasso.with(mActivity)
                        .load(url)
                        .into(mAuthorPhoto);
            }
//  Map
            mLatitude =  mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.LATITUDE));
            mLongitude =  mCursor.getString(mCursor.getColumnIndex(NewsContentProvider.LONGITUDE));
        }

        private void findViews(View view) {
            mNewsText = (TextView) view.findViewById(R.id.tvNewsText_NCV);
            mUnixTime = (TextView) view.findViewById(R.id.tvUnixTime_NCV);
            mAuthorName = (TextView) view.findViewById(R.id.tvAuthorName_NCV);

            mAuthorPhoto = (ImageView) view.findViewById(R.id.ivAuthorPhoto_NCV);
            mNewsPhoto = (ImageView) view.findViewById(R.id.ivNewsPhoto_NCV);
        }

        @Override
        public void onClick(View v) {
            Log.d("sometag", "onClick");

            if (mLatitude != null && mLongitude != null) {
                Log.d("sometag", "start map activity");

                Intent i = new Intent(mActivity, GoogleMapActivity.class);
                i.putExtra(Constants.KEY_LATITUDE, mLatitude)
                        .putExtra(Constants.KEY_LONGITUDE, mLongitude);

                mActivity.startActivity(i);
            }
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