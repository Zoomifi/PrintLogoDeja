package zomifi.op27no2.printlogo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.clover.sdk.util.CustomerMode;

public class SettingsActivity extends Activity implements View.OnClickListener, CustomManagerListener
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Button customizeButton;
    private Button managerButton;
    private Button backButton;
    private Switch mSwitch;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Button homeButton;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(zomifi.op27no2.printlogo.R.layout.activity_settings);
        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        mContext = this;

        managerButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.manager_button);
        managerButton.setOnClickListener(this);
        customizeButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.customizebutton);
        backButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.gobackbutton);
        mSwitch = (Switch) findViewById(zomifi.op27no2.printlogo.R.id.mySwitch);

        homeButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(SettingsActivity.this, NavigationActivity.class);
                SettingsActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                CustomerMode.disable(mContext);

            }
        });


        customizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(SettingsActivity.this, SetupActivity.class);
                SettingsActivity.this.startActivity(myIntent);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
        mSwitch.setChecked(prefs.getBoolean("showMultiple", true));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    edt.putBoolean("showMultiple", true);
                    edt.commit();
                } else {
                    edt.putBoolean("showMultiple", false);
                    edt.commit();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.manager_button) {
            System.out.println("clicked");
            CustomManagerDialog managerDialog = new CustomManagerDialog(mContext, true);
            managerDialog.setCustomManagerListener(this);
            managerDialog.show();
        }
    }

    @Override
    protected void onPause() {

        System.out.println("OnPause Called");
       /* if (LOCKED) {
            Intent i = new Intent(this, CustomerActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }*/


        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();



    }



    @NonNull
    private String formatPrice(String PRICE_STRING)
    {
        StringBuilder builder = new StringBuilder(PRICE_STRING);

        if(builder.length() == 0)
            builder = new StringBuilder("$0.00");
        else if(builder.length() == 1)
            builder.insert(0, "$0.0");
        else if(builder.length() == 2)
            builder.insert(0, "$0.");
        else if(builder.length() > 5)
        {
            builder.insert(0, "$");
            builder.insert(builder.length() - 5, ",");
            builder.insert(builder.length() - 2, ".");
        } else {
            builder.insert(0, "$");
            builder.insert(builder.length() - 2, ".");
        }

        return builder.toString();
    }

/*

    @Override
    public void setPrice(String orderID, int mode, long price, Boolean isPayment) {

    }

    @Override
    public void changeButton(int buttonID, String PRICE_STRING) {

    }
*/


    @Override
    public void managerCallback(int resourceID) {

    }
}
