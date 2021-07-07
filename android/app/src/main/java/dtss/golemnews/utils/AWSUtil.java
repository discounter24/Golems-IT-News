package dtss.golemnews.utils;

import android.util.Log;

import dtss.golemnews.MainActivity;

public class AWSUtil {


    static int calls = 0;
    public static String acceptCookieLink(String link){
        calls++;
        Log.d("AWSCallIncrease",Integer.toString(calls));
        return "https://xmzwpokmv2.execute-api.eu-central-1.amazonaws.com/default/golemAcceptCookies?url=" + link;
    }
}
