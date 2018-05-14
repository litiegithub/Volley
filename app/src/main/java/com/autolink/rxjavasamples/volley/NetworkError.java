package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/11.
 */

public class NetworkError extends VolleyError {
    public NetworkError(){}

    public NetworkError(Throwable cause){
        super(cause);
    }

    public NetworkError(NetworkResponse response){
        super(response);
    }
}
