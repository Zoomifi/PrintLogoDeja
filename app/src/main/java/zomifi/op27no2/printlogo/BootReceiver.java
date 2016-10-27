package zomifi.op27no2.printlogo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by CristMac on 1/10/16.
 */
public class BootReceiver extends BroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            intent = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    60000,
                    AlarmManager.INTERVAL_DAY, alarmIntent);

        }

       /* if(intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")){
            System.out.println("ONREPLACED RECEIVED");
            Uri data = intent.getData();
            if (data.toString().equals("package:" + "zomifi.op27no2.printlogo")){
                // action to do
                System.out.println("ONREPLACED RECEIVED NUMBER 2");
                // Set the alarm here.
                alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                intent = new Intent(context, AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        60000,
                        AlarmManager.INTERVAL_DAY , alarmIntent);
            }
        }*/




    }

// disable broadcast receiver code
   /*ComponentName receiver = new ComponentName(context, BootReceiver.class);
    PackageManager pm = context.getPackageManager();

    pm.setComponentEnabledSetting(receiver,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    PackageManager.DONT_KILL_APP);*/

}
