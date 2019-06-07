package dtss.golemnews.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;

import dtss.golemnews.R;

public abstract class ThemeUtils {


    public static  SharedPreferences sharedPreferences;


    public static boolean isSystemControlled(){
        if (sharedPreferences == null) return  true;

        return sharedPreferences.getString("appThemePref", "system").equalsIgnoreCase("system");
    }

    public static boolean isNightMode(Activity activity){
        if (sharedPreferences == null) return false;
        String appTheme = sharedPreferences.getString("appThemePref", "system");
        if (appTheme.equalsIgnoreCase("dark")){
            return true;
        } else if (appTheme.equalsIgnoreCase("light")){
            return false;
        } else {
            int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    return false;
                case Configuration.UI_MODE_NIGHT_YES:
                    return true;
            }
        }
        return false;


    }


    public static void updateTheme(Activity activity){
        if (isNightMode(activity)){
            activity.setTheme(R.style.AppTheme_AppThemeDark);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

}
