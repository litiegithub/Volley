package com.autolink.rxjavasamples.volley;

import android.content.Intent;

/**
 * Created by Administrator on 2018/5/10.
 */

public class AuthFailureError extends VolleyError {
    private Intent mResolutionIntent ;

    public AuthFailureError(){

    }

    public AuthFailureError(Intent intent){
        mResolutionIntent = intent ;
    }

    public AuthFailureError(NetworkResponse response){
        super(response);
    }
    public AuthFailureError(String message){
        super(message);
    }

    public AuthFailureError(String message , Exception reason){
        super(message , reason);
    }

    public Intent getmResolutionIntent() {
        return mResolutionIntent;
    }

    public String getMessage() {
        if(mResolutionIntent != null){
            return "User needs to (re)enter credentials.";
        }
        return super.getMessage();
    }
}
