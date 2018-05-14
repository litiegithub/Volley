package com.autolink.rxjavasamples.volley;

import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;

import java.util.concurrent.BlockingQueue;


/**
 * Created by Administrator on 2018/5/10.
 */

public class NetworkDispatcher extends Thread {
    private final BlockingQueue<Request<?>> mQueue ;

    private final Network mNetwork ;

    private final Cache mCache ;

    private final ResponseDelivery mDelivery ;

    private volatile boolean mQuit = false ;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network , Cache cache , ResponseDelivery delivery){
        mQueue = queue ;
        mNetwork = network ;
        mCache = cache ;
        mDelivery = delivery ;
    }

    public void quit(){
        mQuit = true ;
        interrupt();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true){
            long startTimeMs = SystemClock.elapsedRealtime();
            Request<?> request ;
            try {
                request = mQueue.take();
            } catch (InterruptedException e) {
                if(mQuit){
                    return ;
                }
                continue;
            }

            try{
                if(request.isCanceled()){
                    continue;
                }
                addTrafficStatsTag(request);

                NetworkResponse networkResponse = mNetwork.performRequest(request);
                if(networkResponse.notModified && request.hasHadResponseDelivered()){
                    request.finish("not-modified");
                    continue;
                }

                Response<?> response = request.parseNetworkResponse(networkResponse);

                if(request.shouldCache() && response.cacheEntry != null){
                    mCache.put(request.getCacheKey() , response.cacheEntry);

                }
                request.markDelivered();
                mDelivery.postResponse(request ,response);
            }catch (VolleyError volleyError){
                volleyError.printStackTrace();
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() -startTimeMs);
                parseAndDeliveryNetworkError(request , volleyError);
            }catch(Exception e){
                VolleyError volleyError = new VolleyError(e);
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                mDelivery.postError(request , volleyError);
            }
        }
    }

    private void parseAndDeliveryNetworkError(Request<?> request , VolleyError error){
        error = request.parseNetworkError(error);
        mDelivery.postError(request ,error);
    }

    private void addTrafficStatsTag(Request<?> request){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());;
        }
    }

}
