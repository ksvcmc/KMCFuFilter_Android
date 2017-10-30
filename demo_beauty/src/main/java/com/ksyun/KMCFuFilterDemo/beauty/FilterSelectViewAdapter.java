package com.ksyun.KMCFuFilterDemo.beauty;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sujia on 2017/8/21.
 */

public class FilterSelectViewAdapter extends RecyclerView.Adapter {
    private final static String TAG = FilterSelectViewAdapter.class.getSimpleName();

    public static final int[] FILTER_ITEM_RES_ARRAY = {
            R.mipmap.nature, R.mipmap.delta, R.mipmap.electric, R.mipmap.slowlived, R.mipmap.tokyo, R.mipmap.warm
    };
    private final static String[] FILTERS_NAME = {"nature", "delta", "electric", "slowlived", "tokyo", "warm"};

    private RecyclerView mOwnerRecyclerView;

    private ArrayList<Boolean> mItemsClickStateList;
    private int mLastClickPosition = -1;
    private OnItemSelectedListener mOnItemSelectedListener;

    private Context mContext;

    public FilterSelectViewAdapter(RecyclerView recyclerView, Context context) {
        mContext = context.getApplicationContext();
        mOwnerRecyclerView = recyclerView;

        mItemsClickStateList = new ArrayList<>();
        initItemsClickState();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_list, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        return new ItemViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemViewHolder holder = (ItemViewHolder) viewHolder;
        if (mItemsClickStateList.get(position) == null || !mItemsClickStateList.get(position)) {
            holder.itemView.setSelected(false);
            holder.setSelected(false);
        } else {
            holder.itemView.setSelected(true);
            holder.setSelected(true);
        }

        holder.mItemThumb.setImageResource(FILTER_ITEM_RES_ARRAY[position % FILTER_ITEM_RES_ARRAY.length]);
        holder.mItemName.setText(FILTERS_NAME[position % FILTER_ITEM_RES_ARRAY.length].toUpperCase());

        holder.mItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastClickPosition != -1 &&
                        mLastClickPosition != position) {
                    ItemViewHolder lastItemViewHolder = (ItemViewHolder)
                            mOwnerRecyclerView.findViewHolderForAdapterPosition(mLastClickPosition);
                    if (lastItemViewHolder != null) {
                        lastItemViewHolder.setSelected(false);
                    }
                    mItemsClickStateList.set(mLastClickPosition, false);
                }
                holder.setSelected(true);
                setClickPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return FILTER_ITEM_RES_ARRAY.length;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final WeakReference<FilterSelectViewAdapter> adpter;
        private LinearLayout mItemLayout;
        private ImageView mItemThumb;
        private TextView mItemName;
        private int position;

        public ItemViewHolder(View itemView, FilterSelectViewAdapter adapter) {
            super(itemView);
            this.adpter = new WeakReference<FilterSelectViewAdapter>(adapter);
            this.mItemLayout = (LinearLayout) itemView;
            this.mItemThumb = (ImageView) itemView.findViewById(R.id.item_thumb);
            this.mItemName = (TextView) itemView.findViewById(R.id.item_name);
        }

        public void setSelected(boolean select) {
            if (select) {
                mItemThumb.setBackgroundResource(R.drawable.item_chosen);
                mItemName.setTextColor(adpter.get().mContext.getResources().getColor(R.color.white));
            } else {
                mItemThumb.setBackgroundColor(Color.TRANSPARENT);
                mItemName.setTextColor(adpter.get().mContext.getResources().getColor(R.color.grey));
            }
        }
    }

    private void initItemsClickState() {
        if (mItemsClickStateList == null) {
            return;
        }
        mItemsClickStateList.clear();

        mItemsClickStateList.addAll(Arrays.asList(new Boolean[FILTER_ITEM_RES_ARRAY.length]));
    }

    private void setClickPosition(int position) {
        if (position < 0) {
            return;
        }
        mItemsClickStateList.set(position, true);
        mLastClickPosition = position;
        if (mOnItemSelectedListener != null) {
            mOnItemSelectedListener.onItemSelected(position);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int itemPosition);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }
}
