package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
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
import android.os.Looper;
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
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.util.CustomerMode;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.printer.CashDrawer;
import com.clover.sdk.v1.printer.ReceiptRegistrationConnector;
import com.clover.sdk.v1.printer.job.PrintJob;
import com.clover.sdk.v1.printer.job.StaticBillPrintJob;
import com.clover.sdk.v1.tender.Tender;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.base.TenderConstants;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Payment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class DoorActivity extends Activity implements View.OnClickListener, CustomPriceEnteredListener, CustomManagerListener
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private MerchantConnector merchantConnector;
    private EmployeeConnector mEmployeeConnector;
    private OrderConnector orderConnector;
    private CustomerConnector customerConnector;
    private TenderConnector tenderConnector;
    private Context mContext;
    private Account account;
    private Order mOrder;
    private Handler handler = new Handler();
    private ReceiptRegistrationConnector receiptConnector;

    private TextView titleText1;
    private TextView titleText2;
    private TextView pageText;

    private ScrollView multiplesScroll;
    private ProgressDialog progressDialog;
    private LinearLayout multiplesLayout;

    private long donationAmount = 0;
    private int TRIGGER = 0;
    private int authTries = 0;
    private static final int SECURE_PAY_REQUEST_CODE = 1;
    private int recentButton;
    private int cPage = 1;
    private int mMultiplicity = 1;
    private ArrayList<ArrayList<Integer>> lineItems = new ArrayList<ArrayList<Integer>>();

    private Boolean managerEnabled = true;

    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private String          mercID = "";
    private String recentItem;
    private String tenderID;
    private String mOrderId;
    private Long mTimestamp;
    private Long mPayment;
    private Long mTotal;
    private Map<String, OrderItem> mItems;
    private String PRICE_STRING = "";

    Button multipleButtons[] = new Button[20];
    Button buttons[] = new Button[16];
    Button priceButtons[] = new Button[7];
    private ImageButton bRight;
    private ImageButton bLeft;
    private Button voidButton;
    private FirebaseHelper mHelper;
    private Boolean customItemPresent = false;

    //TEST PUSH
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);
        mContext = this;
        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        mercID = prefs.getString("mercID", "");
        mHelper = new FirebaseHelper(this);
        mHelper.initialize();


        Button homeButton = (Button) findViewById(R.id.home_button);
        Button voidButton = (Button) findViewById(R.id.voidbutton);
        multiplesLayout = (LinearLayout) findViewById(zomifi.op27no2.printlogo.R.id.multiplicity);
        multiplesScroll = (ScrollView) findViewById(R.id.multiplesscroll);
        titleText1 = (TextView) findViewById(R.id.title_text1);
        titleText2 = (TextView) findViewById(R.id.title_text2);
        bRight = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bright);
        bLeft = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bleft);
        pageText = (TextView) findViewById(zomifi.op27no2.printlogo.R.id.pages);


