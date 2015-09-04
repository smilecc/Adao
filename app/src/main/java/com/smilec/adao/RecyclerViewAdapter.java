package com.smilec.adao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<Board> mBoardList;

    public RecyclerViewAdapter(Context mContext,List<Board> BoardList) {
        mBoardList = BoardList;
        this.mContext = mContext;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_main, parent, false);
        return new ViewHolder(view);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder holder, final int position) {
        final View view = holder.mView;
        holder.mNameTextView.setText(mBoardList.get(position).Name);
        holder.mMsgTextView.setText(mBoardList.get(position).Msg);
        holder.mIdTextView.setText("Board id: " + mBoardList.get(position).Id);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 5, 0);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        Intent intent = new Intent(mContext, DetailActivity.class);
                        intent.putExtra("id", mBoardList.get(position).Id);
                        intent.putExtra("name",mBoardList.get(position).Name);
                        intent.putExtra("msg",mBoardList.get(position).Msg);
                        mContext.startActivity(intent);
                    }
                });
                animator.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBoardList.size();
    }

    // 元素构造器
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameTextView,mMsgTextView,mIdTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameTextView = (TextView) view.findViewById(R.id.board_name);
            mMsgTextView = (TextView) view.findViewById(R.id.board_msg);
            mIdTextView = (TextView) view.findViewById(R.id.board_id);
        }
    }
}
