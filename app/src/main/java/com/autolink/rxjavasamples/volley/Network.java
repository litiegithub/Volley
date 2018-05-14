package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/10.
 */

public interface Network {
    NetworkResponse performRequest(Request<?> request) throws VolleyError ;
}
