package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.printer.CashDrawer;
import com.clover.sdk.v1.printer.ReceiptRegistrationConnector;
import com.clover.sdk.v1.printer.job.PrintJob;
import com.clover.sdk.v1.printer.job.StaticBillPrintJob;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BarActivity extends Activity implements View.OnClickListener, CustomPriceEnteredListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edt;
    private MerchantConnector merchantConnector;
    private EmployeeConnector mEmployeeConnector;
    private OrderConnector orderConnector;
    private CustomerConnector customerConnector;
    private InventoryConnector inventoryConnector;
    private Context mContext;
    private Account account;
    private Order mOrder;
    private Handler handler = new Handler();
    private Runnable runnable;

    private LinearLayout multiplesLayout;
    private LinearLayout ordersLayout;
    private String PRICE_STRING = "";
    private String mOrderId;
    private TextView titleText1;
    private TextView titleText2;

    private ProgressDialog progressDialog;
    private long donationAmount = 0;
    private int TRIGGER = 0;
    private int RESULT_CODE = 2;
    private int mMultiplicity = 1;

    private static final int PAY_REQUEST_CODE = 1;
    private static final int ORDER_REQUEST_CODE = 3;
    private int mOrderMode = 3;
    private Boolean LOCKED = false;
    private Boolean isChange = false;

    private boolean isOpened = false;
    private boolean building = false;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private Button orderButton;
    private Button payButton;
    private Button changeButton;

    Button multipleButtons[] = new Button[20];
    Button myButtons[] = new Button[16];

    private ArrayList<ArrayList<Integer>> lineItems = new ArrayList<ArrayList<Integer>>();

    private ImageButton bRight;
    private ImageButton bLeft;
    private int cPage = 1;
    private TextView pageText;
    private ScrollView multiplesScroll;
    private FirebaseHelper mHelper;

    private Long mTimestamp;
    private Long mPayment;
    private Long mTotal;
    private Map<String, OrderItem> mItems;
    private Boolean customItemPresent = false;
    private ReceiptRegistrationConnector receiptConnector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(zomifi.op27no2.printlogo.R.layout.activity_bar);
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = sharedPreferences.edit();
        mContext = this;
        TRIGGER = 0;
        mHelper = new FirebaseHelper(this);
        mHelper.initialize();

        multiplesLayout = (LinearLayout) findViewById(zomifi.op27no2.printlogo.R.id.multiplicity);
        pageText = (TextView) findViewById(zomifi.op27no2.printlogo.R.id.pages);
        titleText1 = (TextView) findViewById(R.id.title_text1);
        titleText2 = (TextView) findViewById(R.id.title_text2);


        orderButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.orderbutton);
        payButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.pay_button);
    //    changeButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.change_button);
        multiplesScroll = (ScrollView) findViewById(R.id.multiplesscroll);

        if(sharedPreferences.getBoolean("showMultiple", true)){
            multiplesLayout.setVisibility(View.VISIBLE);
        }
        else{
            multiplesLayout.setVisibility(View.GONE);
        }

        bRight = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bright);
        bLeft = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bleft);

        for(int i=0; i<20; i++) {
            int resID = getResources().getIdentifier("mbutton"+(i+1), "id", "zomifi.op27no2.printlogo");
            multipleButtons[i] = ((Button) findViewById(resID));
            multipleButtons[i].setOnClickListener(this);
        }
        for(int i=0; i<16; i++) {
            int resID = getResources().getIdentifier("button"+(i+1), "id", "zomifi.op27no2.printlogo");
            myButtons[i] = ((Button) findViewById(resID));
            myButtons[i].setOnClickListener(this);
        }


        Button homeButton = (Button) findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(BarActivity.this, NavigationActivity.class);
                BarActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        orderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                CustomBarOrderListDialog customDialog = new CustomBarOrderListDialog(mContext);
                customDialog.setCancelable(false);
                customDialog.show();
            }
        });
        payButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                isChange = false;
                payIntent();

            }
        });

