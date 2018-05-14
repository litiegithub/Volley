package com.autolink.rxjavasamples.volley;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/5/10.
 */

public class NetworkResponse {
    public final int statusCode ;

    public final byte[] data ;

    public final boolean notModified ;

    public final long networkTimeMs ;

    public final Map<String ,String> headers ;

    public NetworkResponse(int statusCode ,byte[]data ,Map<String ,String> headers,boolean notModified ,long networkTimeMs){
        this.statusCode = statusCode ;
        this.data = data ;
        this.headers = headers ;
        this.notModified = notModified ;
        this.networkTimeMs = networkTimeMs ;
    }

    public NetworkResponse(int statusCode ,byte[] data ,Map<String ,String>headers ,boolean notModified){
        this(statusCode , data ,headers ,notModified ,0);
    }

    public NetworkResponse(byte[] data){
        this(HttpURLConnection.HTTP_OK ,data ,Collections.<String, String>emptyMap() ,false , 0);
    }

    public NetworkResponse(byte[]data ,Map<String ,String> headers){
        this(HttpURLConnection.HTTP_OK,data ,headers ,false ,0);
    }

}
