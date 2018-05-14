package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/10.
 */
@SuppressWarnings("unused")
public interface RetryPolicy {
    /**
     * 获取当前请求的超时时间
     * @return
     */
    int getCurrentTimeout();

    /**
     * 获取当前请求的重试次数
     * @return
     */
    int getCurrentRetryCount();

    /**
     * 实现类需要重点实现的方法，用于判断当前request是否还需要在进行重试操作
     * @param error
     * @throws VolleyError
     */
    void retry(VolleyError error) throws VolleyError;

}
