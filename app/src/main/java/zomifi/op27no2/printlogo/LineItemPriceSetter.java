package zomifi.op27no2.printlogo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Andrew on 1/16/2016.
 * Used for Delivery Fee application for Zoomifi Inc.
 */

public class LineItemPriceSetter extends Dialog implements View.OnClickListener
{
    private Context context;
    private int                         buttonIndex = 0;
    private int                         mode;
    private String PRICE_STRING = "";
    private long                        PRICE = 0;
    private CustomPriceEnteredListener customPriceEnteredListener;
    private String headerString = "Enter Dollar Amount";
    private boolean                     fromCustomer = false;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private Handler handler = new Handler();
    private Runnable runnable;
    private SharedPreferences sharedPreferences;
    private Boolean isPayment;

    private Button addButton;
    /**
     * Constructor.
     * @param context: Parent's context (from MainActivity or LineItemService.)
     */

    public LineItemPriceSetter(Context context, int mode, Boolean isPayment)
    {
        super(context);

        this.context = context;
        this.mode = mode;
        this.isPayment = isPayment;
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
        setContentView(zomifi.op27no2.printlogo.R.layout.dialog_set_price);
        sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        addButton = (Button) findViewById(R.id.addButton);

        // Configure window size.
        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (6 * height) / 7);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _KEY = "";
                switch (buttonIndex) {
                    case 1:
                        _KEY = context.getString(zomifi.op27no2.printlogo.R.string.donation_button1_price);
                        break;
                    case 2:
                        _KEY = context.getString(zomifi.op27no2.printlogo.R.string.donation_button2_price);
                        break;
                    case 3:
                        _KEY = context.getString(zomifi.op27no2.printlogo.R.string.donation_button3_price);
                        break;
                    case 4:
                        _KEY = context.getString(zomifi.op27no2.printlogo.R.string.donation_button4_price);
                        break;
                    case 5:
                        _KEY = context.getString(zomifi.op27no2.printlogo.R.string.donation_button5_price);
                        break;
                    default:
                        customPriceEnteredListener.setPrice(null,mode,PRICE, isPayment);
                        dismiss();
                        return;
                }

                if (PRICE > 0 && PRICE_STRING.length() > 0) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putLong(_KEY, PRICE);
                    editor.commit();

                    customPriceEnteredListener.changeButton(buttonIndex, PRICE_STRING);

                    dismiss();
                }

            }
        });

        ((TextView) findViewById(zomifi.op27no2.printlogo.R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, zomifi.op27no2.printlogo.R.color.Gray1));
        Button button = (Button)findViewById(zomifi.op27no2.printlogo.R.id.addButton);
        button.setEnabled(false);
        button.setAlpha(0.5f);
        System.out.println("disabled");

        findViewById(zomifi.op27no2.printlogo.R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Set ClickListeners

        findViewById(zomifi.op27no2.printlogo.R.id._1)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._2)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._3)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._4)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._5)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._6)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._7)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._8)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._9)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._0)       .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._00)      .setOnClickListener(this);
        findViewById(zomifi.op27no2.printlogo.R.id._DELETE)  .setOnClickListener(this);

        ((TextView)findViewById(zomifi.op27no2.printlogo.R.id.header)).setText(headerString);
    }

    /**
     * Set the interface for communication between the Dialog and Parent calling class.
     * @param customPriceEnteredListener : Interface for communication.
     */

    public void setCustomPriceEnteredListener(CustomPriceEnteredListener customPriceEnteredListener)
    {
        this.customPriceEnteredListener = customPriceEnteredListener;
    }

    public void setFromCustomer(boolean fromCustomer)
    {
        this.fromCustomer = fromCustomer;
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

            if(v.getId() != zomifi.op27no2.printlogo.R.id._DELETE)
                return;
        }

        // Determine which button was clicked on let control flow from there.

        switch(v.getId())
        {
            case zomifi.op27no2.printlogo.R.id._1:
                this.PRICE_STRING += String.valueOf(1);
                break;
            case zomifi.op27no2.printlogo.R.id._2:
                this.PRICE_STRING += String.valueOf(2);
                break;
            case zomifi.op27no2.printlogo.R.id._3:
                this.PRICE_STRING += String.valueOf(3);
                break;
            case zomifi.op27no2.printlogo.R.id._4:
                this.PRICE_STRING += String.valueOf(4);
                break;
            case zomifi.op27no2.printlogo.R.id._5:
                this.PRICE_STRING += String.valueOf(5);
                break;
            case zomifi.op27no2.printlogo.R.id._6:
                this.PRICE_STRING += String.valueOf(6);
                break;
            case zomifi.op27no2.printlogo.R.id._7:
                this.PRICE_STRING += String.valueOf(7);
                break;
            case zomifi.op27no2.printlogo.R.id._8:
                this.PRICE_STRING += String.valueOf(8);
                break;
            case zomifi.op27no2.printlogo.R.id._9:
                this.PRICE_STRING += String.valueOf(9);
                break;
            case zomifi.op27no2.printlogo.R.id._0:
                if(PRICE != 0 && PRICE_STRING.length() > 0)
                    this.PRICE_STRING += String.valueOf(0);
                break;
            case zomifi.op27no2.printlogo.R.id._00:
                if(PRICE > 0 && PRICE_STRING.length() > 0) {
                    this.PRICE_STRING = "-" + PRICE_STRING;
                }
                else if(PRICE < 0 && PRICE_STRING.length() > 0) {
                    this.PRICE_STRING = PRICE_STRING.substring(1);
                }
                break;
            case zomifi.op27no2.printlogo.R.id._DELETE:
                if(this.PRICE_STRING.length() > 0)
                    this.PRICE_STRING = this.PRICE_STRING.substring(0, this.PRICE_STRING.length() - 1);
                break;
        }

        if(this.PRICE_STRING.length() > 0)
            this.PRICE = Long.parseLong(this.PRICE_STRING);
        else
            this.PRICE = 0;

        ((TextView) findViewById(zomifi.op27no2.printlogo.R.id.TOTAL)).setText(formatPrice());

       /* if(this.PRICE == 0)
        {
            ((TextView) findViewById(zomifi.op27no2.printlogo.R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, zomifi.op27no2.printlogo.R.color.Gray1));
            Button button = (Button)findViewById(zomifi.op27no2.printlogo.R.id.addButton);
            button.setEnabled(true);
            button.setAlpha(0.5f);
            System.out.println("disabled");
        }*/
        /*else */if(!findViewById(zomifi.op27no2.printlogo.R.id.addButton).isEnabled())
        {
            ((TextView) findViewById(zomifi.op27no2.printlogo.R.id.TOTAL)).setTextColor(ContextCompat.getColor(this.context, zomifi.op27no2.printlogo.R.color.Black));
            Button button = (Button)findViewById(zomifi.op27no2.printlogo.R.id.addButton);
            button.setEnabled(true);
            button.setAlpha(1.0f);
            System.out.println("enabled");
        }
    }

    /**
     * Format the price from the long value to USD display.
     * @return : The formatted price as a String.
     */

    @NonNull
    private String formatPrice()
    {
        //test format code
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        //default USD from prefs
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));

        if(PRICE_STRING.isEmpty())
            PRICE_STRING = "0";

        long value = Long.valueOf(PRICE_STRING);
        String price = mCurrencyFormat.format(value / 100.0);
        return price;

    }

    public void setHeader(String headerString)
    {
        this.headerString = headerString;
    }


}