/*        changeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                isChange = true;
                changeIntent();

            }
        });*/


        cPage = sharedPreferences.getInt("currentPage",1);
        bLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cPage>1){
                    cPage = cPage-1;
                    edt.putInt("currentPage",cPage);
                    edt.commit();
                    setUpButtonNames(cPage);
                }

            }
        });
        bRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cPage < 10) {
                    cPage = cPage + 1;
                    edt.putInt("currentPage", cPage);
                    edt.commit();
                    setUpButtonNames(cPage);
                }
            }
        });

        setUpButtonNames(cPage);
        mMultiplicity = sharedPreferences.getInt("multiplicity",1);
        setButtonColor(mMultiplicity);
        this.PRICE_STRING = "";

    }

    private void logout() {
        finishAffinity();
        mEmployeeConnector.logout(new EmployeeConnector.EmployeeCallback<Void>() {
            @Override
            public void onServiceSuccess(Void result, ResultStatus status) {
                super.onServiceSuccess(result, status);
                System.out.println("logged out");
            }

            @Override
            public void onServiceFailure(ResultStatus status) {
                super.onServiceFailure(status);
                System.out.println("logout failure");
            }

            @Override
            public void onServiceConnectionFailure() {
                super.onServiceConnectionFailure();
                System.out.println("connection failure");
            }
        });
        disconnect();

    }

    private void setUpButtonNames(int currentPage) {
        pageText.setText("Page " + currentPage);
        for(int i=0; i<15;i++){
            String id = sharedPreferences.getString(2+"_"+currentPage+"button"+(i+1)+"_id", "");
            myButtons[i].setText(sharedPreferences.getString(id + "_name", "Add from Settings") + "\n" + formatPrice(sharedPreferences.getString(id + "_price" + 2, "")));
            myButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);

       //     myButtons[i].setText(sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_name", "Add from Settings") + "\n" + sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_price", ""));
        }
    }

    @NonNull
    private String formatPrice(String PRICE_STRING) {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));
        String price = "";
        if(!PRICE_STRING.equals("")) {
            long value = Long.valueOf(PRICE_STRING);
            price = mCurrencyFormat.format(value / 100.0);
        }
        return price;

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetMultiplicity();
        titleText1.setText("");
        titleText2.setText("");
        edt.putInt("multiplicity",1);
        edt.commit();


        setUpButtonNames(cPage);
        setButtonColor(mMultiplicity);

        if(sharedPreferences.getBoolean("showMultiple", true)){
            multiplesLayout.setVisibility(View.VISIBLE);
        }
        else{
            multiplesLayout.setVisibility(View.GONE);
        }

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(this);

            if (account == null) {
                Toast.makeText(this, getString(zomifi.op27no2.printlogo.R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Create and Connect
        connect();
    }

    @Override
    protected void onPause() {

       // disconnect();
        System.out.println("OnPause Called");

        super.onPause();

    }

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy called");

        super.onDestroy();

    }

    private void connect() {
        disconnect();
        if (account != null) {
            orderConnector = new OrderConnector(this, account, null);
            orderConnector.connect();

            receiptConnector = new ReceiptRegistrationConnector(this, account, new ServiceConnector.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
                    System.out.println("receipt service connected");
                    unregisterReceiptRegistration();
                    edt.putBoolean("can_print", false);
                    edt.commit();
                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
                    unregisterReceiptRegistration();
                }
            });
            receiptConnector.connect();
        }

    }

    private void disconnect() {
        if (orderConnector != null) {
            orderConnector.disconnect();
            orderConnector = null;
        }

    }

    private void setButtonColor(int num){
        for(int i=0;i<20;i++){
            if(num-1 == i) {
                multipleButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
            }
            else{
                multipleButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);
            }
        }

    }

    @Override
    public void onClick(View v)
    {
        for(int i=0; i<20; i++){
            if(v.getId() == multipleButtons[i].getId()){
                mMultiplicity = i+1;
                setButtonColor(i+1);
                edt.putInt("multiplicity", i+1);
                edt.commit();
            }
        }


        for(int i=0;i<15;i++) {
            if (v.getId() == getResources().getIdentifier("button" + (i + 1), "id", "zomifi.op27no2.printlogo")) {
                if (!myButtons[i].getText().equals("Add from Settings" + "\n" + "")) {
                    if (mOrderMode == 3) {
                        addLineItems(i + 1);
                    }

                }
            }
        }
        if(v.getId() == zomifi.op27no2.printlogo.R.id.button16)
        {
            if(!building) {
              //  building = true;
                LineItemPriceSetter lineItemPriceSetter = new LineItemPriceSetter(this,2, false);
                lineItemPriceSetter.setCustomPriceEnteredListener(this);
                lineItemPriceSetter.setButtonIndex(6);
                lineItemPriceSetter.setFromCustomer(true);
                lineItemPriceSetter.show();
            }
        }
    }

    @Override
    public void setPrice(String orderID, int mode, long price, Boolean isPayment)
    {
        System.out.println("setPrice listener" + PRICE_STRING);
        customItemPresent = true;
        donationAmount = price;
        this.PRICE_STRING = String.valueOf(donationAmount);
        titleText2.setText("Custom Amount: " + formatPrice(PRICE_STRING));
        setUpButtonNames(cPage);
        setUpButtonOrder(cPage);

        changeIntent();

        /*edt.putString("customPrice", Long.toString(price));
        edt.putBoolean("customPresent", true);
        edt.commit();*/

    }

    @Override
    public void changeButton(int buttonID, String PRICE_STRING)
    {
        if(buttonID == 6){
            System.out.println("test listener" + PRICE_STRING);
            donationAmount = Long.parseLong(PRICE_STRING);
        }
    }


    /**
     * UI animation (ProgressBar) for End User to recognize that the creation of the Customer is taking place.
     */

    private void startProgressDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving your Customer...");
        progressDialog.show();
    }

    /**
     * Cancel the ProgressBar.
     */

    private void cancelProgressDialog()
    {
        if(progressDialog != null)
        {
            if(progressDialog.isShowing())
                progressDialog.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        unregisterReceiptRegistration();
        System.out.println("bar result order1 ");

        if (data != null && requestCode == PAY_REQUEST_CODE){
            System.out.println("bar result order2 ");

            if (resultCode == RESULT_OK){
                //Once the secure payment activity completes the result and its extras can be worked with
                String orderID = data.getStringExtra(Intents.EXTRA_ORDER_ID);
                System.out.println("bar result orderID: " + orderID);

             //  new OrderAsyncTask(false).execute();

                mHelper.createOrder("BarOrders", mTotal, mTimestamp, mItems);


               /*Payment tpayment = data.getParcelableExtra(Intents.EXTRA_PAYMENT);
               String amountString = String.format("%.2f", ((Double) (0.01 * tpayment.getAmount())));*/
               Toast.makeText(getApplicationContext(), getString(R.string.payment_successful), Toast.LENGTH_SHORT).show();
               resetOrder();


            } else {
                Toast.makeText(getApplicationContext(), getString(zomifi.op27no2.printlogo.R.string.payment_failed), Toast.LENGTH_SHORT).show();
                resetOrder();
            }

        }
        else{
            resetOrder();
        }

        if (requestCode == RESULT_CODE/* && resultCode == RESULT_OK*/) {
            System.out.println("test1" + data);
         //   b1.setText(sharedPreferences.getString("recentItemId","error"));

        } else {
            System.out.println("test2");
        }
        if (requestCode == ORDER_REQUEST_CODE/* && resultCode == RESULT_OK*/) {
            System.out.println("test1" + data);

        } else {
            System.out.println("test2");

        }
    }


    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
        //    setFullscreen();
        }
    }


    private void createOrder()
    {
        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                new OrderAsyncTask().execute();
            }
        };

        handler.postDelayed(mrunnable, 100);
    }




    public void payIntent(){
        System.out.println("payintent called");
        mTotal = 0l;

        Map<String, OrderItem> items = new HashMap<String, OrderItem>();
        for(int i=0; i<lineItems.size(); i++){
            for(int j=0; j<lineItems.get(i).get(2); j++) {
                String id = sharedPreferences.getString(2 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                String name = sharedPreferences.getString(id + "_name", "");
                Long price = Long.parseLong(sharedPreferences.getString(id + "_price" + 2, "0"));
                mTotal = mTotal + price;

                if(sharedPreferences.getBoolean(id+"_isvcash",false) == true){
                    mTotal = mTotal + (price/10);
                }
            }
        }
        if(customItemPresent){
            mTotal = mTotal + donationAmount;
        }

        registerReceiptRegistration();
        PrintBuilder mBuilder = new PrintBuilder();
        mBuilder.initialize(mContext, 2);
        mBuilder.PrintLineItemsReceipt(lineItems, mTotal);

        new OrderAsyncTask().execute();

      /*  Intent intent = new Intent(Intents.ACTION_PAY);
        intent.putExtra(Intents.EXTRA_AMOUNT, mTotal);
        startActivityForResult(intent, PAY_REQUEST_CODE);
*/
    }

    public void changeIntent(){


        System.out.println("payintent called");
        mTotal = 0l;

        Map<String, OrderItem> items = new HashMap<String, OrderItem>();
        for(int i=0; i<lineItems.size(); i++){
            for(int j=0; j<lineItems.get(i).get(2); j++) {
                String id = sharedPreferences.getString(2 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                String name = sharedPreferences.getString(id + "_name", "");
                Long price = Long.parseLong(sharedPreferences.getString(id + "_price" + 2, "0"));
                mTotal = mTotal + price;

                if(sharedPreferences.getBoolean(id+"_isvcash",false) == true){
                    mTotal = mTotal + (price/10);
                }
            }
        }
        if(customItemPresent){
            mTotal = mTotal + donationAmount;
        }

        registerReceiptRegistration();
        PrintBuilder mBuilder = new PrintBuilder();
        mBuilder.initialize(mContext, 2);
        mBuilder.PrintChangeReceipt(mTotal);

        new ChangeAsyncTask().execute();


    }


    private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {
        int pos;

        public OrderAsyncTask(){
            super();
        }

        //TODO Donate Here app needs to updateOrder to make receipts work correctly?????
        //TODO OTHER code for variable price??

        @Override
        protected final Order doInBackground(Void... params) {
            Order tOrder = null;
            try {
                //creating order just for timestamp and dummy order for receipt printing, no actual line items
                tOrder = orderConnector.createOrder(new Order());

                mTotal = 0l;
                mItems = null;
                mTimestamp = tOrder.getCreatedTime();

                Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                int counter = 0;
                for(int i=0; i<lineItems.size(); i++){
                    for(int j=0; j<lineItems.get(i).get(2); j++) {
                        String id = sharedPreferences.getString(2 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                        String name = sharedPreferences.getString(id + "_name", "");
                        Long price = Long.parseLong(sharedPreferences.getString(id + "_price" + 2, "0"));
                        mTotal = mTotal + price;

                        OrderItem mOrderItem = new OrderItem(name, price, mTimestamp, 0l, false);
                        items.put("item" + counter, mOrderItem);
                        counter++;
                        if(sharedPreferences.getBoolean(id+"_isvcash",false) == true){
                            OrderItem feeOrderItem = new OrderItem(name+" fee", price/10, mTimestamp,0l, false);
                            items.put("item" + counter, feeOrderItem);
                            mTotal = mTotal + (price/10);
                            counter++;
                        }
                    }
                }
                if(customItemPresent){
                    OrderItem mOrderItem = new OrderItem("Custom", donationAmount, mTimestamp,0l, false);
                    items.put("item"+counter, mOrderItem);
                    mTotal = mTotal + donationAmount;
                    customItemPresent = false;
                }
                mItems = items;

                LineItem myLineItem = new LineItem();
                myLineItem.setName("Total");
                myLineItem.setPrice(mTotal);
                orderConnector.addCustomLineItem(tOrder.getId(), myLineItem, false);
                tOrder = orderConnector.updateOrder(tOrder);
                return tOrder;
            } catch (RemoteException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (ClientException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (ServiceException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (BindingException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            }

            return tOrder;
        }

        @Override
        protected final void onPostExecute(Order order) {
                mTimestamp = order.getCreatedTime();

                Intent intent = new Intent(Intents.ACTION_CLOVER_PAY);
                intent.putExtra(Intents.EXTRA_CLOVER_ORDER_ID, order.getId());
                startActivityForResult(intent, PAY_REQUEST_CODE);

                String orderId = order.getId();
                System.out.println("CREATED ORDER ID:" + orderId);
           //     mHelper.createOrder("BarOrders", mTotal, mTimestamp, mItems);



        }
    }


    private class ChangeAsyncTask extends AsyncTask<Void, Void, Order> {
        int pos;

        public ChangeAsyncTask(){
            super();
        }

        //TODO Donate Here app needs to updateOrder to make receipts work correctly?????
        //TODO OTHER code for variable price??

        @Override
        protected final Order doInBackground(Void... params) {
            Order tOrder = null;
            try {
                //creating order just for timestamp and dummy order for receipt printing, no actual line items
                tOrder = orderConnector.createOrder(new Order());

                mTimestamp = tOrder.getCreatedTime();

                LineItem myLineItem = new LineItem();
                myLineItem.setName("Amount Changed:");
                myLineItem.setPrice(mTotal);
                orderConnector.addCustomLineItem(tOrder.getId(), myLineItem, false);
                tOrder = orderConnector.updateOrder(tOrder);
                return tOrder;
            } catch (RemoteException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (ClientException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (ServiceException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            } catch (BindingException e) {
                System.out.println("Order CREATTTED ERROR");
                e.printStackTrace();
            }

            return tOrder;
        }

        @Override
        protected final void onPostExecute(Order order) {
            String orderId = order.getId();
            System.out.println("CREATED ORDER ID:" + orderId);

            mTimestamp = order.getCreatedTime();
            mHelper.createChange("BarOrders", mTotal, mTimestamp);
            CustomCashDialog customDialog = new CustomCashDialog("bar",mContext, mTotal, 0);
            customDialog.setCancelable(false);
            customDialog.show();
            CashDrawer.open(mContext, account);

            PrintJob pj = new StaticBillPrintJob.Builder().order(order).build();
            pj.print(BarActivity.this, account);
            resetOrder();
            resetReceipt();

        }
    }


    private void resetMultiplicity()
    {
        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                mMultiplicity = 1;
                multiplesScroll.scrollTo(0,0);
                setButtonColor(mMultiplicity);
            }
        };

        handler.postDelayed(mrunnable, 50);
    }

    public void addLineItems(int pos){
        // items stored (page 1-x, button number 1-9, multiplicity)
        Boolean newitem = true;

        for(int j=0;j<lineItems.size();j++) {
            if (pos!=16 && lineItems.get(j).get(1) == (pos) && lineItems.get(j).get(0) == cPage) {
                lineItems.remove(j);
                newitem = false;
            }
        }
        if(newitem) {
            // IF ITEM ISN'T ADDED
            ArrayList<Integer> singleitem = new ArrayList<Integer>();
            singleitem.add(cPage);
            singleitem.add(pos);
            singleitem.add(mMultiplicity);
            lineItems.add(singleitem);
        }
        System.out.println("Line Items"+lineItems);

        resetMultiplicity();
        setUpButtonOrder(cPage);
    }

    private void setUpButtonOrder(int currentPage) {
        long total = 0;

        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        for(int i=0;i<16;i++){

           /// myButtons[i].setText(sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_name", "Add from Settings") + "\n" + /*formatPrice(*/sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_price", ""))/*)*/;
            myButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);
            String id = sharedPreferences.getString(2+"_"+currentPage+"button"+(i+1)+"_id", "");
            myButtons[i].setText(sharedPreferences.getString(id + "_name", "Add from Settings") + "\n" + formatPrice(sharedPreferences.getString(id + "_price" + 2, "")));

            //.get(0)=page, .get(1)=position, .get(2)=multiplicity
            //for each item, if its position is equal to the button, and the page is equal to the current page, change button text
            for(int j=0;j<lineItems.size();j++){
                if(lineItems.get(j).get(1) == (i+1) && lineItems.get(j).get(0) == cPage){
                    int mult = lineItems.get(j).get(2);
                    myButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
                   // myButtons[i].setText(sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_name", "Add from Settings") + " x" + lineItems.get(j).get(2) + "\n" + /*formatPrice(*/sharedPreferences.getString(2 + "_" + currentPage + "button" + (i + 1) + "_price", ""))/*)*/;
                    myButtons[i].setText(sharedPreferences.getString(id + "_name", "Add from Settings") + " x" + lineItems.get(j).get(2) + "\n" + formatPrice(sharedPreferences.getString(id + "_price" + 2, "")));
                    total = total+(mult* Long.parseLong(sharedPreferences.getString(id + "_price" + 2, "")));

                    if(sharedPreferences.getBoolean(id+"_isvcash",false) == true){
                        total = total + mult*(Long.parseLong(sharedPreferences.getString(id + "_price" + 2, ""))/10);
                    }
                    break;
                }
            }


        }
        if(customItemPresent){
            total = total + donationAmount;
        }
        myButtons[15].setText("Change");

        titleText1.setText(formatPrice(Long.toString(total)));


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

/*
    private class getOrderUploadFirebaseAsync extends AsyncTask<String, Void, Order>
    {
        @Override
        protected Order doInBackground(String... params) {
            String orderID = params[0];
            Order thisOrder = null;
            try {
                System.out.println("check connect"+orderConnector.isConnected());
                thisOrder = orderConnector.getOrder(orderID);
                thisOrder = orderConnector.updateOrder(thisOrder);
                Long total = thisOrder.getTotal();
                Long timestamp = thisOrder.getCreatedTime();
                List<LineItem> myItems = thisOrder.getLineItems();
                Map<String, BarOrderItem> items = new HashMap<String, BarOrderItem>();
                for(int i=0; i<myItems.size(); i++){
                    String name = myItems.get(i).getName();
                    Long price = myItems.get(i).getPrice();
                    BarOrderItem mOrderItem = new BarOrderItem(name, price, thisOrder.getCreatedTime());
                    items.put("item"+i, mOrderItem);
                }

                mHelper.createBarOrder("BarOrders",total, timestamp, items);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
            return thisOrder;
        }

        @Override
        public void onPostExecute(Order order)
        {
            System.out.println("ORDER UPDATED: "+order.getId());

        }
    }*/



    private void registerReceiptRegistration() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Create and Connect
                receiptConnector.register(Uri.parse(ReceiptRegistrationProviderDejaVu.CONTENT_URI_IMAGE.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                System.out.println("REGISTERED!!");

            }
        }.execute();
    }

    private void unregisterReceiptRegistration() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                receiptConnector.unregister(Uri.parse(ReceiptRegistrationProviderDejaVu.CONTENT_URI_IMAGE.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                System.out.println("UNREGISTERED");

                // shouldn't need to disconnect here - will disconnect when activity is exited regardless, and connect call disconnects before reconnecting
                // disconnect();
            }
        }.execute();
    }

    public void resetOrder()
    {
        donationAmount = 0;
        edt.putBoolean("customPresent", false);
        edt.putString("customPrice", "0");
        edt.commit();
        lineItems.clear();
        setUpButtonNames(cPage);
    }

    private void resetReceipt()
    {
        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                unregisterReceiptRegistration();
            }
        };

        handler.postDelayed(mrunnable, 10000);
    }


}