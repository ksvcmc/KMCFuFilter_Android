package com.ksyun.media.kmcfilter.faceunity.demo;

import android.content.Context;

import com.ksyun.media.streamer.filter.imgtex.ImgTexPreview;
import com.ksyun.media.streamer.kit.KSYStreamer;

/**
 * Created by sujia on 2017/9/20.
 */

public class KMCStreamer extends KSYStreamer {
    private static final String TAG = "KSYStreamer";
    private static final boolean DEBUG = false;

    public KMCStreamer(Context context) {
        super(context.getApplicationContext());
    }


    public ImgTexPreview getImgTexPreviewer() {
        return mImgTexPreview;
    }

    @Override
    public void release() {
        super.release();
        getImgTexFilterMgt().release();
        setOnErrorListener(null);
        setOnInfoListener(null);
        setOnLogEventListener(null);
    }
}
