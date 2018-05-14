package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/9.
 */

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;


/**
 * Volley的网络请求基类
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    private static final String DEFAULT_PARAMS_EDCODING = "UTF-8";

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    private final int mMethod;

    private final String mUrl;

    private final int mDefaultTrafficStatsTag;

    private final Response.ErrorListener mErrorListener;

    private Integer mSequence;

    private RequestQueue mRequestQueue;

    private boolean mShouldCache = true;

    private boolean mCanceled = false;

    private boolean mResponseDelivered = false;

    private boolean mShouldRetryServerErrors = false;

    private RetryPolicy mRetryPolicy;

    private Cache.Entry mCacheEntry = null;

    public Request(int method, String url, Response.ErrorListener listener) {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public int getmMethod() {
        return mMethod;
    }

    public Response.ErrorListener getmErrorListener(){
        return mErrorListener ;
    }

    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * 使用url的host字段的hash值作为统计类的tag。
     *
     * @param url
     * @return
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    /**
     * 设置重试接口。典型的组合模式，关联关系
     *
     * @param retryPolicy
     * @return
     */
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        mRetryPolicy = retryPolicy;
        return this;
    }

    /**
     * 调试打印当前请求进度使用
     *
     * @param tag
     */
    public void addMarker(String tag) {
        Log.e("Volley", tag);
    }

    /**
     * 用于告知请求队列当前request已经结束
     *
     * @param tag
     */
    void finish(final String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }
    }

    /**
     * 设置当前request的请求队列
     *
     * @param requestQueue
     * @return
     */
    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    /**
     * 设置当前request在当前request队列的系列号
     */
    public final Request<?> setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }

    /**
     * 返回request请求的序列号
     */
    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    /**
     * 返回request的url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 使用request的url作为volley cache缓存系统存储的key值（默认url可以唯一标识一个request）
     *
     * @return
     */
    public String getCacheKey() {
        return getUrl();
    }

    /**
     * 设置request对应的volley cache缓存系统中的请求结果
     *
     * @param entry
     * @return
     */
    public Request<?> setCacheEntry(Cache.Entry entry) {
        mCacheEntry = entry;
        return this;
    }

    /**
     * 返回request的cache系统的请求结果
     *
     * @return
     */
    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    /**
     *
     *
     */
    public void cancel() {
        mCanceled = true;
    }

    /**
     * 返回该request是否被取消标识
     *
     * @return
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    /**
     * 返回该请求的headers
     *
     * @return
     * @throws AuthFailureError
     */
    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }

    /**
     * 返回该请求的体中参数
     * 如果是GET请求，则直接返回null
     * 如果是post请求，需要重写该方法，返回需要传递的参数Map
     *
     * @return
     * @throws AuthFailureError
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    /**
     * 返回该request请求参数编码
     *
     * @return
     */
    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_EDCODING;
    }

    /**
     * @return
     */
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params , getParamsEncoding());
        }
        return null ;
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append("=");
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append("&");
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported :"+paramsEncoding);
        }
    }

    /**
     * 设置当前request是否需要被缓存
     * @param shouldCache
     * @return
     */
    public final Request<?> setShouldCache(boolean shouldCache){
        mShouldCache = shouldCache ;
        return this ;
    }

    /**
     * 返回当前request是否需要被缓存
     * @return
     */
    public final boolean shouldCache(){
        return mShouldCache ;
    }

    /**
     * 设置request的重试接口
     * @param shouldRetryServerErrors
     * @return
     */
    public final Request<?> setShouldRetryServerErrors(boolean shouldRetryServerErrors){
        mShouldRetryServerErrors = shouldRetryServerErrors;
        return this ;
    }

    /**
     * 返回该request当遇到服务器错误时是否需要重试标志
     * @return
     */
    public final boolean shouldRetryServerErrors(){
        return mShouldRetryServerErrors ;
    }

    /**
     * request优先级枚举类
     */
    public enum Priority{
        LOW ,
        NORMAL ,
        HIGH ,
        IMMEDIATE
    }

    /**
     * 返回当前request的优先级，子类可以重写该方法修改request的优先级
     * @return
     */
    public Priority getPriority(){
        return Priority.NORMAL ;
    }

    /**
     * 返回重试的时间，用于日志记录
     */
    public final int getTimeoutMs(){
        return mRetryPolicy.getCurrentTimeout();
    }

    /**
     * 返回重试接口
     * @return
     */
    public RetryPolicy getmRetryPolicy(){
        return mRetryPolicy ;
    }

    /**
     * 用于标识已经将response传给改request。
     */
    public void markDelivered(){
        mResponseDelivered = true ;
    }

    /**
     * 返回request是否有response delivered .
     * @return
     */
    public boolean hasHadResponseDelivered(){
        return mResponseDelivered ;
    }

    /**
     * 子类必须重写改方法，用来解析http请求结果
     * @param response
     * @return
     */
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    /**
     * 子类可以重写该方法，从而获取更精准的出错信息
     * @param volleyError
     * @return
     */
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return volleyError ;
    }

    /**
     * 子类必须重写该方法用于将网络结果返回给用户设置的回调接口
     * @param response
     */
    abstract protected void deliverResponse(T response);

    /**
     * 讲网络错误传递给回调接口
     * @param error
     */
    public void deliverError(VolleyError error){
        if(mErrorListener != null){
            mErrorListener.onErrorResponse(error);
        }
    }

    /**
     * 先判断执行顺序，在判断request优先级
     * @param another
     * @return
     */
    @Override
    public int compareTo(Request<T> another) {
        Priority left = this.getPriority();
        Priority right = another.getPriority();
        return left == right ? this.mSequence - another.mSequence : right.ordinal() -left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x"+Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X]" : "[ ]"+getUrl()+" "+trafficStatsTag+" "+getPriority()+" "+mSequence);
    }
}
