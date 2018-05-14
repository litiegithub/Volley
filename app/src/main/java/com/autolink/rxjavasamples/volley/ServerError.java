package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/11.
 */

public class ServerError extends VolleyError {
    public ServerError(NetworkResponse response){
        super(response);
    }

    public ServerError(){
        super();
    }
}
