package com.autolink.rxjavasamples.volley;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/10.
 */

/**
 * 缓存内存的抽象接口
 */
@SuppressWarnings("unused")
public interface Cache {
    /** 通过key获取请求的缓存实体 */
    Entry get(String key);

    /** 存入一个请求的缓存实体 */
    void put(String key , Entry entry);

    void initialize();

    void invalidate(String key ,boolean fullExpire);

    /** 移除指定的缓存实体 */
    void remove(String key);

    /** 清空缓存 */
    void clear();

    /** 真正Htttp请求缓存实体类 */
    class Entry{
        /** Http响应体 */
        public byte[] data ;

        /** Http响应首部中用于缓存新鲜度验证的ETag */
        public String etag ;

        /**  HTTP响应时间 */
        public long serverDate ;

        /** 缓存内容最后一次修改时间 */
        public long lastModified ;

        /** Request的缓存过期时间 */
        public long ttl ;

        /** request的缓存新鲜时间 */
        public long softTtl ;

        /** Http响应Headers */
        public Map<String ,String> responseHeaders = Collections.emptyMap();

        /** 判断缓存内容是否过期 */
        public boolean isExpired(){
            return this.ttl < System.currentTimeMillis();
        }

        /** 判断缓存是否新鲜，不新鲜的缓存需要发到服务端做新鲜度检测 */
        public boolean refreshNeeded(){
            return this.softTtl < System.currentTimeMillis();
        }
    }
}
