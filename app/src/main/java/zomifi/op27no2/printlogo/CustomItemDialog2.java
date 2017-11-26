package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.Platform;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomItemDialog2 extends Dialog implements View.OnClickListener
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private Context context;
    private int mode;
    private int page;
    private int position;

    private String itemName = "";
    private String itemPrice1 = "";
    private String itemPrice2 = "";
    private String itemPrice3 = "";

    private EditText nameEdit;
    private CheckBox checkBox;
    private Spinner  categorySpinner;
    private String   spinnerValue;

    private Long holdPrice;

    private Button submitButton;
    Button amountButtons[] = new Button[3];


    private Boolean isEditing;

    public static final int MAIN_ACTIVITY_INSTRUCTIONS          = 1001;
    public static final int CUSTOMER_FACING_SIDE_INVALID_INPUT  = 1002;
    public static final int RECEIPT_ACTIVITY                    = 1003;

    private String myID;

    private int dialogFrom = 1001;

    public CustomItemDialog2(Context context, int position, int page, int mode)
    {
        super(context);

        this.context = context;
        this.position = position;
        this.page = page;
        this.mode = mode;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom_item);
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }
        myID = prefs.getString(mode + "_" + page + "button" + position + "_id", "");
        System.out.println("retrieved: "+mode + "_" + page + "button" + position + "_id"+" "+myID);


        nameEdit = (EditText) findViewById(R.id.customNameEdit);

        if(!prefs.getString(myID + "_name", "Click to Add").equals("Click to Add")) {
            nameEdit.setText(prefs.getString(myID + "_name", ""));
        }
        else{
            nameEdit.setText("");
        }
        for(int i=0; i<3; i++) {
            int resID = context.getResources().getIdentifier("amountbutton"+(i), "id", "zomifi.op27no2.printlogo");
            amountButtons[i] = ((Button) findViewById(resID));
            amountButtons[i].setText(formatPrice(prefs.getString(myID + "_price"+(i+1), "0")));
            amountButtons[i].setOnClickListener(this);
        }
        //default item prices to the
        itemPrice1 = prefs.getString(myID + "_price1", "0");
        itemPrice2 = prefs.getString(myID + "_price2", "0");
        itemPrice3 = prefs.getString(myID + "_price3", "0");



        // set whether mini/mobile or station
        if(Platform.isCloverMobile() || Platform.isCloverMini()){

        }
        else if(Platform.isCloverStation()){

        }

        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        spinnerValue = prefs.getString(myID + "_category","Vu Cash");
        selectValue(categorySpinner, spinnerValue);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                spinnerValue = categorySpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        submitButton = (Button) findViewById(R.id.customDialogSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameEdit.getText().toString().equals("")){
                    String id = prefs.getString("recentItemId", "default");
                    edt.putString(mode + "_" + page + "button" + position + "_id", id);
                    edt.putString(myID + "_name", nameEdit.getText().toString());
                    edt.putString(myID + "_price1", itemPrice1);
                    edt.putString(myID + "_price2", itemPrice2);
                    edt.putString(myID + "_price3", itemPrice3);
                    edt.putString(myID + "_category", spinnerValue);
                    edt.commit();

                   /* try {
                        DB snappydb = DBFactory.open(context); //create or open an existing database using the default name

                        snappydb.put(myID + "_name", nameEdit.getText().toString());
                        snappydb.put(myID + "_price1", itemPrice1);
                        snappydb.put(myID + "_price2", itemPrice2);
                        snappydb.put(myID + "_price3", itemPrice3);
                        snappydb.put(myID + "_category", spinnerValue);

                        snappydb.close();

                    } catch (SnappydbException e) {
                        System.out.println("snappy error: "+e.getMessage());
                    }*/

                    dismiss();
                    ((SetupActivity) context).onResume();

                }else{
                    Toast.makeText(context, "Must Enter Item Name", Toast.LENGTH_LONG).show();
                }

            }
        });

        checkBox = (CheckBox) findViewById(R.id.vcash_checkbox);
        checkBox.setText("Include Bar V-Cash "+prefs.getInt("fee_amount",10)+"% fee?:");
        checkBox.setChecked(prefs.getBoolean(myID + "_isvcash", false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edt.putBoolean(myID+ "_isvcash", isChecked);
                edt.commit();
            }
        });


        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        findViewById(R.id.customDialogCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((SetupActivity) context).onResume();
            }
        });

        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (5 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @NonNull
    private String formatPrice(String PRICE_STRING) {

        //test format code
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
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


    @Override
    public void onClick(View v) {
        for(int i=0;i<3;i++) {
        if (v.getId() == amountButtons[i].getId()) {

            final int finalI = i;
            CustomPriceEnteredListener cc = new CustomPriceEnteredListener() {
                @Override
                public void setPrice(String orderID, String name, int mode, long price, Boolean isPayment) {
                    amountButtons[finalI].setText(formatPrice(Long.toString(price)));
                    switch (mode) {
                        case 1:
                            itemPrice1 = Long.toString(price);
                            break;
                        case 2:
                            itemPrice2 = Long.toString(price);
                            break;
                        case 3:
                            itemPrice3 = Long.toString(price);
                            break;
                    }
                }

                @Override
                public void changeButton(int buttonID, String PRICE_STRING) {

                }
            };
            LineItemPriceSetter lineItemPriceSetter = new LineItemPriceSetter(context, (i+1), false);
            lineItemPriceSetter.setCustomPriceEnteredListener(cc);
            lineItemPriceSetter.setButtonIndex(6);
            lineItemPriceSetter.setHeader("Set Dollar Amount");
            lineItemPriceSetter.show();

        }
        }

    }




}
