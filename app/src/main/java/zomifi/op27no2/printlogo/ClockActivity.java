package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.clover.sdk.v3.order.OrderConnector;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class ClockActivity extends Activity implements View.OnClickListener, CustomManagerListener
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private MerchantConnector merchantConnector;
    private EmployeeConnector mEmployeeConnector;
    private OrderConnector orderConnector;
    private CustomerConnector customerConnector;
    private static Context mContext;
    private Account account;
    private static FirebaseHelper mHelper;
    private static Handler handler = new Handler();

    private String mercID;
    private String editString = "";
    private EditText mEdit;
    private Spinner activeSpinner;

    private int mMode = 0;
    private Set<Integer> mSet = new HashSet<Integer>();
    private static int toggle = 2;
    private String spinnerMode = "all";

    private static FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference myRef;
    private ImageButton clearButton;

    Button modeButtons[] = new Button[4];
    private int page=1;
    private Boolean readyToScroll = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        mContext = this;
        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        mercID = prefs.getString("mercID","");
        //TODO AUTHENTICATE FIREBASE
        mHelper = new FirebaseHelper(mContext);
        mHelper.initialize();


        mMode = prefs.getInt("mMode",0);

        mEdit = (EditText) findViewById(R.id.search_bar);
        activeSpinner = (Spinner) findViewById(R.id.active_spinner);
        spinnerMode = prefs.getString("spinner_mode","All");
        selectValue(activeSpinner, spinnerMode);

        Button homeButton = (Button) findViewById(R.id.home_button);
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        ImageButton clearButton = (ImageButton) findViewById(R.id.clear);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(ClockActivity.this, NavigationActivity.class);
                ClockActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        /*addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomIPEDialog customDialog = new CustomIPEDialog(mContext, true, "");
                customDialog.setCancelable(false);
                customDialog.show();
            }
        });*/

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setText("");
            }
        });

        for(int i=0; i<3; i++) {
            int resID = getResources().getIdentifier("mode"+(i), "id", "zomifi.op27no2.printlogo");
            modeButtons[i] = ((Button) findViewById(resID));
        }
        setButtonColor(mMode);

        activeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                spinnerMode = activeSpinner.getSelectedItem().toString();
                edt.putString("spinner_mode",spinnerMode);
                edt.commit();
                System.out.println("Spinner mode: "+spinnerMode);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        mEdit.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                editString = s.toString();
                mAdapter.notifyDataSetChanged();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }
        });

        final RecyclerView recycler = (RecyclerView) findViewById(R.id.employee_recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        myRef = FirebaseDatabase.getInstance().getReference().child(mercID).child("Employees");

        mAdapter = new FirebaseRecyclerAdapter<Employee, EmployeeHolder>(Employee.class, zomifi.op27no2.printlogo.R.layout.list_item_employee, EmployeeHolder.class, myRef) {
            @Override
            public void populateViewHolder(EmployeeHolder employeeViewHolder, Employee employee, int position) {
                employeeViewHolder.setName(employee.gesName());

                if(prefs.getBoolean("mupdated"+employee.gesUniqueID(), false) == false) {
                    if (employee.gesName() != null && employee.gesUniqueID() != null) {
                        mHelper.addEmployeeNameList(employee.gesName(), employee.gesStageName(), employee.gesUniqueID(), employee.gesStatus());
                    }
                }

                if(employee.gesClocked() == true) {
                    employeeViewHolder.setText("Clocked In:      "+employee.gesLastTime());
                    employeeViewHolder.setButtonText("Clock Out");
                }
                else if(employee.gesClocked() == false){
                    employeeViewHolder.setText("Clocked Out");
                    employeeViewHolder.setButtonText("Clock In");
                }

                if(employee.gesStatus().equals("Active")) {
                    employeeViewHolder.setStatus("Active");
                }
                else if(employee.gesStatus().equals("Inactive")){
                    employeeViewHolder.setStatus("Inactive");
                }

                //conditions which will hide employee, adds to mSet which is counts offset for alternating gray/white list items
                if(!employee.gesName().toLowerCase().contains(editString.toLowerCase()) || (employee.gesClocked() && mMode ==2) || (!employee.gesClocked() && mMode ==1) || (employee.gesStatus().equals("Inactive") && spinnerMode.equals("Active")) || (employee.gesStatus().equals("Active") && spinnerMode.equals("Inactive"))) {
                    mSet.add(position);
                    employeeViewHolder.setVisibility(false);
                }
                else{
                    mSet.remove(position);
                    employeeViewHolder.setVisibility(true);
                    if((position - valuesBelow(position)) % 2 == 0){
                        employeeViewHolder.setBackgroundDark();
                    }
                    else{
                        employeeViewHolder.setBackgroundLight();
                    }
                }


            }
        };


        recycler.setAdapter(mAdapter);





    }



    public static class EmployeeHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CustomManagerListener{
        View mView;
        public Button myButton;
        public String myID;

        public EmployeeHolder(View itemView) {
            super(itemView);
            mView = itemView;
            myButton = (Button) itemView.findViewById(R.id.button1);
            myButton.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.text1);
            field.setText(name);
        }

        public void setVisibility(Boolean visible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)mView.getLayoutParams();
            if (visible){
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayout.LayoutParams.MATCH_PARENT;
                mView.setVisibility(View.VISIBLE);
            }else{
                mView.setVisibility(View.GONE);
                param.height = 0;
                param.width = 0;
            }
            mView.setLayoutParams(param);
        }

        public void setText(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text2);
            field.setText(text);
        }
        public void setStatus(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text3);
            field.setText(text);
        }
        public void setButtonText(String text) {
            myButton.setText(text);
        }

        public void setBackgroundDark(){
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.lines);
            layout.setBackgroundResource(R.drawable.lightgray_button);
        }
        public void setBackgroundLight(){
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.lines);
            layout.setBackgroundResource(R.drawable.whitegray_button);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Employee mEmployee = (Employee) mAdapter.getItem(position);
            myID = mEmployee.gesUniqueID();
            Long time = Calendar.getInstance().getTimeInMillis();

            if(v.getId() == myButton.getId()){
                System.out.println("button");

                DatabaseReference myRef = mAdapter.getRef(position);
                if(mEmployee.gesClocked() == true) {
                    //clock employee out
                    mHelper.clockOut(myRef, mEmployee.gesName(), myID, time);
                }
                else if(mEmployee.gesClocked() == false){
                    //clock employee in
                    mHelper.clockIn(myRef,mEmployee.gesName(), myID, time);
                }
               // toggle = 0;
                notifyData();


            }
            else{
                System.out.println("not button");
                CustomManagerDialog managerDialog = new CustomManagerDialog(mContext, false);
                managerDialog.setCustomManagerListener(this);
                managerDialog.setButtonID(v.getId());
                managerDialog.show();
            }
        }

        @Override
        public void managerCallback(int resourceID) {
            //show employee dialog - false means not new
            CustomIPEDialog customDialog = new CustomIPEDialog(mContext, false, myID);
            customDialog.setCancelable(false);
            customDialog.show();
        }
    }

    public void buttonClicked(View view) {
        for(int i=0;i<3;i++) {
            if (view.getId() == modeButtons[i].getId()) {
                mMode = i;
                edt.putInt("mMode",i);
                edt.commit();
                setButtonColor(i);
            }
        }
        mAdapter.notifyDataSetChanged();

    }

    public void setButtonColor(int mode){
        for(int i=0;i<3;i++){
            if(i==mode) {
                modeButtons[i].setBackgroundResource(R.drawable.red_button);
            }
            else{
                modeButtons[i].setBackgroundResource(R.drawable.aqua_green_button);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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



    @Override
    protected void onPause()
    {

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        mAdapter.cleanup();
        super.onDestroy();

    }

    private void connect() {
        disconnect();
        System.out.println("Connect Called");

        if (account != null) {
            merchantConnector = new MerchantConnector(this, account, new ServiceConnector.OnServiceConnectedListener()
            {

                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> connector)
                {

                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {

                }
            });
            merchantConnector.connect();

            mEmployeeConnector = new EmployeeConnector(this, account, new ServiceConnector.OnServiceConnectedListener()
            {

                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> connector)
                {

                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {

                }
            });
            mEmployeeConnector.connect();

            customerConnector = new CustomerConnector(this, account, null);
            customerConnector.connect();

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
        if(v.getId() == R.id.add_button);{
            CustomManagerDialog managerDialog = new CustomManagerDialog(mContext, false);
            managerDialog.setCustomManagerListener(this);
            managerDialog.setButtonID(R.id.add_button);
            managerDialog.show();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    private static void notifyData(){
        Runnable mrunnable = new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        };
        handler.postDelayed(mrunnable, 300);
    }


    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


    @Override
    public void managerCallback(int resourceID) {
        if(resourceID == R.id.add_button){
            CustomIPEDialog customDialog = new CustomIPEDialog(mContext, true, "");
            customDialog.setCancelable(false);
            customDialog.show();
        }
    }



}