/*        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomManagerDialog managerDialog = new CustomManagerDialog(mContext, false);
                managerDialog.setCustomManagerListener(this);
                managerDialog.show();

                Intent myIntent = new Intent(DoorActivity.this, NavigationActivity.class);
                DoorActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                CustomerMode.disable(mContext);

            }
        });
        */
        homeButton.setOnClickListener(this);
        voidButton.setOnClickListener(this);
        voidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDoorOrderDialog customDialog = new CustomDoorOrderDialog(mContext);
                customDialog.setCancelable(false);
                customDialog.show();
            }
        });

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
                if(cPage<10){
                    cPage = cPage+1;
                    edt.putInt("currentPage",cPage);
                    edt.commit();
                    setUpButtonNames(cPage);
                }
            }
        });
        for(int i=0; i<16; i++) {
            int resID = getResources().getIdentifier("button"+(i+1), "id", "zomifi.op27no2.printlogo");
            buttons[i] = ((Button) findViewById(resID));
            buttons[i].setOnClickListener(this);
        }
        for(int i=0; i<20; i++) {
            int resID = getResources().getIdentifier("mbutton"+(i+1), "id", "zomifi.op27no2.printlogo");
            multipleButtons[i] = ((Button) findViewById(resID));
            multipleButtons[i].setOnClickListener(this);
        }
        for(int i=0; i<7; i++) {
            int resID = getResources().getIdentifier("money"+(i+1), "id", "zomifi.op27no2.printlogo");
            priceButtons[i] = ((Button) findViewById(resID));
            priceButtons[i].setOnClickListener(this);
        }


        mMultiplicity = prefs.getInt("multiplicity",1);
        setMultiplesColor(mMultiplicity);
        setUpButtonNames(cPage);
        this.PRICE_STRING = "";
    }

    private void setUpButtonNames(int currentPage) {
        long total = 0;
        pageText.setText("Page "+currentPage);
/*
        for(int i=0; i<8;i++){
            buttons[i].setText(prefs.getString(3 + "_" + currentPage + "button" + (i + 1) + "_name", "Add from Settings") + "\n" + prefs.getString(3 + "_" + currentPage + "button" + (i + 1) + "_price", ""));
        }
*/
        prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        for(int i=0;i<15;i++){

          //  buttons[i].setText(prefs.getString(3+"_"+currentPage + "button" + (i + 1) + "_name", "Add from Settings") + "\n" + prefs.getString(3+"_"+currentPage + "button" + (i + 1) + "_price", ""));
          //  buttons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);

            String id = prefs.getString(3+"_"+currentPage+"button"+(i+1)+"_id", "");
            buttons[i].setText(prefs.getString(id + "_name", "Add from Settings") + "\n" + formatPrice(prefs.getString(id + "_price" + 3, "")));
            buttons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);

            //.get(0)=page, .get(1)=position, .get(2)=multiplicity
            //for each item, if its position is equal to the button, and the page is equal to the current page, change button text
            for(int j=0;j<lineItems.size();j++){
                if(lineItems.get(j).get(1) == (i+1) && lineItems.get(j).get(0) == cPage){
                    int mult = lineItems.get(j).get(2);
                    buttons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
                 //   buttons[i].setText(prefs.getString(3 + "_" + currentPage + "button" + (i + 1) + "_name", "Add from Settings") + " x" + lineItems.get(j).get(2) + "\n" + prefs.getString(3 + "_" + currentPage + "button" + (i + 1) + "_price", ""));
                    buttons[i].setText(prefs.getString(id + "_name", "Add from Settings") + " x" + lineItems.get(j).get(2) + "\n" + formatPrice(prefs.getString(id + "_price" + 3, "")));
                    total = total+(mult*Long.parseLong(prefs.getString(id + "_price" + 3, "")));
                    break;
                }
            }

        }
        if(customItemPresent){
            total = total + donationAmount;
        }
        buttons[15].setText("Custom Amount");
      //  if(!customItemPresent) {
            titleText1.setText(formatPrice(Long.toString(total)));
      //  }
      //  else if(customItemPresent){
      //  }

    }
    private void setMultiplesColor(int num){
        for(int i=0;i<20;i++){
            if(num-1 == i) {
                multipleButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
            }
            else{
                multipleButtons[i].setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.aqua_green_button);
            }
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
        managerEnabled = false;

        resetMultiplicity();
        stopAnim();
        titleText1.setText("");
        titleText2.setText("");
        recentItem = null;
        donationAmount = 0;
        mOrderId = null;
        customItemPresent = false;

        if(Platform.isCloverMobile() || Platform.isCloverMini()) {
            CustomerMode.enable(this);
        }

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(this);

            if (account == null) {
                Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Create and Connect
        connect();

    }

    private void connectMerchantConnector()
    {
        disconnectMerchantConnector();
        if(this.account != null)
        {
            this.merchantConnector = new MerchantConnector(this, this.account, null);
            this.merchantConnector.connect();
        }
    }



    private void disconnectMerchantConnector()
    {
        if(this.merchantConnector != null)
        {
            this.merchantConnector.disconnect();
            this.merchantConnector = null;
        }
    }

    @Override
    protected void onPause()
    {

        disconnect();

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        System.out.println("onDestroy called");
        super.onDestroy();
    }

    private void connect() {
        disconnect();

        System.out.println("Connect Called");

        if (account != null) {

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


            this.tenderConnector = new TenderConnector(mContext, account, new ServiceConnector.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
                    if (tenderConnector != null) {
                        getTender();
                    }
                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
                }
            });
            this.tenderConnector.connect();

            orderConnector = new OrderConnector(this, account, null);
            orderConnector.connect();
        }
        else
            System.out.println("Account not connected.");
    }

    private void disconnect() {

        if (mEmployeeConnector != null) {
            mEmployeeConnector.disconnect();
            mEmployeeConnector = null;
        }
        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }
        if (customerConnector != null) {
            customerConnector.disconnect();
            customerConnector = null;
        }
        if (orderConnector != null) {
            orderConnector.disconnect();
            orderConnector = null;
        }
    }

    @Override
    public void onClick(View v)
    {
        recentItem = null;

        for(int i=0;i<15;i++){
            if(v.getId() == getResources().getIdentifier("button"+(i+1), "id", "zomifi.op27no2.printlogo")){
                if (!buttons[i].getText().equals("Add from Settings" + "\n" + "")) {
                    addLineItems(i + 1);
                }

 /*               recentItem = prefs.getString("3_"+cPage+"button" + (i + 1) + "_id", "");
                recentButton = i;
                //titleText.setText("Amount: "+prefs.getString("3_1button" + (i + 1) + "_price", ""));
                try {
                    Order holdOrder = new CreateDonationAsync().execute().get();
                    mOrder = holdOrder;
                   // orderId = mOrder.getId();
                } catch (InterruptedException e) {
                    Log.e("ERROR: Interrupted", e.getMessage());
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Log.e("ERROR: Execution", e.getMessage());
                    e.printStackTrace();
                }*/

            }
        }
        if(v.getId() == R.id.button16) {
            recentButton = 16;
            recentItem = "custom";
            LineItemPriceSetter lineItemPriceSetter = new LineItemPriceSetter(this,3, false);
            lineItemPriceSetter.setCustomPriceEnteredListener(this);
            lineItemPriceSetter.setButtonIndex(6);
            lineItemPriceSetter.show();
        }
        if(v.getId() == R.id.home_button) {
            CustomManagerDialog managerDialog = new CustomManagerDialog(mContext, false);
            managerDialog.setCustomManagerListener(this);
            managerDialog.setButtonID(R.id.home_button);
            managerDialog.show();

        }

        for(int i=0; i<7; i++){
            if(v.getId() == priceButtons[i].getId()){
            //get amount from prefs - or set custom amount
                mPayment = null;
                if(i==4){
                    // custom price dialog for payment
                    LineItemPriceSetter lineItemPriceSetter = new LineItemPriceSetter(this,3, true);
                    lineItemPriceSetter.setCustomPriceEnteredListener(this);
                    lineItemPriceSetter.setButtonIndex(6);
                    lineItemPriceSetter.show();
                }
                else {
                    switch (i) {
                        case 0:
                            mPayment = prefs.getLong("cash_amount" + 1, 1000);
                            break;
                        case 1:
                            mPayment = prefs.getLong("cash_amount" + 2, 2000);
                            break;
                        case 2:
                            mPayment = prefs.getLong("cash_amount" + 3, 5000);
                            break;
                        case 3:
                            mPayment = prefs.getLong("cash_amount" + 4, 10000);
                            break;
                        case 5:
                            mPayment = prefs.getLong("cash_amount" + 5, 1200);
                            break;
                        case 6:
                            mPayment = prefs.getLong("cash_amount" + 6, 2200);
                            break;
                    }
                    new OrderAsyncTaskCash().execute();
                }

            }
        }

        for(int i=0; i<20; i++){
            if(v.getId() == multipleButtons[i].getId()){
                mMultiplicity = i+1;
                setMultiplesColor(i + 1);
                edt.putInt("multiplicity", i+1);
                edt.commit();
            }
        }

    }

    @Override
    public void setPrice(String orderID, String name, int mode, long price, Boolean isPayment)
    {
        if(!isPayment) {
            System.out.println("setPrice listener" + PRICE_STRING);
            customItemPresent = true;
            donationAmount = price;
            this.PRICE_STRING = String.valueOf(donationAmount);
            titleText2.setText("Custom Amount: " + formatPrice(PRICE_STRING));
            setUpButtonNames(cPage);
            /*edt.putString("customPrice", Long.toString(price));
            edt.putBoolean("customPresent", true);
            edt.commit();*/
        }
        else if(isPayment){
            mPayment = price;
            new OrderAsyncTaskCash().execute();
        }


    }

    @Override
    public void changeButton(int buttonID, String PRICE_STRING)
    {
        if(buttonID == 6){
            System.out.println("test listener" + PRICE_STRING);
            donationAmount = Long.parseLong(PRICE_STRING);
            titleText2.setText("Amount: " + formatPrice(PRICE_STRING));
        }
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
        setUpButtonNames(cPage);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        unregisterReceiptRegistration();

        if (requestCode == SECURE_PAY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                //Once the secure payment activity completes the result and its extras can be worked with

                Payment payment = data.getParcelableExtra(Intents.EXTRA_PAYMENT);
                String amountString = String.format("%.2f", ((Double) (0.01 * payment.getAmount())));
                Toast.makeText(getApplicationContext(), getString(R.string.payment_successful, amountString), Toast.LENGTH_SHORT).show();

                String order = payment.getOrder().getId();
                System.out.println("orderresult:" + order);

           //     receiptDialog(order);

            }
        }
    }

    private void receiptDialog(final String orderId)
    {
        CustomReceiptDialog customReceiptDialog = new CustomReceiptDialog(mContext, mercID, orderId, "email", "phone");
        customReceiptDialog.setHeaderText("How Would You Like Your Receipt?");
        customReceiptDialog.show();
    }


    private class OrderAsyncTaskCash extends AsyncTask<Void, Void, Order> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

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
                        String id = prefs.getString(3 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                        String name = prefs.getString(id + "_name", "");
                        String category = prefs.getString(id + "_category", "");
                        Long price = Long.parseLong(prefs.getString(id + "_price" + 3, "0"));
                        mTotal = mTotal + price;

                        OrderItem mOrderItem = new OrderItem(name, category, price, mTimestamp, 0l,0l, false, false, false, false);
                        items.put("item" + counter, mOrderItem);
                        counter++;
                    }
                }
                if(customItemPresent){
                    OrderItem mOrderItem = new OrderItem("Custom", "Custom", donationAmount, mTimestamp, 0l,0l, false, false, false, false);
                    items.put("item"+counter, mOrderItem);
                    mTotal = mTotal + donationAmount;
                }
                mItems = items;

                LineItem myLineItem = new LineItem();
                myLineItem.setName("Total");
                myLineItem.setPrice(mTotal);
                orderConnector.addCustomLineItem(tOrder.getId(), myLineItem, false);
                tOrder = orderConnector.updateOrder(tOrder);

