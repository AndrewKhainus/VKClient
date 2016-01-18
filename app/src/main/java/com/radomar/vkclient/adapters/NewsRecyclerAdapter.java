package com.radomar.vkclient.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.radomar.vkclient.R;
import com.radomar.vkclient.models.ItemModel;
import com.radomar.vkclient.models.NewsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radomar on 14.01.2016
 */
public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.CustomViewHolder> {

    private Context mContext;
    private List<ItemModel> mData;

    public NewsRecyclerAdapter(Context context, List<ItemModel> data) {
        mContext = context;
        this.mData = data;
    }

    public void setData(List<ItemModel> data) {
        mData = data;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.news_card_view, parent, false);
        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    /**
     View Holder
     */
    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView mNewsText;

        public CustomViewHolder(View itemView) {
            super(itemView);

            findViews(itemView);
        }

        public void onBind(int position) {
            ItemModel model = mData.get(position);

            mNewsText.setText(model.text);
        }

        private void findViews(View view) {
            mNewsText = (TextView) view.findViewById(R.id.tvNewsText_NCV);
        }

    }
}
