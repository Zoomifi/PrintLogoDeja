package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ResultStatus;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.clover.sdk.v3.payments.Payment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.R.attr.start;

public class IPEActivity extends Activity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor edt;
    private MerchantConnector merchantConnector;
    private EmployeeConnector mEmployeeConnector;
    private OrderConnector orderConnector;
    private CustomerConnector customerConnector;
    private InventoryConnector inventoryConnector;
    private Employee employee;
    private DatabaseReference myRef;
    private String mercID;
    private static FirebaseRecyclerAdapter mAdapter;
    private Set<Integer> mSet = new HashSet<Integer>();


    private Context mContext;
    private Account account;
    private Order mOrder;
    private Handler handler = new Handler();
    private Runnable runnable;
    private Payment mPayment;
    private boolean navigateAway = false;
    private boolean building = false;

    private GridViewAdapter customGridViewAdapter;
    private LinearLayout buttonLayout;
    private LinearLayout customerLayout;
    private TextView titleText;
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText phoneText;
    private EditText emailText;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private String PRICE_STRING = "";
    private String orderID;
    private List<String> gridData;

    private ProgressDialog progressDialog;
    private long donationAmount = 0;
    private String donationName = "";
    private int TRIGGER = 0;
    private int RESULT_CODE = 2;

    private static final int SECURE_PAY_REQUEST_CODE = 1;

    private boolean isOpened = false;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private GridView gridView;

    private TextView tabText;
    private Boolean editPrimed = false;
    private Boolean removePrimed = false;
    private Button editButton;
    private Button logoutButton;
    private Button saveButton;
    private Button selectButton;
    private Button clearButton;
    private Button removeButton;

    private Payment mPay;
    private ValueAnimator colorAnimation;
    private IPESelector mSelector;
    private FirebaseHelper mHelper;
    private ArrayList<ArrayList<Integer>> lineItems = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> ipeNums = new ArrayList<Integer>();
    private  SnappyHelper mDatabaseHelper;

    ArrayList<String> mOrderIds = new ArrayList<String>();
    ArrayList<Long> mOrderTotals = new ArrayList<Long>();
    ArrayList<Long> mOrderTimestamps = new ArrayList<Long>();
    ArrayList<Map<String, OrderItem>> mOrderItems = new ArrayList<Map<String, OrderItem>>();

    Map<String,String> mIPEs = new HashMap<String, String>();
    Set<String> displaydIPEs = new HashSet<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipe);
        System.out.println("ipe on create");
        sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = sharedPreferences.edit();
        mDatabaseHelper = new SnappyHelper(this);
        mercID = sharedPreferences.getString("mercID", "");

        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHelper = new FirebaseHelper(this);
        mHelper.initialize();
        donationAmount = sharedPreferences.getLong("CustomLongPrice", 0l);
        donationName = sharedPreferences.getString("customName", "");
        gridView = (GridView) findViewById(R.id.tabList);
        gridData = new ArrayList<>();
        for(int i=0; i<49 ; i++){
            gridData.add("Click to Add");
        }


        // tabText = (TextView) findViewById(R.id.tabtext);
        int colorFrom = ContextCompat.getColor(mContext, R.color.White);
        int colorTo = ContextCompat.getColor(mContext, R.color.Red);
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                tabText.setTextColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(Animation.INFINITE);

        buttonLayout = (LinearLayout) findViewById(R.id.buttonView);

        Button homeButton = (Button) findViewById(R.id.home_button);
        selectButton = (Button) findViewById(R.id.select_button);
        saveButton = (Button) findViewById(R.id.savebutton);
        clearButton = (Button) findViewById(R.id.textclear);
        removeButton = (Button) findViewById(R.id.remove_button);
        editButton = (Button) findViewById(R.id.editbutton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(IPEActivity.this, NavigationActivity.class);
                IPEActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        selectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                navigateAway = true;
                Intent myIntent = new Intent(IPEActivity.this, ItemsActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                IPEActivity.this.startActivity(myIntent);
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "IPEs and Items Deselected", Toast.LENGTH_SHORT).show();
                resetScreens();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrders();


            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (removePrimed) {
                    removePrimed = false;
                    removeButton.setAlpha(1f);
                } else {
                    removePrimed = true;
                    removeButton.setAlpha(0.5f);
                    Toast.makeText(getApplicationContext(), "Select Tab to Edit", Toast.LENGTH_SHORT).show();
                }*/
                toggleRemove();
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          /*      if (editPrimed) {
                    editPrimed = false;
                    editButton.setAlpha(1f);
                } else {
                    editPrimed = true;
                    editButton.setAlpha(0.5f);
                    Toast.makeText(getApplicationContext(), "Select Tab to Edit", Toast.LENGTH_SHORT).show();
                }*/
                toggleEdit();
            }
        });


        customGridViewAdapter = new GridViewAdapter(this, gridData);
        gridView.setAdapter(customGridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uniqueID = sharedPreferences.getString("uniqueID" + position, "");
                // if position is: not full, show dialog
                if (sharedPreferences.getBoolean("full" + position, false) == false) {
                    CustomIPEListDialog customDialog = new CustomIPEListDialog(mContext, position);
                    customDialog.setCancelable(false);
                    customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //onResume();
                            setupGrid();
                        }
                    });
                    customDialog.show();
                }else if(editPrimed ==true){
                    toggleEdit();
                    //pass "" order ID since we are querying for most recent.
                    CustomIPEOrderItemsListDialog customDialog = new CustomIPEOrderItemsListDialog(mContext, sharedPreferences.getString("uniqueID"+position,""), "", true);
                    customDialog.setCancelable(false);
                    customDialog.show();

                }else if(removePrimed ==true){
                    toggleRemove();
                    IPESelector mSelector = new IPESelector(mContext);
                    mSelector.clearPosition(position, uniqueID);
                    //onResume();
                    setupGrid();

                }
                //else if position is: active, turn green - inactive, turn red
                else{
                    if (sharedPreferences.getBoolean("active" + position, false) == false) {
                        edt.putBoolean("active"+position, true);
                        edt.putBoolean(uniqueID+"active", true);

                    }else{
                        edt.putBoolean("active"+position, false);
                        edt.putBoolean(uniqueID+"active", false);
                    }
                    edt.commit();
                    updateIPENumbs();
                    customGridViewAdapter.notifyDataSetChanged();
                }

            }
        });

       getEmployees();

    }



    private void deactivateIPEs(){
        edt.putInt("item_size", 0);
        edt.commit();
        for(int i=0; i<customGridViewAdapter.getCount();i++){
            edt.putBoolean("active" + i, false);
            edt.commit();
        }
        for (String id : mIPEs.values()) {
            edt.putBoolean(id+"active", false);
            edt.commit();
        }
        customGridViewAdapter.notifyDataSetChanged();
    }
    private void updateTitleText(){
        int tabposition = sharedPreferences.getInt("currenttab",0);
        if(sharedPreferences.getBoolean("isactive", false) == true && !sharedPreferences.getString("name"+tabposition, "Click to Add").equals("Click to Add")) {
            tabText.setText(sharedPreferences.getString("name" + tabposition, "Error"));
            colorAnimation.start();

        }
        else{
            tabText.setText("None");
            if(colorAnimation.isRunning()) {
                colorAnimation.end();
            }
            tabText.setTextColor(ContextCompat.getColor(mContext, R.color.White));
        }

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

     //   setupFullscreenMode();
    }


    @NonNull
    private String formatPrice(String PRICE_STRING) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        //default USD from prefs
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));
        long value = Long.valueOf(PRICE_STRING);
        String price = mCurrencyFormat.format(value / 100.0);
        return price;
    }

    @Override
    protected void onResume() {
        System.out.println("IPETAB OnResume Called");

        navigateAway = false;
        building = false;
        editPrimed = false;
        editButton.setAlpha(1f);
        //updateTitleText();
        setupGrid();

        prefsToLineItems();

        mHelper = new FirebaseHelper(this);
        mHelper.initialize();

    //    getEmployees();

      /*  for(int i=0; i<49 ; i++){
            if(sharedPreferences.getString("name"+i, "Click to Add").equals("Click to Add")){
                gridData.set(i,"Click to Add");
            }
            else {
                gridData.set(i, sharedPreferences.getString("name" + i, "Click to Add") + " \n" + sharedPreferences.getString("lastfour" + i, "") + " \n" + sharedPreferences.getString("custom" + i, ""));
            }
        }*/
        customGridViewAdapter.notifyDataSetChanged();

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

        super.onResume();

    }

    @Override
    protected void onPause() {
        System.out.println("onpause");
        //disconnect();
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
            merchantConnector = new MerchantConnector(this, account, null);
            merchantConnector.connect();

            mEmployeeConnector = new EmployeeConnector(this, account, null);
            mEmployeeConnector.connect();

            customerConnector = new CustomerConnector(this, account, null);
            customerConnector.connect();

            orderConnector = new OrderConnector(this, account, null);
            orderConnector.connect();

        }
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void resetScreens() {
        deactivateIPEs();
        // updateTitleText();
    }

    private void toggleRemove(){
        if (removePrimed) {
            removePrimed = false;
            removeButton.setAlpha(1f);
        } else {
            removePrimed = true;
            removeButton.setAlpha(0.5f);
            Toast.makeText(getApplicationContext(), "Select Tab to Remove", Toast.LENGTH_SHORT).show();
        }
    }
    private void toggleEdit(){
        if (editPrimed) {
            editPrimed = false;
            editButton.setAlpha(1f);
        } else {
            editPrimed = true;
            editButton.setAlpha(0.5f);
            Toast.makeText(getApplicationContext(), "Select Tab to Edit", Toast.LENGTH_SHORT).show();
        }
    }



    private void createOrders()
    {
        ArrayList<String> orderIdList = new ArrayList<String>();
        ArrayList<String> ipeIDList = new ArrayList<String>();
        ArrayList<String> ipeNameList = new ArrayList<String>();
        //ipeNums contains the integers of active ipes
        for(int i=0; i<ipeNums.size(); i++) {
        //    System.out.println("ipe iterate"+i);
            if (sharedPreferences.getBoolean("active" + ipeNums.get(i), false) == true) {
         //       System.out.println("ipe iterate isactive" + i);
                String myOrderId = sharedPreferences.getString("orderId" + i, "");
                orderIdList.add(myOrderId);
                ipeIDList.add(sharedPreferences.getString("uniqueID" + ipeNums.get(i), ""));
                ipeNameList.add(sharedPreferences.getString("name" + ipeNums.get(i), ""));
            }
        }
        new OrderAsyncTask(orderIdList, ipeIDList, ipeNameList).execute();

    }

    private class OrderAsyncTask extends AsyncTask<Void, Void, ArrayList<Order>> {
        ArrayList<String> orderIdList;
        ArrayList<String> ipeIDList;
        ArrayList<String> ipeNameList;

        public OrderAsyncTask(ArrayList<String> orderIdList, ArrayList<String> ipeIDList, ArrayList<String> ipeNameList){
            super();
            this.orderIdList = orderIdList;
            this.ipeIDList = ipeIDList;
            this.ipeNameList = ipeNameList;
        }

        @Override
        protected final ArrayList<Order> doInBackground(Void... params) {
            ArrayList<Order> myOrders = new ArrayList<Order>();
            mOrderTotals.clear();
            mOrderTimestamps.clear();
            mOrderItems.clear();

            try {
                for(int i=0; i<orderIdList.size();i++) {
                    Order myOrder = null;
                    if (orderIdList.get(i).equals("")) {
                        // order doesn't exist, create it
                        myOrder = orderConnector.createOrder(new Order());
                    } else {
                        // otherwise, retrieve it
                        myOrder = orderConnector.getOrder(orderIdList.get(i));
                    }
                    myOrder= orderConnector.updateOrder(myOrder);


                    Long iTotal = 0l;
                    Long time = Calendar.getInstance().getTimeInMillis();
                    Map<String, OrderItem> iOrderItems = new HashMap<String, OrderItem>();
                    int counter = 0;

                    //add all line items
                    for (int j = 0; j < lineItems.size(); j++) {
              //          System.out.println("Line items called:");
                        if (lineItems.get(j).get(1) == 9) {
              //              System.out.println("Line items2  called:" +donationAmount);
                            //add custom amount
                            // LineItem myLineItem = new LineItem();
                            //myLineItem.setPrice(donationAmount);
                            OrderItem mOrderItem = new OrderItem(donationName, "Custom", donationAmount, time ,0l,0l, false, false, false, false);
                            iOrderItems.put("item"+counter, mOrderItem);
                            iTotal = iTotal+donationAmount;
                            counter++;
                            // orderConnector.addCustomLineItem(myOrder.getId(), myLineItem, false);

                        } else {
               //             System.out.println("Line items 3 called:");
                            for (int k = 0; k < lineItems.get(j).get(2); k++) {
                //                System.out.println("Line items 4 called:");
                                String id = sharedPreferences.getString(1 + "_" + lineItems.get(j).get(0) + "button" + lineItems.get(j).get(1) + "_id", "error");
                                String name = sharedPreferences.getString(id + "_name", "");
                                String category = sharedPreferences.getString(id + "_category", "");
                                Long price = Long.parseLong(sharedPreferences.getString(id + "_price" + 1, "0"));
                                /*LineItem myLineItem = new LineItem();
                                myLineItem.setName(name);
                                myLineItem.setPrice(price);*/
                                OrderItem mOrderItem = new OrderItem(name, category, price, time ,0l,0l, false, false, false, false);
                                iOrderItems.put("item"+counter, mOrderItem);
                                iTotal = iTotal+price;
                                counter++;
                                // orderConnector.addCustomLineItem(myOrder.getId(), myLineItem, false);
                                //orderConnector.addFixedPriceLineItem(myOrder.getId(), sharedPreferences.getString(1 + "_" + lineItems.get(j).get(0) + "button" + lineItems.get(j).get(1) + "_id", "error"), null, null);
                            }
                        }
                    }
                    myOrder= orderConnector.updateOrder(myOrder);
                    myOrders.add(myOrder);


                    mOrderTotals.add(iTotal);
                    mOrderTimestamps.add(time);
                    mOrderItems.add(iOrderItems);


                }
            } catch (RemoteException e) {
                System.out.println("Order  ERROR1:"+e.getMessage());

            } catch (ClientException e) {
                System.out.println("Order  ERROR2:"+e.getMessage());

            } catch (ServiceException e) {
                System.out.println("Order  ERROR3:"+e.getMessage());

            } catch (BindingException e) {
                System.out.println("Order  ERROR4:"+e.getMessage());

            }

            return myOrders;
        }

        @Override
        protected final void onPostExecute(ArrayList<Order> orders) {
            // Populate the UI
     //       System.out.println("order: " + orders);

            String toastString = "";
     /*       ArrayList<String> mOrderIds = new ArrayList<String>();
            ArrayList<Long> mOrderTotals = new ArrayList<Long>();
            ArrayList<Long> mOrderTimestamps = new ArrayList<Long>();*/
/*
            ArrayList<Map<String, OrderItem>> mOrderItems = new ArrayList<Map<String, OrderItem>>();
*/
            for(int i=0;i<orders.size();i++) {
                toastString = toastString + orders.get(i).getId()+", ";
                mOrderIds.add(orders.get(i).getId());
              //  mOrderTotals.add(orders.get(i).getTotal());
              //  mOrderTimestamps.add(orders.get(i).getCreatedTime());
               /* List<LineItem> myItems = orders.get(i).getLineItems();
                Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                for (int j = 0; j < myItems.size(); j++) {
                    String name = myItems.get(j).getName();
                    Long price = myItems.get(j).getPrice();
                    OrderItem mOrderItem = new OrderItem(name, price, orders.get(i).getCreatedTime(), 0l, false);
                    items.put("item" + j, mOrderItem);
                }*/
                // mOrderItems.add(items);
            }

            Toast.makeText(getApplicationContext(), "Orders Updated:"+toastString, Toast.LENGTH_SHORT).show();
   //         System.out.println("order results: " + mOrderTotals);
   //         System.out.println("order results: " + mOrderTimestamps);
   //         System.out.println("order results: " + mOrderItems);
   //         System.out.println("order results: " + mOrderIds);
   //         System.out.println("order results: " + ipeIDList);
            mHelper.createOrUpdateIPEOrders("IPEOrders", mOrderTotals, mOrderTimestamps, mOrderItems, mOrderIds, ipeIDList, ipeNameList);
            resetScreens();
        }
    }

    private void prefsToLineItems(){
        lineItems.clear();
        ipeNums.clear();
        for(int i=0; i<sharedPreferences.getInt("item_size", 0); i++) {
            ArrayList<Integer> singleitem = new ArrayList<Integer>();
            singleitem.add(sharedPreferences.getInt(i + "item_page", 0));
            singleitem.add(sharedPreferences.getInt(i + "item_pos", 0));
            singleitem.add(sharedPreferences.getInt(i + "item_mult", 0));
            lineItems.add(singleitem);
        }
        for(int j=0; j<49; j++) {
            if (sharedPreferences.getBoolean("active" + j, false) == true){
                ipeNums.add(j);
                //System.out.println("ipeNums: "+ipeNums);
            }
        }
    }

    private void updateIPENumbs(){
        ipeNums.clear();
        for(int j=0; j<49; j++) {
            if (sharedPreferences.getBoolean("active" + j, false) == true){
                ipeNums.add(j);
              //  System.out.println("ipeNums: "+ipeNums);
            }
        }
    }


    private int valuesBelow(int value){
        int count = 0;
        for (Integer sets : mSet) {
            if(sets < value){
                count = count+1;
            }
        }
        System.out.println("values below: " + count);
        return count;
    }

 /*   private void getEmployees() {
        ArrayList<String> mNameList = new ArrayList<String>();
        Map<String,String> mMap = new HashMap<String, String>();
        mNameList = mDatabaseHelper.getArray("namelist");
        for(int i=0;i<mNameList.size();i++){
            mMap.put(mNameList.get(i), mDatabaseHelper.getString(mNameList.get(i)));
        }
        mIPEs = mMap;
        setupGrid();

        mHelper.retrieveIPENamesLight(new onGetEmployeeNamesLight() {
            @Override
            public void onStart() {
                //DO SOME THING WHEN START GET DATA HERE
            }

            @Override
            public void onSuccess(Map<String,String> data) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                ArrayList<String> mNameList = new ArrayList<String>();
                Iterator it = data.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    System.out.println("key pairs: "+pair.getKey() + " = " + pair.getValue());
                    mDatabaseHelper.storeString((String)pair.getKey(),(String) pair.getValue());
                    mNameList.add((String) pair.getKey());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                mDatabaseHelper.storeArray("namelist", mNameList);

                mIPEs = data;
              //  System.out.println("Main getipes: " + mIPEs);
                setupGrid();
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                System.out.println("failed to retrieve employees");
            }
        });
    }*/

    private void getEmployees() {
        //new getEmployees(this).execute();

        final Long start = System.currentTimeMillis();

  /*      ArrayList<String> mNameList = new ArrayList<String>();
        Map<String,String> mMap = new HashMap<String, String>();
        try {
            DB snappydb = DBFactory.open(mContext);
            String[] slist  =  snappydb.getArray("namelist", String.class);
            mNameList = new ArrayList<String>(Arrays.asList(slist));
            for(int i = 0; i<mNameList.size(); i++){
                mMap.put(mNameList.get(i), snappydb.get(mNameList.get(i)));
            }
            snappydb.close();

        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        mIPEs = mMap;
        setupGrid();*/

        final Long finish = System.currentTimeMillis();
        System.out.println("timestamp database: "+(finish-start));

        final Map<String,String> mEmployees = new HashMap<String, String>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference thisRef = database.getReference().child(mercID);
        DatabaseReference myRef = thisRef.child("EmployeeNames");
        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        //    System.out.println("filter datasnap" + dataSnapshot);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO figure out if finishsetup can be put in handler to help
                                for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                                    EmployeeLight mEmployee = mSnapshot.getValue(EmployeeLight.class);
                                    mEmployees.put(mEmployee.gesStageName(), mEmployee.gesUniqueID());
                                }
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        finishSetup(mEmployees);
                                    }
                                });
                            }
                        }).start();

                        Long finish2 = System.currentTimeMillis();
                        System.out.println("timestamp firebase: "+(finish2-finish)+" milliseconds");

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("error looking for employee names" + databaseError.toString());
                    }
        });

    }


    private class getEmployyees extends AsyncTask<Void, Void,  Map<String,String>> {
        private Context mContext;

        public getEmployyees(Context context){
            super();
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

                ArrayList<String> mNameList = new ArrayList<String>();
                Map<String,String> mMap = new HashMap<String, String>();
                mNameList = mDatabaseHelper.getArray("namelist");
                for(int i=0;i<mNameList.size();i++){
                    mMap.put(mNameList.get(i), mDatabaseHelper.getString(mNameList.get(i)));
                }
                mIPEs = mMap;
                setupGrid();

        }

        @Override
        protected Map<String,String> doInBackground(Void... params) {

           /* FirebaseHelper mHelper = new FirebaseHelper(mContext);
            mHelper.retrieveIPENamesLight(new onGetEmployeeNamesLight() {
                @Override
                public void onStart() {
                    //DO SOME THING WHEN START GET DATA HERE
                }

                @Override
                public void onSuccess(Map<String,String> data) {
                    //DO SOME THING WHEN GET DATA SUCCESS HERE
                    Map<String,String> mMap = new HashMap<String,String>();

                    ArrayList<String> mNameList = new ArrayList<String>();
                    Iterator it = data.entrySet().iterator();
                    SnappyHelper mDataHelper = new SnappyHelper(mContext);
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        System.out.println("key pairs: "+pair.getKey() + " = " + pair.getValue());
                        mDataHelper.storeString((String)pair.getKey(),(String) pair.getValue());
                        mNameList.add((String) pair.getKey());
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                    mDataHelper.storeArray("namelist", mNameList);
                    finishAsync(data);

                }

                @Override
                public void onFailed(DatabaseError databaseError) {
                    //DO SOME THING WHEN GET DATA FAILED HERE
                    System.out.println("failed to retrieve employees");
                }
            });*/
            final Map<String,String> mEmployees = new HashMap<String, String>();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference thisRef = database.getReference().child(mercID);
            DatabaseReference myRef = thisRef.child("EmployeeNames");
            myRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //    System.out.println("filter datasnap" + dataSnapshot);
                            for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                                EmployeeLight mEmployee = mSnapshot.getValue(EmployeeLight.class);
                                mEmployees.put(mEmployee.gesStageName(), mEmployee.gesUniqueID());
                            }
                            finishSetup(mEmployees);
                            Long finish = System.currentTimeMillis();
                            System.out.println("retrieve IPE duration: "+(finish-start)+" milliseconds");

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("error looking for employee names" + databaseError.toString());
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Map<String,String> data) {
            super.onPostExecute(data);

        }

    }



    public void finishSetup(Map<String,String> data){
        mIPEs = data;
        setupGrid();
    }


    public void setupGrid() {
        System.out.println("setup grid called");
        gridData.clear();
        IPESelector mSelector = new IPESelector(mContext);
        Set<String> mSet = new HashSet<String>();
        int i = 0;

        //If active add name to grid
        for (String name : mIPEs.keySet()) {
            if (sharedPreferences.getBoolean(mIPEs.get(name) + "active", false) == true) {
                gridData.add(name);
            //    System.out.println("active" + name);
            }
        }
        Collections.sort(gridData, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        //if active make name red with showEmployee, store active w/ #
        for(int k=0;k<gridData.size();k++){
            String name = gridData.get(k);
            mSelector.showEmployee(name, mIPEs.get(name), k);
            edt.putBoolean("active" + k, true);
            edt.commit();
            mSet.add(name);
        }
     //   System.out.println("active sorted" + gridData);

 /*       for (String id : mIPEs) {
            String name = sharedPreferences.getString(id + "name", "Click to Add:");
            if (sharedPreferences.getBoolean(id + "active", false) == true) {
                if(!mSet.contains(name)) {
                    gridData.set(i, name);
                    mSelector.showEmployee(name, id, i);
                    mSet.add(name);
                    edt.putBoolean("active"+i, true);


                    System.out.println("my set:" + mSet);
                    System.out.println("name: "+name+" position: "+i);
                    i++;
                }
                else{
                    edt.putBoolean("active"+i, false);
                }
            }
        }*/

        //automatically add any who are clockedbutweren't added manually
        for (String name : mIPEs.keySet()) {
            if (sharedPreferences.getBoolean(mIPEs.get(name) + "clocked", false) == true) {
                if (!mSet.contains(name)){
                gridData.add(name);
                mSelector.addEmployee(name, mIPEs.get(name), (gridData.size() - 1));
                mSet.add(name);
          //          System.out.println("grid filling clocked" + gridData);

                }
            }
        }

        //if added, add to grid, but not necessarily active
        for (String name : mIPEs.keySet()) {
            if (sharedPreferences.getBoolean(mIPEs.get(name) + "added", false) == true) {
                if (!mSet.contains(name)){
                    gridData.add(name);
                    mSelector.addEmployee(name, mIPEs.get(name), (gridData.size() - 1));
                    mSet.add(name);
          //          System.out.println("grid filling added" + gridData);
                }
            }
        }

/*            for (String id : mIPEs) {
            String name = sharedPreferences.getString(id + "name", "Click to Add:");
            if (sharedPreferences.getBoolean(id + "clocked", false) == true) {
                if(!mSet.contains(name)) {
                    gridData.set(i, name);
                    mSelector.addEmployee(name, id, i);
                    mSet.add(name);
                    System.out.println("my set:" + mSet);
                    i++;
                }
            }
        }
        for (String id : mIPEs) {
            String name = sharedPreferences.getString(id + "name", "Click to Add:");
            if (sharedPreferences.getBoolean(id + "added", false) == true) {
                if(!mSet.contains(name)) {
                    gridData.set(i, name);
                    mSelector.addEmployee(name, id, i);
                    mSet.add(name);
                    System.out.println("my set:" + mSet);
                    i++;
                }
            }
        }*/
        int fill = gridData.size();

        if(fill<49){
            for(int j=fill; j<49;j++){
                gridData.add("Click to Add");
                mSelector.clearGrid(j);
       //         System.out.println("click to add: " + j);
                i++;
            }
        }
/*        for(int i=0; i<49 ; i++){

            if(sharedPreferences.getString("name"+i, "Click to Add").equals("Click to Add")){
                gridData.set(i,"Click to Add");
            }
            else {
                gridData.set(i, sharedPreferences.getString("name" + i, "Click to Add") + " \n" + sharedPreferences.getString("lastfour" + i, "") + " \n" + sharedPreferences.getString("custom" + i, ""));
            }
        }*/

        customGridViewAdapter.notifyDataSetChanged();
    }


}