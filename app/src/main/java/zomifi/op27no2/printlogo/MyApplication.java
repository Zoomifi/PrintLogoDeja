package zomifi.op27no2.printlogo;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends android.app.Application {
    private static Context context;

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        System.out.println("APPLICATION CALLED");

        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());


    }


    public static Context getAppContext() {
        return MyApplication.context;
    }



}
