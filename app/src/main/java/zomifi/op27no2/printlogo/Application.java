package zomifi.op27no2.printlogo;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class Application extends android.app.Application {

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("APPLICATION CALLED");
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "4HEcYf3QM7WTNaCV5cdx4igevMjGCstzaQTgKaLQ", "UKvJxHiksfKfNNMPGiW9KtyH7g7NeTBCbHJX30y7");

    }
}
