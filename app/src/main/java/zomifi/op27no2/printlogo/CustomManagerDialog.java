package zomifi.op27no2.printlogo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Andrew on 1/16/2016.
 * Used for Delivery Fee application for Zoomifi Inc.
 */

public class CustomManagerDialog extends Dialog implements View.OnClickListener
{
    private Context context;
    private int                         buttonIndex = 0;
    private String PRICE_STRING = "";
    private long                        PRICE = 0;
    public CustomManagerListener customManagerListener;
    private String headerString = "Manager Code Required";
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Boolean fromSettings;
    private int resourceID;
    /**
     * Constructor.
     * @param context: Parent's context (from MainActivity or LineItemService.)
     */

    public CustomManagerDialog(Context context, Boolean fromSettings)
    {
        super(context);

        this.context = context;
        this.fromSettings = fromSettings;
    }

    /**
     * Initialize Dialog buttons and basic setup.
     * @param savedInstanceState : Bundle for
     */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_manager);
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

        if(fromSettings){
            if(prefs.getBoolean("first_password", true) == true){
                setHeader("Set Code");
            }
            else{
                setHeader("Enter Code to Change");
            }

        }

        // Configure window size.
        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (6 * height) / 7);

        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((fromSettings == true) && (prefs.getBoolean("first_password", true) == true)) {
                    edt.putString("manager_code",PRICE_STRING);
                    edt.putBoolean("first_password", false);
                    edt.commit();
                    dismiss();
                }
                else if((fromSettings == true) && PRICE_STRING.equals(prefs.getString("manager_code",""))){
                    // user has entered correct code to enable change,
                    setHeader("Set Code");
                    edt.putBoolean("first_password", true);
                    edt.commit();

                }
                else if(prefs.getBoolean("first_password", true) == true){
                    customManagerListener.managerCallback(resourceID);
                    dismiss();
                }
                // successful code entered
                else if(PRICE_STRING.equals(prefs.getString("manager_code",""))){

                    customManagerListener.managerCallback(resourceID);
                   /* Intent myIntent = new Intent(context, NavigationActivity.class);
                    context.startActivity(myIntent);
                    CustomerMode.disable(context);*/
                    dismiss();

                }
                else{
                    dismiss();
                }


               // customSecondsEnteredListener.changeButton(PRICE_STRING);

            }
        });

        ((TextView) findViewById(R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, R.color.Gray1));
        Button button = (Button)findViewById(R.id.addButton);
        button.setEnabled(false);
        button.setAlpha(0.5f);

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Set ClickListeners

        findViewById(R.id._1)       .setOnClickListener(this);
        findViewById(R.id._2)       .setOnClickListener(this);
        findViewById(R.id._3)       .setOnClickListener(this);
        findViewById(R.id._4)       .setOnClickListener(this);
        findViewById(R.id._5)       .setOnClickListener(this);
        findViewById(R.id._6)       .setOnClickListener(this);
        findViewById(R.id._7)       .setOnClickListener(this);
        findViewById(R.id._8)       .setOnClickListener(this);
        findViewById(R.id._9)       .setOnClickListener(this);
        findViewById(R.id._0)       .setOnClickListener(this);
        findViewById(R.id._DELETE)  .setOnClickListener(this);

        ((TextView)findViewById(R.id.header)).setText(headerString);


        if(prefs.getBoolean("first_password", true) == true) {
            Button buttonn = (Button) findViewById(R.id.addButton);
            buttonn.setEnabled(true);
            buttonn.setAlpha(1.0f);
        }

    }

    /**
     * Set the interface for communication between the Dialog and Parent calling class.
     * @param customManagerListener : Interface for communication.
     */

    public void setCustomManagerListener(CustomManagerListener customManagerListener)
    {
        this.customManagerListener = customManagerListener;
    }


    /**
     * Set the Button ID for use within this class (internal).
     * @param ID : Internal button ID of the selected Button.
     */

    public void setButtonIndex(int ID)
    {
        this.buttonIndex = ID;
    }

    /**
     * Write the new price for whichever default button the user selected.
     * @param buttonID : Internal ID for the button the user clicked.
     * @param price : New price input.
     */

    private void saveDefaultPrice(int buttonID, long price)
    {
        //SharedPreferences sharedPreferences = this.context.getSharedPreferences(this.context.getString(R.string.FILE), Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedPreferences.edit();

        /*switch(buttonID)
        {
            case DeliveryFeeContract.BUTTON_ID_1:
                editor.putLong(this.context.getString(R.string.DefaultPriceButton1), price);
                break;
            case DeliveryFeeContract.BUTTON_ID_2:
                editor.putLong(this.context.getString(R.string.DefaultPriceButton2), price);
                break;
        }

        editor.commit();

        this.customPriceEnteredListener.changeButton(this.buttonID, formatPrice());*/
    }

    /**
     * Callback method which handles the user input for the price entered,
     * @param v : View used as reference.
     */

    @Override
    public void onClick(View v)
    {
        // The entered price cannot be greater then $99,999.99 (This is the limit clover imposed).

        if(this.PRICE_STRING.length() > 6)
        {
            // The EU can click the delete button at this point. No other numbers can be entered
            // though.

            if(v.getId() != R.id._DELETE)
                return;
        }

        // Determine which button was clicked on let control flow from there.

        switch(v.getId())
        {
            case R.id._1:
                this.PRICE_STRING += String.valueOf(1);
                break;
            case R.id._2:
                this.PRICE_STRING += String.valueOf(2);
                break;
            case R.id._3:
                this.PRICE_STRING += String.valueOf(3);
                break;
            case R.id._4:
                this.PRICE_STRING += String.valueOf(4);
                break;
            case R.id._5:
                this.PRICE_STRING += String.valueOf(5);
                break;
            case R.id._6:
                this.PRICE_STRING += String.valueOf(6);
                break;
            case R.id._7:
                this.PRICE_STRING += String.valueOf(7);
                break;
            case R.id._8:
                this.PRICE_STRING += String.valueOf(8);
                break;
            case R.id._9:
                this.PRICE_STRING += String.valueOf(9);
                break;
            case R.id._0:
                if(PRICE != 0 && PRICE_STRING.length() > 0)
                    this.PRICE_STRING += String.valueOf(0);
                break;
            case R.id._00:
                if(PRICE != 0 && PRICE_STRING.length() > 0)
                    this.PRICE_STRING += "00";
                break;
            case R.id._DELETE:
                if(this.PRICE_STRING.length() > 0)
                    this.PRICE_STRING = this.PRICE_STRING.substring(0, this.PRICE_STRING.length() - 1);
                break;
        }

        if(this.PRICE_STRING.length() > 0)
            this.PRICE = Long.parseLong(this.PRICE_STRING);
        else
            this.PRICE = 0;

        ((TextView) findViewById(R.id.TOTAL)).setText(formatPrice());

        if(this.PRICE == 0)
        {
            ((TextView) findViewById(R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, R.color.Gray1));
            Button button = (Button)findViewById(R.id.addButton);
            button.setEnabled(false);
            button.setAlpha(0.5f);
        }
        else if(!findViewById(R.id.addButton).isEnabled())
        {
            ((TextView) findViewById(R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, R.color.Black));
            Button button = (Button)findViewById(R.id.addButton);
            button.setEnabled(true);
            button.setAlpha(1.0f);
        }

        if(prefs.getBoolean("first_password", true) == true) {
            Button button = (Button) findViewById(R.id.addButton);
            button.setEnabled(true);
            button.setAlpha(1.0f);
        }
    }

    /**
     * Format the price from the long value to USD display.
     * @return : The formatted price as a String.
     */

    @NonNull
    private String formatPrice()
    {
       StringBuilder builder = new StringBuilder(this.PRICE_STRING);

        return builder.toString();
    }

    public void setHeader(String headerString)
    {
        this.headerString = headerString;
        ((TextView)findViewById(R.id.header)).setText(headerString);
    }

    public void setButtonID(int buttonID){
        resourceID = buttonID;
    }

}
