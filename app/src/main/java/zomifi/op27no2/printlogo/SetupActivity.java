package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class SetupActivity extends Activity implements View.OnClickListener {
    private SharedPreferences prefs;
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
    private boolean navigateAway = false;
    private boolean building = false;

    private LinearLayout buttonLayout;
    private LinearLayout customerLayout;

    private String PRICE_STRING = "";
    private String orderID;

    private int TRIGGER = 0;
    private int ITEM_CODE = 2;

    private static final int SECURE_PAY_REQUEST_CODE = 1;
    private Boolean LOCKED = false;
    private Boolean TOUCH_ACTIVE = false;

    private boolean isOpened = false;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    Button buttons[] = new Button[16];

    private Button barButton;
    private Button doorButton;
    private Button ipeButton;

    private ImageButton bRight;
    private ImageButton bLeft;
    private int cPage = 1;
    private int cMode;
    private TextView pageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(zomifi.op27no2.printlogo.R.layout.setup_activity);

        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

        mContext = this;
        cPage = prefs.getInt("currentPage",1);
        cMode = prefs.getInt("mode",1);


        TRIGGER = 0;
        LOCKED = true;

        buttonLayout = (LinearLayout) findViewById(zomifi.op27no2.printlogo.R.id.items_view);
       // customerLayout.setVisibility(View.GONE);

        barButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.bar_button);
        doorButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.door_button);
        ipeButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.ipe_button);
        Button homeButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.home_button);
        Button backButton = (Button) findViewById(zomifi.op27no2.printlogo.R.id.select_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SetupActivity.this, NavigationActivity.class);
                SetupActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.ipe_button){
                    edt.putInt("mode",1);

                }

                if(v.getId() == R.id.door_button){
                    edt.putInt("mode",3);

                }
                if(v.getId() == R.id.bar_button){
                    edt.putInt("mode",2);
                }
                edt.commit();
                setUpButtonColors();

            }
        };
        barButton.setOnClickListener(mOnClickListener);
        doorButton.setOnClickListener(mOnClickListener);
        ipeButton.setOnClickListener(mOnClickListener);


        bRight = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bright);
        bLeft = (ImageButton) findViewById(zomifi.op27no2.printlogo.R.id.bleft);
        pageText = (TextView) findViewById(R.id.pages);

        for(int i=0; i<15; i++) {
                String buttonID = "button" + (i+1);
                int resID = getResources().getIdentifier(buttonID, "id", "zomifi.op27no2.printlogo");
                buttons[i] = ((Button) findViewById(resID));
                buttons[i].setOnClickListener(this);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
            }
        });


            bLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cPage > 1) {
                        cPage = cPage - 1;
                        edt.putInt("currentPage", cPage);
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
        setUpButtonColors();

    }


    private void setUpButtonColors(){
        cMode = prefs.getInt("mode",1);
        barButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.gray_button);
        ipeButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.gray_button);
        doorButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.gray_button);
        if(cMode == 1){
            ipeButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
            for(int i=8;i<15;i++){
                buttons[i].setVisibility(View.GONE);
            }
        }
        else{
                for(int i=8;i<15;i++){
                    buttons[i].setVisibility(View.VISIBLE);
                }
        }
        if(cMode == 3){
            doorButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
        }
        if(cMode == 2){
            barButton.setBackgroundResource(zomifi.op27no2.printlogo.R.drawable.red_button);
        }
        setUpButtonNames(cPage);
    }

    private void setUpButtonNames(int currentPage) {
         pageText.setText("Page "+currentPage);
         cMode = prefs.getInt("mode",1);

        for(int i=0;i<15;i++){
            String id = prefs.getString(cMode+"_"+currentPage+"button"+(i+1)+"_id", "");
            buttons[i].setText(prefs.getString(id+"_name", "Add from Settings") + "\n" + formatPrice(prefs.getString(id+"_price"+cMode, "0")));
        }
    }

    @NonNull
    private String formatPrice(String PRICE_STRING) {
        if(PRICE_STRING == ""){
            PRICE_STRING = "0";
        }

        //default USD from prefs
        String currency = prefs.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));
        long value = Long.valueOf(PRICE_STRING);
        String price = mCurrencyFormat.format(value / 100.0);
        return price;

    }

    @Override
    protected void onResume() {
        super.onResume();
        building = false;
        System.out.println("OnResume Called, locked: " + LOCKED);
        LOCKED = true;

        setUpButtonColors();

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
        getEmployee();

    }


    private void getEmployee() {
        System.out.println("GETEMPLOYEE CALLED");

        new AsyncTask<Void, Void, List<Employee>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected List<Employee> doInBackground(Void... params) {
                List<Employee> employees = null;

                try {
                    employees = mEmployeeConnector.getEmployees();


                } catch (RemoteException e) {


                } catch (ClientException e) {


                } catch (ServiceException e) {


                } catch (BindingException e) {


                }
                return employees;
            }

            @Override
            protected void onPostExecute(List<Employee> employees) {
                super.onPostExecute(employees);

                if (!isFinishing()) {

                }
            }
        }.execute();
    }


    @Override
    protected void onPause() {
        disconnect();
        System.out.println("OnPause Called" + LOCKED);

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

    @Override
    public void onClick(View v)
    {
        for(int i=0; i<15; i++) {
            if(v.getId() == getResources().getIdentifier("button"+(i+1), "id", "zomifi.op27no2.printlogo")) {
                edt.putInt("recentButton", i+1);
                edt.commit();
                loadOrder();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ITEM_CODE/* && resultCode == RESULT_OK*/) {
            System.out.println("test1" + data);
            setItemName();

        } else {
            System.out.println("onActivity Result no result");
        }
    }


    public void startIntent(){
        Intent intent = new Intent("com.clover.intent.action.ITEM_SELECT");
        intent.putExtra("com.clover.intent.extra.ORDER_ID", orderID);
        startActivityForResult(intent, 2);

    }

    private void loadOrder() {
        new OrderAsyncTask().execute();
    }

    private void setItemName(){
        System.out.println("setitemname called");
        if(inventoryConnector!=null) {
            inventoryConnector.disconnect();
        }
        inventoryConnector = new InventoryConnector(this, this.account, new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
                if (inventoryConnector != null) {
                    System.out.println("inventory notnull");
                    new ItemDetailAsyncTask().execute();
                }
                else{
                    System.out.println("inventory null?");
                }
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
            }
        });
        inventoryConnector.connect();

    }

    private class ItemDetailAsyncTask extends AsyncTask<Void, Void, Item> {

        @Override
        protected final Item doInBackground(Void... params) {

            try {

                Item mItem = inventoryConnector.getItem(prefs.getString("recentItemId","default"));

                return mItem;
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

            System.out.println("Order CREATTTED NULL");
            return null;
        }

        @Override
        protected final void onPostExecute(Item item) {
            if(item !=null) {

                // Populate the UI
                int pos = prefs.getInt("recentButton", 0);

                String itemname = item.getName();
                Long itemprice = item.getPrice();

                System.out.println(" ITEM NAME:" + itemname);
                String recentid = prefs.getString("recentItemId", "default");

                edt.putString(cMode + "_" + cPage + "button" + pos + "_id", recentid);
                System.out.println("stored: " + cMode + "_" + cPage + "button" + pos + "_id" + " " + recentid);

                if (prefs.getString(recentid + "_name", "").equals("")) {
                    edt.putString(recentid + "_name", itemname);
                    System.out.println("inital name set:" + recentid);
                }

                if (prefs.getString(recentid + "_price1", "").equals("")) {
                    System.out.println("inital price1 set:" + recentid);
                }
                if (prefs.getString(recentid + "_price2", "").equals("")) {
                    System.out.println("inital price2 set:" + recentid);
                }
                if (prefs.getString(recentid + "_price3", "").equals("")) {
                    System.out.println("inital price3 set:" + recentid);
                }

                //edt.putString(cMode+"_"+cPage+"button" + pos + "_name", itemname);
                //edt.putString(cMode+"_"+cPage+"button"+pos+"_price", formatPrice(Long.toString(itemprice)));
                edt.commit();
                System.out.println("button" + pos + "_name: " + itemname);

                itemDialog(pos);
//            setUpButtonNames(cPage);
            }
        }
    }

    private void itemDialog(int position){
        CustomItemDialog2 customDialog = new CustomItemDialog2(mContext, position, cPage, cMode);
        customDialog.setCancelable(false);
        customDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setUpButtonNames(cPage);
            }
        });
        customDialog.show();

    }

    private class OrderAsyncTask extends AsyncTask<Void, Void, Order> {

        @Override
        protected final Order doInBackground(Void... params) {

            try {
                Order mOrder = orderConnector.createOrder(new Order());
                System.out.println("Order CREATTTED!: "+mOrder.getId());
                return mOrder;
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

            System.out.println("Order CREATTTED NULL");
            return null;
        }

        @Override
        protected final void onPostExecute(Order order) {
            // Populate the UI

            orderID = order.getId();
            System.out.println("CREATED ORDER ID:"+orderID);
            startIntent();
        }
    }


}