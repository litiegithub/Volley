package com.autolink.rxjavasamples.volley.toolbox;

import android.os.SystemClock;
import android.util.Log;

import com.autolink.rxjavasamples.volley.AuthFailureError;
import com.autolink.rxjavasamples.volley.Cache;
import com.autolink.rxjavasamples.volley.ClientError;
import com.autolink.rxjavasamples.volley.Network;
import com.autolink.rxjavasamples.volley.NetworkError;
import com.autolink.rxjavasamples.volley.NetworkResponse;
import com.autolink.rxjavasamples.volley.NoConnctionError;
import com.autolink.rxjavasamples.volley.Request;
import com.autolink.rxjavasamples.volley.RetryPolicy;
import com.autolink.rxjavasamples.volley.ServerError;
import com.autolink.rxjavasamples.volley.TimeoutError;
import com.autolink.rxjavasamples.volley.VolleyError;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by Administrator on 2018/5/11.
 */

public class BasicNetwork implements Network {
    private final HttpStack mHttpStack ;

    public BasicNetwork(HttpStack httpStack){
        mHttpStack = httpStack ;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        while(true){
            HttpResponse httpResponse = null ;
            byte[] responseContents = null ;
            Map<String ,String> responseHeaders = Collections.emptyMap();
            try {
                Map<String ,String>headers = new HashMap<String, String>();
                addCacheHeaders(headers ,request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request , headers);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                responseHeaders = convertHeaders(httpResponse.getAllHeaders());

                if(statusCode == HttpStatus.SC_NOT_MODIFIED){
                    Cache.Entry entry = request.getCacheEntry();
                    if(entry == null){
                        return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED ,null ,responseHeaders ,true ,SystemClock.elapsedRealtime() - requestStart);
                    }

                    entry.responseHeaders.putAll(responseHeaders);
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED ,entry.data , entry.responseHeaders ,true ,SystemClock.elapsedRealtime() - requestStart);
                }

                if(httpResponse.getEntity() != null){
                    responseContents = entityToBytes(httpResponse.getEntity());
                }else{
                    responseContents = new byte[0];
                }
                if(statusCode < 200 || statusCode > 299){
                    throw new IOException();
                }
                return new NetworkResponse(statusCode , responseContents ,responseHeaders ,false ,SystemClock.elapsedRealtime() - requestStart);

            } catch(SocketException e ){
                attemptRetryOnException("socket" ,request ,new TimeoutError());
            }catch(ConnectTimeoutException e){
                attemptRetryOnException("connection" ,request ,new TimeoutError());
            }catch(MalformedURLException e){
                throw new RuntimeException("Bad URL"+request.getUrl() , e);
            }catch (IOException e) {
                int statusCode ;
                if(httpResponse != null){
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                }else{
                    throw new NoConnctionError(e);
                }
                NetworkResponse networkResponse ;
                if(responseContents != null){
                    networkResponse = new NetworkResponse(statusCode , responseContents ,responseHeaders , false ,SystemClock.elapsedRealtime() - requestStart);
                    if(statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN){
                        attemptRetryOnException("auth" , request , new AuthFailureError(networkResponse));;
                    }else if(statusCode >= 400 && statusCode <= 499){
                        throw new ClientError(networkResponse);
                    }else if(statusCode >= 500 && statusCode <= 599){
                        if(request.shouldRetryServerErrors()){
                            attemptRetryOnException("server" ,request , new ServerError(networkResponse));
                        }else{
                            throw new ServerError(networkResponse);
                        }
                    }else{
                        throw new ServerError(networkResponse);
                    }
                }else{
                    attemptRetryOnException("network",request ,new NetworkError());
                }
            }
        }
    }

    private void addCacheHeaders(Map<String ,String>headers , Cache.Entry entry){
        if(entry == null){
            return ;
        }

        if(entry.etag != null){
            headers.put("If-None-Match" ,entry.etag);
        }

        if(entry.lastModified > 0){
            Date refTime = new Date(entry.lastModified);
            headers.put("" , DateUtils.formatDate(refTime));
        }
    }

    private static Map<String , String>convertHeaders(Header[] headers){
        Map<String ,String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER );
        for (Header header :headers){
            result.put(header.getName() ,header.getValue());
        }
        return result ;
    }

    private byte[] entityToBytes(HttpEntity entity) throws IOException ,ServerError {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        }finally{
            try {
                entity.consumeContent();
            }catch (IOException e){
                e.printStackTrace();
            }
            bytes.close();
        }
    }



    private void attemptRetryOnException(String logPrefix ,Request<?> request ,VolleyError exception)throws VolleyError{
        RetryPolicy retryPolicy = request.getmRetryPolicy();
        int oldTimeout = request.getTimeoutMs();
        retryPolicy.retry(exception);
        Log.e("Volley" , String.format("%s-retry [timeout=%s]" ,logPrefix ,oldTimeout));
    }
}
