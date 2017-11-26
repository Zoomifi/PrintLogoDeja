package zomifi.op27no2.printlogo;

import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class Application extends android.app.Application {

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("APPLICATION CALLED");

        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());

    }
}
