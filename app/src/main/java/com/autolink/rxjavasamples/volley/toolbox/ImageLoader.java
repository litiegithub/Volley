package com.autolink.rxjavasamples.volley.toolbox;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.autolink.rxjavasamples.volley.Request;
import com.autolink.rxjavasamples.volley.RequestQueue;
import com.autolink.rxjavasamples.volley.Response;
import com.autolink.rxjavasamples.volley.VolleyError;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Administrator on 2018/5/14.
 */
@SuppressWarnings({"unused" ,"StringBufferReplaceableByString"})
public class ImageLoader {
    private final RequestQueue mRequestQueue ;

    private final ImageCache mCache ;

    private final HashMap<String , BatchedImageRequest> mInFlightRequests = new HashMap<String ,BatchedImageRequest>();

    private final HashMap<String , BatchedImageRequest> mBatchedResponse = new HashMap<String , BatchedImageRequest>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mRunnable ;

    public interface ImageCache{
        Bitmap getBitmap(String url);
        void putBitmap(String url , Bitmap bitmap);
    }

    public ImageLoader(RequestQueue queue , ImageCache imageCache){
        mRequestQueue = queue ;
        mCache = imageCache ;
    }

    public static ImageListener getImageListener(final ImageView view, final int defaultImageResId,final int errorImageResId) {
        return new ImageListener() {
            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }
        };
    }

    public ImageContainer get(String requestUrl , ImageListener imageListener , int maxWidth , int maxHeight , ImageView.ScaleType scaleType){
        throwIfNotOnMainThread();
        final String cacheKey = getCacheKey(requestUrl , maxWidth ,maxHeight ,scaleType);
        Bitmap cacheBitmap = mCache.getBitmap(cacheKey);
        if(cacheBitmap != null){
            ImageContainer container = new ImageContainer(cacheBitmap ,requestUrl , null ,null);

            imageListener.onResponse(container , true);
            return container ;
        }

        ImageContainer imageContainer = new ImageContainer(null ,requestUrl ,cacheKey ,imageListener);
        imageListener.onResponse(imageContainer ,true);

        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if(request != null){
            request.addContainer(imageContainer);
            return imageContainer;
        }
        Request<Bitmap> newRequest = makeImageRequest(requestUrl , maxWidth ,maxHeight ,scaleType ,cacheKey);
        mRequestQueue.add(newRequest);
        mInFlightRequests.put(cacheKey ,new BatchedImageRequest(newRequest ,imageContainer));
        return imageContainer ;
    }

    private String getCacheKey(String url , int maxWidth , int maxHeight , ImageView.ScaleType scaleType){
        return new StringBuilder(url.length()+12).append("#W").append(maxWidth).append("#H").append(maxHeight).append("#S").append(scaleType.ordinal()).toString();
    }

    private boolean isCached(String requestUrl , int maxWidth ,int maxHeight){
        return isCached(requestUrl , maxWidth ,maxHeight , ImageView.ScaleType.CENTER_INSIDE);
    }

    private boolean isCached(String requestUrl ,int maxWidth ,int maxHeight ,ImageView.ScaleType scaleType){
        throwIfNotOnMainThread();
        String cacheKey = getCacheKey(requestUrl , maxWidth ,maxHeight ,scaleType);
        return mCache.getBitmap(cacheKey) != null ;
    }

    protected Request<Bitmap> makeImageRequest(final String  requestUrl , int maxWidth , int maxHeight , ImageView.ScaleType scaleType ,final String cacheKey){
        return new ImageRequest(requestUrl ,new Response.Listener<Bitmap>(){
            @Override
            public void onResponse(Bitmap response) {
                onGetImageSuccess(cacheKey , response);
            }
        } ,maxWidth ,maxHeight ,scaleType ,Bitmap.Config.RGB_565 , new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                onGetImageError(cacheKey , error);
            }
        }) ;
    }


    private void onGetImageError(String cacheKey , VolleyError error){
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);
        if(request != null){
            request.setError(error);
            batchResponse(cacheKey , request);
        }
    }

    protected void onGetImageSuccess(String cacheKey , Bitmap response){
        mCache.putBitmap(cacheKey , response);
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);
        if(request != null){
            request.mResponseBitmap = response ;
            batchResponse(cacheKey , request);
        }
    }

    private void batchResponse(String cacheKey , BatchedImageRequest request){
        mBatchedResponse.put(cacheKey , request);
        if(mRunnable == null){
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for(BatchedImageRequest bir : mBatchedResponse.values()){
                        for (ImageContainer container : bir.mContainers){
                            if(container.mListener == null){
                                continue;
                            }

                            if(bir.getError() == null){
                                container.mBitmap = bir.mResponseBitmap;
                                container.mListener.onResponse(container , false );
                            }else{
                                container.mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    mBatchedResponse.clear();
                    mRunnable = null ;
                }
            };
            mHandler.postDelayed(mRunnable , 100);
        }
    }


    private void throwIfNotOnMainThread(){
        if(Looper.myLooper() != Looper.getMainLooper()){
            throw new IllegalStateException("ImageLoader must be invoked from the main thread");
        }
    }

    public interface ImageListener extends Response.ErrorListener{
        void onResponse(ImageContainer response ,boolean isImmediate);
    }

    public class ImageContainer{
        private Bitmap mBitmap ;

        private final String mCacheKey ;

        private final String mRequestUrl ;

        private final ImageListener mListener ;

        public ImageContainer(Bitmap bitmap , String requestUrl ,String cacheKey ,ImageListener listener){
            mBitmap = bitmap ;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey ;
            mListener = listener;
        }

        public void cancelRequest(){
            if(mListener == null){
                return ;
            }

            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if(request != null){
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if(canceled){
                    mInFlightRequests.remove(mCacheKey);
                }
            }else{
                request = mBatchedResponse.get(mCacheKey);
                if(request != null){
                    request.removeContainerAndCancelIfNecessary(this);
                    if(request.mContainers.size() == 0){
                        mBatchedResponse.remove(mCacheKey);
                    }
                }
            }
        }

        public Bitmap getBitmap(){
            return mBitmap ;
        }

        public String getRequestUrl(){
            return mRequestUrl ;
        }
    }

    private class BatchedImageRequest{
        private final Request<?> mRequest ;

        private Bitmap mResponseBitmap ;

        private VolleyError mError ;

        private final LinkedList<ImageContainer> mContainers = new LinkedList<>();

        public BatchedImageRequest(Request<?> request ,ImageContainer container){
            mRequest = request ;
            mContainers.add(container);
        }

        public VolleyError getError(){
            return mError ;
        }

        public void setError(VolleyError error){
            mError = error;
        }

        public void addContainer(ImageContainer container){
            mContainers.add(container);
        }

        public boolean removeContainerAndCancelIfNecessary(ImageContainer container){
            mContainers.remove(container);
            if(mContainers.size() == 0){
                mRequest.cancel();
                return true ;
            }
            return false ;
        }
    }
}
