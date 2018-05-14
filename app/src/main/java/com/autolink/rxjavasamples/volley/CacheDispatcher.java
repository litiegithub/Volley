package com.autolink.rxjavasamples.volley;

import android.os.Process;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Administrator on 2018/5/11.
 */

public class CacheDispatcher extends Thread {
    private final BlockingQueue<Request<?>> mCacheQueue ;

    private final BlockingQueue<Request<?>> mNetworkQueue ;

    private final Cache mCache ;

    private final ResponseDelivery mDelivery ;

    private volatile boolean mQuit = false ;

    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue ,BlockingQueue<Request<?>> networkQueue , Cache cache ,ResponseDelivery delivery){
        mCacheQueue = cacheQueue ;
        mNetworkQueue = networkQueue ;
        mCache = cache ;
        mDelivery = delivery ;
    }

    public void quit(){
        mQuit = true ;
        interrupt();
    }

    public void run(){
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);;
        mCache.initialize();
        while(true){
            try {
                final Request<?> request = mCacheQueue.take();
                if(request.isCanceled()){
                    request.finish("cache-discard-canceled");
                    continue;
                }

                Cache.Entry entry = mCache.get(request.getCacheKey());
                if(entry == null){
                    mNetworkQueue.put(request);
                    continue;
                }

                if(entry.isExpired()){
                    request.setCacheEntry(entry);
                    mNetworkQueue.put(request);
                    continue;
                }

                Response<?>response = request.parseNetworkResponse(new NetworkResponse(entry.data ,entry.responseHeaders));

                if(!entry.refreshNeeded()){
                    mDelivery.postResponse(request ,response);
                }else{
                    request.setCacheEntry(entry);
                    response.intermediate = true;
                    mDelivery.postResponse(request, response, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mNetworkQueue.put(request);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                if(mQuit){
                    return ;
                }
            }

        }
    }

}
