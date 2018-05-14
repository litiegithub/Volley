package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/11.
 */

public class ClientError extends ServerError {
    public ClientError(NetworkResponse networkResponse){
        super(networkResponse);
    }

    public ClientError(){
        super();
    }
}
