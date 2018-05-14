package com.autolink.rxjavasamples.volley.toolbox;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.autolink.rxjavasamples.volley.NetworkResponse;
import com.autolink.rxjavasamples.volley.Request;
import com.autolink.rxjavasamples.volley.Response;
import com.autolink.rxjavasamples.volley.VolleyError;

/**
 * Created by Administrator on 2018/5/14.
 */

public class ImageRequest extends Request<Bitmap> {
    public static final int DEFAULT_IMAGE_REQUEST_MS = 1000 ;

    public static final int DEFAULT_IMAGE_MAX_RETRIES = 2 ;

    private final Response.Listener<Bitmap> mListener ;

    private final Bitmap.Config mDecodeConfig ;

    private final int mMaxWidth ;

    private final int mMaxHeight ;

    private ImageView.ScaleType mScaleType ;

    private static final Object sDecodeLock = new Object();

    public ImageRequest(String url , Response.Listener<Bitmap> listener , int maxWidth , int maxHeight , ImageView.ScaleType scaleType ,Bitmap.Config decodeConfig ,Response.ErrorListener errorListener){
        super(Method.GET , url , errorListener);
        mListener = listener ;
        mDecodeConfig = decodeConfig ;
        mMaxWidth = maxWidth ;
        mMaxHeight = maxHeight ;
        mScaleType = scaleType ;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }


    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                return Response.error(new VolleyError(e));
            }
        }
    }

    private Response<Bitmap> doParse(NetworkResponse response){
        byte[] data =  response.data;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap ;
        if(mMaxWidth == 0 && mMaxHeight == 0){
            decodeOptions.inPreferredConfig = mDecodeConfig ;
            bitmap = BitmapFactory.decodeByteArray(data , 0 ,data.length ,decodeOptions);
        }else{
            //
            decodeOptions.inJustDecodeBounds = true ;
            BitmapFactory.decodeByteArray(data , 0 ,data.length ,decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            int desiredWidth = getResizedDimension(mMaxWidth , mMaxHeight ,actualWidth ,actualHeight ,mScaleType);
            int desiredHeight = getResizedDimension(mMaxWidth , mMaxHeight , actualWidth ,actualHeight ,mScaleType);

            decodeOptions.inJustDecodeBounds = false ;
            decodeOptions.inSampleSize = findBestSampleSize(actualWidth ,actualHeight ,desiredWidth ,desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data ,0 ,data.length ,decodeOptions);

            if(tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)){
                bitmap = Bitmap.createScaledBitmap(tempBitmap ,desiredWidth ,desiredHeight ,true);
                tempBitmap.recycle();
            }else{
                bitmap = tempBitmap;
            }
        }

        if(bitmap == null){
            return Response.error(new VolleyError(response));
        }else{
            return Response.success(bitmap , HttpHeaderParser.parseCacheHeaders(response));
        }
    }

    static int findBestSampleSize(int actualWidth , int actualHeight ,int desiredWidth ,int desiredHeight){
        double wr = actualWidth/desiredWidth ;
        double hr = actualHeight / desiredHeight ;
        double ratio = Math.min(wr , hr);
        float n = 1.0f ;
        while((n*2) <= ratio){
            n*= 2 ;
        }
        return (int)n ;
    }

    private static int getResizedDimension(int maxPrimary ,int maxSecondary ,int actualPrimary ,int actualSecondary ,ImageView.ScaleType scaleType){
        if((maxPrimary == 0)&&(maxSecondary)==0){
            return actualPrimary ;
        }

        if(scaleType == ImageView.ScaleType.FIT_XY){
            if(maxPrimary == 0){
                return actualPrimary ;
            }
            return maxPrimary ;
        }

        if(maxPrimary == 0){
            double ratio = (double)maxSecondary /(double)actualSecondary ;
            return (int)(actualPrimary*ratio) ;
        }

        double ratio = (double) actualSecondary /(double)actualPrimary ;
        int resized = maxPrimary ;

        if(scaleType == ImageView.ScaleType.CENTER_CROP){
            if((resized*ratio) < maxSecondary){
                resized = (int)(maxSecondary / ratio);
            }
            return resized ;
        }

        if((resized * ratio)>maxSecondary){
            resized = (int)(maxSecondary /ratio);
        }
        return resized ;
    }

    @Override
    protected void deliverResponse(Bitmap response) {
        mListener.onResponse(response);
    }
}
