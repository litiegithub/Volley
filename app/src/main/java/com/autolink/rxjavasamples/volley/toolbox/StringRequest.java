package com.autolink.rxjavasamples.volley.toolbox;

import com.autolink.rxjavasamples.volley.NetworkResponse;
import com.autolink.rxjavasamples.volley.Request;
import com.autolink.rxjavasamples.volley.Response;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2018/5/14.
 */
@SuppressWarnings("unused")
public class StringRequest extends Request<String> {
    private final Response.Listener<String> mListener ;

    public StringRequest(int method , String url , Response.Listener<String> listener, Response.ErrorListener errorListener){
        super(method , url , errorListener);
        mListener = listener ;
    }

    public StringRequest(String url , Response.Listener<String> listener , Response.ErrorListener errorListener){
        this(Method.GET , url ,listener ,errorListener);
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        String parsed ;
        try {
            parsed = new String(response.data ,HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed , HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
