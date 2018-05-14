package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/9.
 */


import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Request 请求调度队列
 */
@SuppressWarnings("unuesd")
public class RequestQueue {
    public interface RequestFinshedListener<T>{
        void onRequestFinished(Request<T> request);
    }

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

//    private final Map<String , Queue<Request<?>>> mWaitingRequests = new HashMap<String ,Queue<Request<?>>>();
    private final Map<String, Queue<Request<?>>> mWaitingRequests =new HashMap<String, Queue<Request<?>>>();

    private final Set<Request<?>> mCurrentRequests = new HashSet<Request<?>>();

    private final PriorityBlockingQueue<Request<?>> mCacheQueue = new PriorityBlockingQueue<>();

    private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue<>();

    /** 默认开启的网络线程的数量 */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    private final Cache mCache ;

    private final Network mNetwork ;

    private final ResponseDelivery mDelivery ;

    private NetworkDispatcher[] mDispatchers ;

    private CacheDispatcher mCacheDispatcher ;
    private List<RequestFinshedListener> mFinishedListeners = new ArrayList<>();

    public RequestQueue(Cache cache , Network  network){
        this(cache ,network ,DEFAULT_NETWORK_THREAD_POOL_SIZE );
    }

    public RequestQueue(Cache cache , Network network ,int threadPoolSize){
        this(cache , network ,threadPoolSize ,new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(Cache cache ,Network network ,int threadPoolSize ,ResponseDelivery delivery){
        mCache = cache ;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery ;
    }

    public void start(){
        stop();
        mCacheDispatcher = new CacheDispatcher(mCacheQueue , mNetworkQueue ,mCache ,mDelivery);
        mCacheDispatcher.start();
        for(int i = 0 ;i < mDispatchers.length ;i++){
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mNetworkQueue ,mNetwork ,mCache ,mDelivery);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    private void stop(){
        if(mCacheDispatcher != null){
            mCacheDispatcher.quit();
        }
        for(NetworkDispatcher dispatcher : mDispatchers){
            if(dispatcher != null){
                dispatcher.quit();
            }
        }
    }

    public <T> Request<?> add(Request<T> request){
        request.setRequestQueue(this);
        synchronized (mCurrentRequests){
            mCurrentRequests.add(request);
        }
        request.setSequence(getSequenceNumber());
        if(!request.shouldCache()){
            mNetworkQueue.add(request);
            return request;
        }

        synchronized (mWaitingRequests){
            String cacheKey = request.getCacheKey();
            if(mWaitingRequests.containsKey(cacheKey)){
                Queue<Request<?>> stageRequest = mWaitingRequests.get(cacheKey);
                if(stageRequest == null){
                    stageRequest = new LinkedList<>();
                }
                stageRequest.add(request);
                mWaitingRequests.put(cacheKey , stageRequest);
            }else{
                mWaitingRequests.put(cacheKey , null);
                mCacheQueue.add(request);
            }
            return request ;
        }
    }

    private int getSequenceNumber(){
        return mSequenceGenerator.incrementAndGet();
    }

    <T> void finish(Request<T> request){
        synchronized (mCurrentRequests){
            mCurrentRequests.remove(request);
        }

        synchronized (mFinishedListeners){
            for (RequestFinshedListener<T> listener : mFinishedListeners){
                listener.onRequestFinished(request);
            }

            if(request.shouldCache()){
                synchronized (mWaitingRequests){
                    String cacheKey = request.getCacheKey();
                    Queue<Request<?>> waitingRequests = mWaitingRequests.remove(request);
                    if(waitingRequests != null){
                        mCacheQueue.addAll(waitingRequests);
                    }
                }
            }
        }
    }

    public <T> void addRequestFinishedListener(RequestFinshedListener<T> listener){
        synchronized (mFinishedListeners){
            mFinishedListeners.add(listener);
        }
    }

    public <T> void removeRequestFinishedListener(RequestFinshedListener<T> listener){
        synchronized (mFinishedListeners){
            mFinishedListeners.remove(listener);
        }
    }
}
