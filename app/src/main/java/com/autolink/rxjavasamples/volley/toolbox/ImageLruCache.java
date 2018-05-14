package com.autolink.rxjavasamples.volley.toolbox;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Administrator on 2018/5/14.
 */
@SuppressWarnings("unused")
public class ImageLruCache implements ImageLoader.ImageCache {
    private LruCache<String , Bitmap> mLruCache ;

    public ImageLruCache(){
        this((int)Runtime.getRuntime().maxMemory()/8);
    }

    public ImageLruCache(final int cacheSize){
        createLruCache(cacheSize);
    }

    private void createLruCache(final int cacheSize){
        mLruCache = new LruCache<String ,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mLruCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mLruCache.put(url ,bitmap);
    }
}
