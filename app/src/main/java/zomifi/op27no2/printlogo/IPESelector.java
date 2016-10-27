package zomifi.op27no2.printlogo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by CristMac on 7/11/16.
 */
public class IPESelector {
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor edt;
    public Context mContext;

    public IPESelector(Context context){
        this.mContext = context;
        sharedPreferences = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = sharedPreferences.edit();
    }

    public void showEmployee(String name, String uniqueID, int position){
        edt.putString("name" + position, name);
        edt.putString("uniqueID" + position, uniqueID);
        edt.putBoolean("full"+position, true);
        edt.putBoolean("active"+position, true);
        edt.commit();
    }

    public void clearPosition(int position){
        edt.putString("name" + position, "Click to Add");
        edt.putString("uniqueID" + position, "");
        edt.putBoolean("full"+position, false);
        edt.putBoolean("active"+position, false);
        edt.commit();
    }

}
