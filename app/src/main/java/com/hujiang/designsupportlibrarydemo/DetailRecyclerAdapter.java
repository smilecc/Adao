package com.hujiang.designsupportlibrarydemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.me.lewisdeane.ldialogs.CustomDialog;

public class DetailRecyclerAdapter extends RecyclerView.Adapter<DetailRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<Chuan> mChuanList;

    public DetailRecyclerAdapter(Context mContext,List<Chuan> ChuanList) {
        mChuanList = ChuanList;
        this.mContext = mContext;
    }

    @Override
    public DetailRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_detail, parent, false);
        return new DetailRecyclerAdapter.ViewHolder(view);
    }

    private final static String ThumbCdn = "http://h-nimingban-com.n1.yun.tf:8999/Public/Upload/thumb/";
    private final static String ImageCdn = "http://h-nimingban-com.n1.yun.tf:8999/Public/Upload/image/";
    @Override
    public void onBindViewHolder(final DetailRecyclerAdapter.ViewHolder holder, final int position) {
        final View view = holder.mView;
        holder.mUseridTextView.setText(mChuanList.get(position).userid);
        holder.mCountTextView.setText(mChuanList.get(position).count + "");
        holder.mTimeTextView.setText(mChuanList.get(position).time);
        holder.mTitleTextView.setText(mChuanList.get(position).title);
        CharSequence charSequence= Html.fromHtml(mChuanList.get(position).content);
        holder.mContentTextView.setText(charSequence);
        if(mChuanList.get(position).img.equals(""))
        {
            holder.mImageView.setVisibility(View.GONE);
        }
        else
        {
            holder.mImageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(ThumbCdn + mChuanList.get(position).img)
                    .placeholder(R.drawable.ic_img)
                    .error(R.drawable.ic_error)
                    .into(holder.mImageView);
        }
        if(mChuanList.get(position).title.equals("无标题")) holder.mTitleTextView.setVisibility(View.GONE);
        View.OnClickListener Listener;

        if(!(mChuanList.get(position).isEndPage)) {
            Listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 5, 0);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Intent intent = new Intent(mContext, DetailActivity.class);
                            intent.putExtra("id", mChuanList.get(position).id);
                            intent.putExtra("name", mChuanList.get(position).title);
                            intent.putExtra("userid", mChuanList.get(position).userid);
                            intent.putExtra("time", mChuanList.get(position).time);
                            intent.putExtra("content", mChuanList.get(position).content);
                            intent.putExtra("count", mChuanList.get(position).count);
                            intent.putExtra("img", mChuanList.get(position).img);
                            intent.putExtra("mode", 1);
                            mContext.startActivity(intent);
                        }
                    });
                    animator.start();
                }
            };
        }
        else
        {
            holder.mCountTextView.setVisibility(View.GONE);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ImgActivity.class);
                    intent.putExtra("img", ImageCdn + mChuanList.get(position).img);
                    mContext.startActivity(intent);
                }
            });

            final CustomDialog.Builder builder = new CustomDialog.Builder(mContext, "本串引用的串", mChuanList.get(position).id == -1?"刷新前不能回复本串":"回复本串");
            builder.negativeText("返回");
            builder.positiveColor("#009900");
            builder.contentTextSize(15);
            builder.contentColor("#000000");



            Listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 5, 0);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ;

                            Pattern p = Pattern.compile("&gt;&gt;No\\.([\\d]*)");
                            Matcher m = p.matcher(mChuanList.get(position).content);

                            Log.d("adao",mChuanList.get(position).content );

                            if(m.find())
                            {
                                int replyId = Integer.parseInt(m.group(1));
                                Log.d("adao",replyId+" id");
                                int FindListIndex = -1;
                                for (int i=0;i<mChuanList.size();++i)
                                {
                                    if(mChuanList.get(i).id == replyId)
                                    {
                                        FindListIndex = i;
                                        break;
                                    }
                                }

                                if(FindListIndex == -1) builder.content("串失踪啦 ⊂彡☆))д`) 不在本页或被删除");
                                else builder.content(mChuanList.get(FindListIndex).userid + "<br/>" + mChuanList.get(FindListIndex).time + "<br/><br/>" + mChuanList.get(FindListIndex).content);
                            }
                            else
                            {
                                builder.content("该串没有引用其他串 |∀ﾟ");
                            }
                            final CustomDialog customDialog = builder.build();
                            customDialog.setClickListener(new CustomDialog.ClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    Intent intent = new Intent(mContext,ReplyActivity.class);
                                    intent.putExtra("isBoard",false);
                                    intent.putExtra("id",mChuanList.get(position).formChuanId);
                                    intent.putExtra("replyId",mChuanList.get(position).id);
                                    Activity activity = (Activity)mContext;
                                    activity.startActivityForResult(intent,1);
                                }

                                @Override
                                public void onCancelClick() {

                                }
                            });
                            customDialog.show();

                        }
                    });

                    animator.start();
                }

            };

            View.OnLongClickListener LongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return false;
                }
            };
            view.setOnLongClickListener(LongClickListener);
        }
        view.setOnClickListener(Listener);

        holder.mContentTextView.setOnClickListener(Listener);
    }

    @Override
    public int getItemCount() {
        return mChuanList.size();
    }

    // 元素构造器
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mUseridTextView;
        public final TextView mTimeTextView;
        public final TextView mCountTextView;
        public final TextView mTitleTextView;
        public final TextView mContentTextView;
        public final ImageView mImageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUseridTextView = (TextView) view.findViewById(R.id.chuan_userid);
            mTimeTextView = (TextView) view.findViewById(R.id.chuan_time);
            mCountTextView = (TextView) view.findViewById(R.id.chuan_count);
            mTitleTextView = (TextView) view.findViewById(R.id.chuan_title);
            mContentTextView = (TextView) view.findViewById(R.id.chuan_content);
            mImageView = (ImageView) view.findViewById(R.id.chuan_img);

            mContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