/*                System.out.println("Order called lineItems:"+lineItems);
                for(int i=0; i<lineItems.size(); i++){
                   // String itemId = prefs.getString(3+"_"+lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id","");
                    for(int j=0; j<lineItems.get(i).get(2); j++) {
                        System.out.println("iterate lineItems:"+lineItems.get(i));

                        String id = prefs.getString(3 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                        String name = prefs.getString(id + "_name", "");
                        Long price = Long.parseLong(prefs.getString(id + "_price" + 3, "0"));
                        LineItem myLineItem = new LineItem();
                        myLineItem.setName(name);
                        myLineItem.setPrice(price);
                        orderConnector.addCustomLineItem(tOrder.getId(), myLineItem, false);
                        // orderConnector.addFixedPriceLineItem(tOrder.getId(), itemId, null, null);
                    }
                }*/


            } catch (RemoteException e) {
                System.out.println("Order CREATTTED ERROR"+e.getMessage());
                e.printStackTrace();
            } catch (ClientException e) {
                System.out.println("Order CREATTTED ERROR"+e.getMessage());
                e.printStackTrace();
            } catch (ServiceException e) {
                System.out.println("Order CREATTTED ERROR"+e.getMessage());
                e.printStackTrace();
            } catch (BindingException e) {
                System.out.println("Order CREATTTED ERROR"+e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Order created" + tOrder);
            return tOrder;
        }

        @Override
        protected final void onPostExecute(Order order) {
            mOrderId = order.getId();
            System.out.println("Order postExecute"+mOrderId + " total:"+order.getTotal());
            mTimestamp = order.getCreatedTime();
            stopAnim();

        /*    mTotal = 0l;
            mItems = null;

            Map<String, OrderItem> items = new HashMap<String, OrderItem>();
            for(int i=0; i<lineItems.size(); i++){

                for(int j=0; j<lineItems.get(i).get(2); j++) {
                    String id = prefs.getString(3 + "_" + lineItems.get(i).get(0) + "button" + lineItems.get(i).get(1) + "_id", "error");
                    String name = prefs.getString(id + "_name", "");
                    Long price = Long.parseLong(prefs.getString(id + "_price" + 3, "0"));
                    mTotal = mTotal + price;

                    OrderItem mOrderItem = new OrderItem(name, price, mTimestamp, false);
                    items.put("item" + i, mOrderItem);
                }
            }
            mItems = items;*/

                if(0 >= mTotal) {
                    Toast.makeText(DoorActivity.this, "Total must be greater than zero", Toast.LENGTH_LONG).show();
                }
                else if(mPayment >= mTotal) {

                CustomCashDialog customDialog = new CustomCashDialog("door",mContext, mPayment, mTotal);
                customDialog.setCancelable(false);
                customDialog.show();
                CashDrawer.open(mContext, account);
                mHelper.createOrder("DoorOrders", mTotal, mTimestamp, mItems);

                registerReceiptRegistration();
                PrintBuilder mBuilder = new PrintBuilder();
                mBuilder.initialize(mContext,3);
                mBuilder.PrintLineItemsReceipt(lineItems, mTotal, false);


                PrintJob pj = new StaticBillPrintJob.Builder().order(order).build();
                pj.print(DoorActivity.this, account);
                resetOrder();
                resetReceipt();


            }
            else{
                // not enough cash received
                Toast.makeText(DoorActivity.this, "Payment ("+formatPrice(mPayment.toString())+") must be greater than order total("+formatPrice(mTotal.toString())+")", Toast.LENGTH_LONG).show();
            }

     /*       if(mPayment >= order.getTotal()){
                authTries = 0;
            //    getAuth();
            }
            else{
                //TODO dialog message payment too low
            }*/

        }
    }

    private void getTender() {
        System.out.println("GETTENDER CALLED");

        new AsyncTask<Void, Void, Tender>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected com.clover.sdk.v1.tender.Tender doInBackground(Void... params) {
                com.clover.sdk.v1.tender.Tender myTender = null;
                try {
                    List<Tender> tenders = tenderConnector.getTenders();
                    for (com.clover.sdk.v1.tender.Tender tender :
                            tenders) {
                        if (tender.getLabelKey().equalsIgnoreCase(TenderConstants.CASH))
                        {
                            tenderID = tender.getId();
                            myTender = tender;
                            break;
                        }
                    }

                } catch (RemoteException e) {

                } catch (ClientException e) {

                } catch (ServiceException e) {

                } catch (BindingException e) {

                }
                return myTender;
            }

            @Override
            protected void onPostExecute(com.clover.sdk.v1.tender.Tender tender) {
                super.onPostExecute(tender);
                if (tender != null) {

                }

            }
        }.execute();
    }

    private void getAuth() {
        System.out.println("GETAUTH CALLED");

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                CloverAuth.AuthResult authResult = null;
                {
                    try {
                        authResult = CloverAuth.authenticate(mContext, account);
                        request(authResult);
                    } catch (OperationCanceledException e) {
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }



        }.execute();
    }


    private void request(CloverAuth.AuthResult authResult){
        String paymentsUri ="";
        paymentsUri = "/v3/merchants/" + mercID + "/orders/" + mOrderId + "/payments/";
        String url = authResult.baseUrl + paymentsUri + "?access_token=" + authResult.authToken;
        System.out.println("request url"+ url);

        JSONObject payment = new JSONObject();

        try {
            payment.put("amount", mPayment);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject tender = new JSONObject();
        try {
            tender.put("id", tenderID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            payment.put("tender", tender);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String result = "default";
        System.out.println("request json" + payment.toString());

        try {
            apiHelper.getInstance().doPostRequest(url, payment.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Something went wrong
                    System.out.println("request fail");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        public void run() {
                            Toast.makeText(DoorActivity.this, "Payment Request Failed", Toast.LENGTH_LONG).show();
                            stopAnim();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        System.out.println("request success" + responseStr);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            public void run() {
                                //Toast.makeText(MainActivity2.this, "Order Created and Paid, You May Clear At Any Time", Toast.LENGTH_SHORT).show();
                                stopAnim();
                                CustomCashDialog customDialog = new CustomCashDialog("door",mContext, mPayment , mTotal);
                                customDialog.setCancelable(false);
                                customDialog.show();
                                CashDrawer.open(mContext, account);
                                mHelper.createOrder("DoorOrders", mTotal, mTimestamp, mItems);
                            }
                        });


                        // Do what you want to do with the response.
                    } else {
                        if(authTries<25){
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                                public void run() {
                                    getAuth();
                                    authTries++;
                                }
                            }, 200);
                            System.out.println("try again" + response.body().string());
                        }
                        else {
                            // Request not successful
                            new Handler(Looper.getMainLooper()).post(new Runnable() {

                                public void run() {
                                    Toast.makeText(DoorActivity.this, "Payment Request Failed, Check Your Internet Connection", Toast.LENGTH_LONG).show();
                                    stopAnim();
                                }
                            });
                            System.out.println("request unsuccessful " + response.body().string());
                        }
                    }
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("request try error");
        }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    void startAnim(){
        findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);
    }

    void stopAnim(){
        findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
    }

    private void resetMultiplicity()
    {
        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                mMultiplicity = 1;
                multiplesScroll.scrollTo(0,0);
                setMultiplesColor(mMultiplicity);
            }
        };

        handler.postDelayed(mrunnable, 50);
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


    @Override
    public void managerCallback(int buttonID) {
        managerEnabled = true;

        if(buttonID == R.id.home_button) {
            Intent myIntent = new Intent(this, NavigationActivity.class);
            this.startActivity(myIntent);
            CustomerMode.disable(this);
        }



    }




}