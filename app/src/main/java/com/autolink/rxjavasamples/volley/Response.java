package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/10.
 */
/** 网络请求结果封装类，其中泛型T为网络解析结果 */
public class Response<T> {
    /** request请求成功回调接口，用于用户自行处理网络请求返回的结果 */
    public interface Listener<T>{
        void onResponse(T response);
    }

    /** request请求失败回调接口，用于用户自行处理网络请求失败的情况*/
    public interface ErrorListener{
        void onErrorResponse(VolleyError error);
    }

    public static <T> Response<T> success(T result ,Cache.Entry cacheEntry ){
        return new Response<T>(result , cacheEntry);
    }

    public static <T> Response<T> error(VolleyError error){
        return new Response<T>(error);
    }

    public final T result ;

    public final Cache.Entry cacheEntry ;

    public final VolleyError error;

    public boolean intermediate = false ;

    public boolean isSuccess(){
        return error == null ;
    }

    private Response(T result , Cache.Entry cacheEntry){
        this.result = result ;
        this.cacheEntry = cacheEntry;
        this.error = null ;
    }

    private Response(VolleyError error){
        this.result = null ;
        this.cacheEntry = null ;
        this.error = error;
    }
}
