package com.autolink.rxjavasamples.volley.toolbox;

import android.content.Context;

import com.autolink.rxjavasamples.volley.Network;
import com.autolink.rxjavasamples.volley.RequestQueue;

import java.io.File;

/**
 * Created by Administrator on 2018/5/9.
 */
@SuppressWarnings("unused")
public class Volley {
    private static final String DEFAULT_CACHE_DIR = "volley";

    public static RequestQueue newRequestQueue(Context context){
        return newRequestQueue(context , null);
    }

    private static RequestQueue newRequestQueue(Context context ,HttpStack stack){
        File cacheDir = new File(context.getCacheDir() , DEFAULT_CACHE_DIR);
        if(stack == null){
            stack = new HurlStack();
        }

        Network network = new BasicNetwork(stack);
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir) ,network);
        queue.start();
        return queue ;
    }
}
