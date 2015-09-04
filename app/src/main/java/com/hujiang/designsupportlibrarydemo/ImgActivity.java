package com.hujiang.designsupportlibrarydemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by sxcui on 2015/9/1.
 */
public class ImgActivity extends Activity {

    private static final String LOG_TAG = "image-test";
    private ProgressBar progressBar;
    private LinearLayout layoutProgressBar;

    ImageViewTouch mImage;
    String ImgPath;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.img_activity);
        Intent intent = getIntent();
        ImgPath = intent.getStringExtra("img");
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mImage = (ImageViewTouch) findViewById( R.id.image );

        mImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);

        mImage.setSingleTapListener(new ImageViewTouch.OnImageViewTouchSingleTapListener() {

            @Override
            public void onSingleTapConfirmed() {
                Log.d(LOG_TAG, "onSingleTapConfirmed");
            }
        });

        mImage.setDoubleTapListener(new ImageViewTouch.OnImageViewTouchDoubleTapListener() {

            @Override
            public void onDoubleTap() {
                Log.d(LOG_TAG, "onDoubleTap");
            }
        });

        mImage.setOnDrawableChangedListener(new ImageViewTouchBase.OnDrawableChangeListener() {

            @Override
            public void onDrawableChanged(Drawable drawable) {
                Log.i(LOG_TAG, "onBitmapChanged: " + drawable);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.img_progressBar);
        layoutProgressBar = (LinearLayout) findViewById(R.id.img_progressBar_layout);
    }

@Override
    protected void onResume() {
    super.onResume();
    layoutProgressBar.setVisibility(View.VISIBLE);
    Picasso.with(ImgActivity.this)
            .load(ImgPath + "")
            .into(mImage, new ImageLoadedCallback(progressBar) {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    layoutProgressBar.setVisibility(View.GONE);
                    mImage.setVisibility(View.VISIBLE);
                }
            });
}

@Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged( newConfig );
    }
    private class ImageLoadedCallback implements Callback {
    ProgressBar progressBar;

    public  ImageLoadedCallback(ProgressBar progBar){
        progressBar = progBar;
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }
}

}

