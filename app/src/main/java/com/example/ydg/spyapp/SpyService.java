package com.example.ydg.spyapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;
import android.util.Log;

/**
 * Created by ydg on 2015-02-17.
 * Spy Service 클래스
 */
public class SpyService extends Service {

    public final static String LogTag = "checking_spyservice";
    boolean isOn;

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(LogTag, "Service start");
        SpyServiceThread spyThread = new SpyServiceThread(this, mHandler);
        spyThread.start();
        isOn = true;

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        Log.i(LogTag, "Service end");
        Toast.makeText(this, "Service End", Toast.LENGTH_SHORT).show();
        isOn = false;
    }

    class SpyServiceThread extends Thread {
        SpyService mParent;
        Handler mHandler;

        public SpyServiceThread(SpyService parent, Handler handler) {
            mParent = parent;
            mHandler = handler;
        }

        public void run() {
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                String strings = (String)msg.obj;
                Toast.makeText(SpyService.this, strings, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
