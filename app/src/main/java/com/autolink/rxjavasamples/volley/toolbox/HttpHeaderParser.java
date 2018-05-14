package com.autolink.rxjavasamples.volley.toolbox;

import com.autolink.rxjavasamples.volley.Cache;
import com.autolink.rxjavasamples.volley.NetworkResponse;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import java.util.Map;

/**
 * Created by Administrator on 2018/5/14.
 */

public class HttpHeaderParser {
    public static Cache.Entry parseCacheHeaders(NetworkResponse response){
        long now = System.currentTimeMillis() ;

        Map<String ,String> headers = response.headers ;

        long serverDate = 0 ;
        long lastModified = 0;
        long serverExpires = 0 ;
        long softExpire = 0 ;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0 ;
        boolean hasCacheControl = false ;
        boolean mustRevalidate = false ;

        String serverEtag ;
        String headerValue ;

        headerValue = headers.get("Date");
        if(headerValue != null){
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if(headerValue != null){
            hasCacheControl = true ;
            String[]tokens = headerValue.split(",");
            for (String token :tokens){
                token = token.trim();
                if(token.startsWith("no-cache") || token.equals("no-store")){
                    return null ;
                }else if(token.startsWith("max-age=")){
                    try {
                    maxAge = Long.parseLong(token.substring(8));
                    }catch (Exception e){
                        maxAge = 0;
                    }
                }else if(token.startsWith("stale-while-revaludate=")){
                    try {
                    staleWhileRevalidate = Long.parseLong(token.substring(23));
                    }catch (Exception e){
                        staleWhileRevalidate = 0;
                    }
                }else if(token.equals("must-revalidate") || token.equals("proxy-revalidate")){
                    mustRevalidate = true ;
                }
            }
        }

        headerValue = headers.get("Expires");
        if(headerValue != null){
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Last-Modified");
        if(headerValue != null){
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");
        if(hasCacheControl){
            softExpire = now + maxAge * 1000 ;
            finalExpire = mustRevalidate ? softExpire : softExpire + staleWhileRevalidate * 1000 ;
        }else if(serverDate > 0&& serverExpires >= serverDate){
            softExpire = now + (serverExpires -serverDate);
            finalExpire = softExpire ;
        }

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag ;
        entry.softTtl = softExpire ;
        entry.ttl = finalExpire ;
        entry.serverDate = serverDate ;
        entry.lastModified = lastModified ;
        entry.responseHeaders = headers ;

        return entry ;
    }

    public static long parseDateAsEpoch(String dateStr){
        try {
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            return 0;
        }
    }

    public static String parseCharset(Map<String ,String>headers ,String defaultCharset){
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if(contentType != null){
            String [] params = contentType.split(";");
            for (int i = 1 ;i < params.length ;i++){
                String [] pair = params[i].trim().split("=");
                if(pair.length == 2){
                    if(pair[0].equals("charset")){
                        return pair[1];
                    }
                }
            }
        }
        return defaultCharset ;
    }

    public static String parseCharset(Map<String ,String> headers){
        return parseCharset(headers , HTTP.DEFAULT_CONTENT_CHARSET);
    }
}
