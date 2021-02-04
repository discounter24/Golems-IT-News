package dtss.golemnews.utils;

public class AWSUtil {


    public static String acceptCookieLink(String link){
        return "https://xmzwpokmv2.execute-api.eu-central-1.amazonaws.com/default/golemAcceptCookies?url=" + link;
    }
}
