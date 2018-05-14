package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/10.
 */

public class DefaultRetryPolicy implements RetryPolicy {

    private int mCurrentTimeMs ;

    private int mCurrentRetryCount ;

    private final int mMaxNumReties ;

    private final float mBackoffMulitplier ;

    public static final int DEFAULT_TIMEOUT_MS = 2500;

    public static final int DEFAULT_MAX_RETRIES = 0 ;

    public static final float DEFAULT_BACKOFF_MULT  = 1f ;

    public DefaultRetryPolicy(){
        this(DEFAULT_TIMEOUT_MS ,DEFAULT_MAX_RETRIES ,DEFAULT_BACKOFF_MULT);
    }

    public DefaultRetryPolicy(int initialIimeoutMs ,int maxNumReties ,float backoffMulitplier){
        mCurrentTimeMs = initialIimeoutMs ;
        mMaxNumReties = maxNumReties ;
        mBackoffMulitplier = backoffMulitplier ;
    }

    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeMs;
    }

    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        mCurrentRetryCount ++ ;

        mCurrentTimeMs += mCurrentTimeMs*mBackoffMulitplier ;
        if(!hasAttempRemaining()){
            throw error ;
        }
    }

    private boolean hasAttempRemaining(){
        return mCurrentTimeMs <= mMaxNumReties ;
    }
}
