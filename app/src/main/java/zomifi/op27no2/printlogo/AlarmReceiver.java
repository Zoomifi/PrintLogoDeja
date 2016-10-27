package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.clover.sdk.v1.merchant.MerchantConnector;

/**
 * Created by CristMac on 1/10/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private Account account;
    private MerchantConnector merchantConnector;
    private String mercID;
    private Context mContext;
    private Boolean proceed = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, AlertActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);


    }
}

