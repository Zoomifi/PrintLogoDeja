package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomCashDialog extends Dialog
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private Order mOrder;
    private OrderConnector orderConnector;
    private String orderID;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private LinearLayout feeText;
    private String  headerText;
    private String  bodyText;
    private Context context;
    private int position;
    private int page;

    private String  orderId = "";
    private String  activity;


    private TextView receivedText;
    private TextView totalText;
    private TextView changeText;
    private Long received;
    private Long total;
    private Long change;

    private Button submitButton;
    private Button amountButton;

    private Boolean isEditing;

    public static final int MAIN_ACTIVITY_INSTRUCTIONS          = 1001;
    public static final int CUSTOMER_FACING_SIDE_INVALID_INPUT  = 1002;
    public static final int RECEIPT_ACTIVITY                    = 1003;

    private int dialogFrom = 1001;

    public CustomCashDialog(String activity,Context context, long received,long total)
    {
        super(context);

        this.context = context;
        this.received = received;
        this.total = total;
        this.activity = activity;


    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom_cash);
        prefs = context.getSharedPreferences(context.getString(R.string.donation_here_file), Context.MODE_PRIVATE);
        edt = prefs.edit();
        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }
        FrameLayout fullLayout = (FrameLayout) findViewById(R.id.full_layout);
        fullLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //show dialog here
                System.out.println("timer setup");
         //       ((MainActivity) context).stopTimeout();
         //       ((MainActivity) context).startTimeout();

                return false;
            }
        });

        // set whether mini/mobile or station
        if(Platform.isCloverMobile() || Platform.isCloverMini()){

        }
        else if(Platform.isCloverStation()){

        }

        receivedText = (TextView) findViewById(R.id.received_amount);
        totalText = (TextView) findViewById(R.id.total_amount);
        changeText = (TextView) findViewById(R.id.change_amount);
        receivedText.setText(formatPrice(Long.toString(received)));
        totalText.setText(formatPrice(Long.toString(total)));
        changeText.setText(formatPrice(Long.toString(received - total)));

        submitButton = (Button) findViewById(R.id.customDialogSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(activity.equals("door")) {
                    ((DoorActivity) context).onResume();
                }
                else if(activity.equals("bar")){
                    ((BarActivity) context).onResume();
                }
            //    ((MainActivity) context).clearOrder();

            }
        });

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));



        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (5 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    @NonNull
    private String formatPrice(String PRICE_STRING) {

        //test format code
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.donation_here_file), Context.MODE_PRIVATE);
        //default USD from prefs
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));

        String price = "";

            long value = Long.valueOf(PRICE_STRING);
            price = mCurrencyFormat.format(value / 100.0);




        return price;
    }
    @NonNull
    private String deformatPrice(String PRICE_STRING) {


        String price = PRICE_STRING.replaceAll("\\D+","");

        return price;
    }


}
