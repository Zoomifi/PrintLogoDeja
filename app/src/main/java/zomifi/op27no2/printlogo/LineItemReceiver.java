package zomifi.op27no2.printlogo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by CristMac on 11/12/15.
 */
public class LineItemReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edt;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
/*
        sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
*/
        edt = sharedPreferences.edit();

        String action = intent.getAction();
        if (action.equals("com.clover.intent.action.LINE_ITEM_ADDED")) {
            String orderId = intent.getStringExtra("com.clover.intent.extra.ORDER_ID");
            String lineItemId = intent.getStringExtra("com.clover.intent.extra.LINE_ITEM_ID");
            String itemId = intent.getStringExtra("com.clover.intent.extra.ITEM_ID");
            edt.putString("recentItemId", itemId);
            edt.putString("recentOrderId", orderId);
            edt.putString("recentLineItemId", lineItemId);
            edt.putBoolean("selected", true);
            edt.commit();

        }

    }







}