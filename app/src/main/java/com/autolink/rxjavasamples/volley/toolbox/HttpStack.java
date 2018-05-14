package com.autolink.rxjavasamples.volley.toolbox;

import com.autolink.rxjavasamples.volley.AuthFailureError;
import com.autolink.rxjavasamples.volley.Request;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/11.
 */

public interface HttpStack {
    HttpResponse performRequest(Request<?> request , Map<String ,String> additionalHeaders) throws IOException ,AuthFailureError ;
}
