package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/10.
 */

public interface ResponseDelivery {
    void postResponse(Request<?> request ,Response<?> response);

    void postResponse(Request<?> request ,Response<?> response ,Runnable runnable);

    void postError(Request<?> request ,VolleyError error);
}
