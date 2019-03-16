package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantAddress;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;

import java.util.List;
import java.util.TimeZone;

/**
 * Created by CristMac on 7/2/16.
 */
public class NavigationActivity extends Activity implements View.OnClickListener{

    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private MerchantConnector merchantConnector;
    private EmployeeConnector mEmployeeConnector;
    private Account account;
    private Context mContext;

    private static String  mercID ="default";
    private static String  mEmail = "do data";
    private static String  mAdd= "no data";
    private static String  mCity = "no data";
    private static String  mState= "no data";
    private static String  mCountry= "no data";
    private static String  mNumber= "no data";
    private static String  mZip= "no data";
    private static String  mTimezone= "no data";
    private static String  mName= "no data";

    private Button clockButton;
    private Button doorButton;
    private Button ipesButton;
    private Button barButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(zomifi.op27no2.printlogo.R.layout.activity_navigation);
        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        mContext = this;

        clockButton = (Button) findViewById(R.id.clockin_button);
        doorButton = (Button) findViewById(R.id.bar_button);
        ipesButton = (Button) findViewById(R.id.ipes_button);
        barButton = (Button) findViewById(R.id.door_button);
        settingsButton = (Button) findViewById(R.id.settings_button);
        clockButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(NavigationActivity.this, ClockActivity.class);
                NavigationActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        doorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(NavigationActivity.this, DoorActivity.class);
                NavigationActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        ipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(NavigationActivity.this, ItemsActivity.class);
                NavigationActivity.this.startActivity(myIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        barButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(NavigationActivity.this, BarActivity.class);
                NavigationActivity.this.startActivity(myIntent);
                // overridePendingTransition(R.anim.pull_in_bottom, R.anim.push_out_top);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(NavigationActivity.this, SettingsActivity.class);
                NavigationActivity.this.startActivity(myIntent);
                // overridePendingTransition(R.anim.pull_in_bottom, R.anim.push_out_top);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

    }

    @Override
    protected void onResume( ) {
        super.onResume();
        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(this);
            if (account == null) {
                Toast.makeText(this, getString(zomifi.op27no2.printlogo.R.string.no_account), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        connect();
        getEmployee();
        getMerchant();
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
                    getMerchant();
                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                }
            });
            merchantConnector.connect();

            mEmployeeConnector = new EmployeeConnector(this, account, new ServiceConnector.OnServiceConnectedListener()
            {
                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> connector){
                        getEmployee();
                }
                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                }
            });
            mEmployeeConnector.connect();

        }
        else {
            System.out.println("Account not connected.");
        }
    }
    private void getMerchant() {
        System.out.println("GETMERCHANT CALLED");

        new AsyncTask<Void, Void, Merchant>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();


            }

            @Override
            protected Merchant doInBackground(Void... params) {
                Merchant merchant = null;
                try {
                    merchant = merchantConnector.getMerchant();
                    MerchantAddress mAddress = merchant.getAddress();
                    mAdd = mAddress.getAddress1()+" "+mAddress.getAddress2()+" "+mAddress.getAddress3();
                    mCity = mAddress.getCity();
                    mState = mAddress.getState();
                    mZip = mAddress.getZip();
                    mCountry = mAddress.getCountry();
                    TimeZone tz = merchant.getTimeZone();
                    mTimezone = tz.getDisplayName();
                    mName = merchant.getName();
                    mNumber = merchant.getPhoneNumber();
                    mercID = merchant.getId();

                    edt.putString("currencyCode", merchant.getCurrency().getCurrencyCode());
                    //edt.putString("mercID", "EWK49MD4D7GFC");
                    edt.putString("mercID", mercID);
                    edt.commit();

                    infoexecute();


                } catch (RemoteException e) {

                } catch (ClientException e) {

                } catch (ServiceException e) {

                } catch (BindingException e) {

                }
                return merchant;
            }

            @Override
            protected void onPostExecute(Merchant merchant) {
                super.onPostExecute(merchant);
            }
        }.execute();
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

                    for (Employee employee : employees) {
                        if (employee.getIsOwner()) {
                            mEmail = employee.getEmail();
                            System.out.println("EMAIL: "+mEmail);
                        }
                    }

                    infoexecute2();



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

    public static void infoexecute() {
       /* DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://zoomifi-app-installs.firebaseio.com/DejaVu/" + mercID);
        Firebase ref = new Firebase("https://zoomifi-app-installs.firebaseio.com/EasyOrder/" + mercID);
        ref.child("merchantid").setValue(mercID);
        ref.child("zipcode").setValue(mZip);
        ref.child("timezone").setValue(mTimezone);
        ref.child("phone").setValue(mNumber);
        ref.child("merchantName").setValue(mName);
        ref.child("address").setValue(mAdd);
        ref.child("city").setValue(mCity);
        ref.child("state").setValue(mState);
        ref.child("county").setValue(mCountry);*/
    }
    public static void infoexecute2() {
       /* Firebase ref = new Firebase("https://zoomifi-app-installs.firebaseio.com/EasyOrder/" + mercID);
        ref.child("email").setValue(mEmail);
*/
    }










    @Override
    protected void onPause( ) {
        super.onPause();

    }
    @Override
    protected void onDestroy( ) {
        super.onDestroy();

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
    }

    @Override
    public void onClick(View v) {

    }


}
