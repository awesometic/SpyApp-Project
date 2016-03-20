package com.example.ydg.spyapp;

import android.util.Log;

/**
 * Created by awesometic on 16. 3. 20.
 *
 * This is for hooking libraries using NDK
 */
public class HOOK {
    public final static String LogTag = "checking_hook";
    public native String firstMessage();

    static {
        System.loadLibrary("hook");
    }

    public HOOK() {
        Log.i(LogTag, firstMessage());
    }
}
