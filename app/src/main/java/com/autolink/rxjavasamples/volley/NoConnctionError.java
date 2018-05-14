package com.autolink.rxjavasamples.volley;

/**
 * Created by Administrator on 2018/5/14.
 */
@SuppressWarnings("unuesd")
public class NoConnctionError extends NetworkError {
    public NoConnctionError(){
        super();
    }

    public NoConnctionError(Throwable reason){
        super(reason);
    }
}
