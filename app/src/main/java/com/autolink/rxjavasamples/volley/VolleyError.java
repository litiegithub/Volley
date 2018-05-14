package com.autolink.rxjavasamples.volley;

import android.net.NetworkRequest;

/**
 * Created by Administrator on 2018/5/10.
 */
@SuppressWarnings("unused")
public class VolleyError extends Exception {
    public final NetworkResponse networkResponse ;
    private long networkTimeMs ;

    public VolleyError(){
        networkResponse = null ;
    }

    public VolleyError(NetworkResponse response){
        networkResponse = response ;
    }

    public VolleyError(String exceptionMessage){
        super(exceptionMessage);
        networkResponse = null ;
    }

    public VolleyError(String exceptionMessage ,Throwable reason){
        super(exceptionMessage , reason);
        networkResponse = null ;
    }

    public VolleyError(Throwable cause){
        super(cause);
        networkResponse = null ;
    }

    void setNetworkTimeMs(long networkTimeMs){
        this.networkTimeMs = networkTimeMs ;
    }

    public long getNetworkTimeMs(){
        return networkTimeMs;
    }
}
