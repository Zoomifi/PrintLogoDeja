package zomifi.op27no2.printlogo;
//
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantAddress;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.printer.ReceiptRegistrationConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.firebase.client.Firebase;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends Activity {
        private SharedPreferences prefs;
        private SharedPreferences.Editor edt;
        private Account account;
        private ReceiptRegistrationConnector connector;
        private MerchantConnector merchantConnector;
        private EmployeeConnector mEmployeeConnector;

        private Context mContext;
        private ImageView im1;
        private static String  mercID ="default";
        private static String  mEmail = "do data";
        private static String  mCloverID;
        private static String  mAdd= "no data";
        private static String  mCity = "no data";
        private static String  mState= "no data";
        private static String  mCountry= "no data";
        private static String  mNumber= "no data";
        private static String  mZip= "no data";
        private static String  mTimezone= "no data";
        private static String  mName= "no data";
        private TextView mtext;
        private TextView stext;
        private TextView merchanttext;
        private String myurl;
        private Switch mSwitch;
        private SeekBar seek1;
        private Boolean active;
        private Boolean tMerchant = false;
        private Boolean tEmployee = false;
        private Firebase myFirebaseRef;
        private String[] dejaIDs = {"T5DSG9F56XBH4","GYBR6EF1WX9VT","82XMR6HYERFEP","Q3PE9R1YYFV10","YAJ2RH7QFRBKY","YSAAWCE2DQD1G","QZDKVW8GJTJP2","0XXN388XKTD5E","EWK49MD4D7GFC","14VF8GY0SC44Y","SXSC2FP357J26", "XHEGZQ2GPFWT0", "97BD6F1VNAP9Y"};

    HttpURLConnection httpcon;
        String url = null;
        String data = null;
        String result = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mContext = this;

            Firebase.setAndroidContext(this);
            myFirebaseRef = new Firebase("https://zoomifi-app-installs.firebaseio.com/");

            prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();


            im1 = (ImageView) findViewById(R.id.receipt_logo);
            mtext = (TextView) findViewById(R.id.background_text);
            stext = (TextView) findViewById(R.id.scaletext);
            merchanttext = (TextView) findViewById(R.id.merc);
            mSwitch = (Switch) findViewById(R.id.mySwitch);

            active = prefs.getBoolean("active", false);
            System.out.println("ONSTART ACTIVE ="+active);
            mSwitch.setChecked(active);
            seek1 = (SeekBar) findViewById(R.id.scale);
            seek1.setProgress(prefs.getInt("scalepercent",100));
            stext.setText("Logo Size: " + prefs.getInt("scalepercent", 100) + "%");
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        active = true;
                        connect();
                        edt.putBoolean("active", true);
                        edt.commit();
                    } else {
                        active = false;
                        if (connector != null) {
                            unregisterReceiptRegistration();
                        }
                        edt.putBoolean("active", false);
                        edt.commit();
                    }
                }
            });
            seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // TODO Auto-generated method stub
                    int perc = progress;
                    if(perc<10){
                        perc = 10;
                    }
                    stext.setText("Logo Size: "+perc+"%");
                    edt.putInt("scalepercent", perc);
                    edt.commit();
                    ReceiptRegistrationProvider.passMercID(mercID, mContext);
                }
            });

            //PRINT BUTTON FOR TESTING
      /*      buttonPrint = (Button) findViewById(R.id.button_print);
            buttonPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Set the add on text and print the last order's bill receipt
                    new MyAsyncTask().execute();
                }
                    *//*@Override
                    public void onClick(View view) {
                        final String url = "http://s3-media4.fl.yelpcdn.com/bphoto/auBUquTp9M9-eDdtqkI97g/o.jpg";


                        if (TextUtils.isEmpty(url)) {
                            return;
                        }

                        new AsyncTask<Void, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(Void... voids) {
                                try {
                                    InputStream is = (InputStream) new URL(url).getContent();
                                    Bitmap b = BitmapFactory.decodeStream(is);
                                    return b;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                if (bitmap == null) {
                                    return;
                                }
                                new ImagePrintJob.Builder().bitmap(bitmap).maxWidth().build().print(MainActivity.this, account);
                            }
                        }.execute();
                    }*//*
            });
            System.out.println("ONCREATE APPTEST");

*/
        }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    public void setImage(){

        myurl = "https://zoomifi-logo.s3.amazonaws.com/"+mercID+"/logo.png";

        if(isNetworkConnected()) {
            System.out.println("ISCONNECTED");
            Picasso.with(this)
                    .load(myurl)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .fit()
                    .centerInside()
                    .into(im1, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mtext.setText("");
                        }

                        @Override
                        public void onError() {
                            mtext.setText("No Image");
                        }
                    });
        } else {
            System.out.println("IS NOT CONNECTED");

            Picasso.with(this)
                    .load(myurl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .fit()
                    .into(im1, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mtext.setText("");
                        }

                        @Override
                        public void onError() {
                            mtext.setText("No Image");
                        }
                    });
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
        active = prefs.getBoolean("active", false);
        System.out.println("ONRESUME ACTIVE ="+active);

        // Create and Connect
        connect();

        getEmployee();
        // Get the merchant object
        getMerchant();

    }
    public Bitmap getImageBitmap(Context context,String name,String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    private void getMerchant() {
        System.out.println("GETMERCHANT CALLED2");

        new AsyncTask<Void, Void, Merchant>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                System.out.println("GETMERCHANT PRE EXECUTE");


            }

            @Override
            protected Merchant doInBackground(Void... params) {
                Merchant merchant = null;
                System.out.println("GETMERCHANT BACKGROUND");

                try {
                    merchant = merchantConnector.getMerchant();
                   /* List<Employee> employees = mEmployeeConnector.getEmployees();
                    for (Employee employee : employees) {
                        if (employee.getIsOwner()) {
                            mEmail = employee.getEmail();
                        }
                    }*/
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
                    System.out.println("merchant ID: "+mercID);


                    for(int i=0;i<dejaIDs.length;i++){
                        if(mercID !=null) {
                            if (dejaIDs[i].equals(mercID)) {
                                Intent myIntent = new Intent(MainActivity.this, NavigationActivity.class);
                                MainActivity.this.startActivity(myIntent);
                                finish();
                            }
                        }
                    }

                    // either merchant or employee will finish first, check here and send info
                    tMerchant = true;
                    if(tMerchant==true && tEmployee==true ){
                        System.out.println("preexecute");

                        infoexecute();
                        edt.putBoolean("firstrun",false);
                        edt.commit();
                    }

                } catch (RemoteException e) {
                    System.out.println("RemoteException error: "+e.getMessage());


                } catch (ClientException e) {
                    System.out.println("ClientException error: "+e.getMessage());


                } catch (ServiceException e) {
                    System.out.println("ServiceException error: "+e.getMessage());


                } catch (BindingException e) {
                    System.out.println("BindingException error: "+e.getMessage());


                }
                return merchant;
            }

            @Override
            protected void onPostExecute(Merchant merchant) {
                super.onPostExecute(merchant);

                if (!isFinishing()) {
                    System.out.println("GETMERCHANT INFO");

                    // Populate the merchant information
                    if (merchant != null) {

                        ReceiptRegistrationProvider.passMercID(mercID, mContext);
                        merchanttext.setText("This is your Merchant ID: "+mercID);
                        setImage();

                    }


                }
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
                    tEmployee = true;
                    if(tMerchant==true && tEmployee==true ){

                        infoexecute();
                        edt.putBoolean("firstrun",false);
                        edt.commit();
                    }

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
        Firebase ref = new Firebase("https://zoomifi-app-installs.firebaseio.com/PrintLogo/" + mercID);
        ref.child("email").setValue(mEmail);
        ref.child("merchantid").setValue(mercID);
        ref.child("zipcode").setValue(mZip);
        ref.child("timezone").setValue(mTimezone);
        ref.child("phone").setValue(mNumber);
        ref.child("merchantName").setValue(mName);
        ref.child("address").setValue(mAdd);
        ref.child("city").setValue(mCity);
        ref.child("state").setValue(mState);
        ref.child("county").setValue(mCountry);

        Map<String, String> comment = new HashMap<String, String>();
        comment.put("email", mEmail);
        comment.put("merchantID", mercID);
        comment.put("zipcode", mZip);
        comment.put("timezone", mTimezone);
        comment.put("phone", mNumber);
        comment.put("merchantName", mName);
        comment.put("address", mAdd);
        comment.put("city", mCity);
        comment.put("state", mState);
        comment.put("country", mCountry);
        comment.put("cloverID", "KJY6ST0D4YQE6");
        String json = new GsonBuilder().create().toJson(comment, Map.class);
     //   makeRequest("https://ops.zoomifi.com/appinstall.php", json);
    }
/*    public static HttpResponse makeRequest(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            return new DefaultHttpClient().execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    private void registerReceiptRegistration() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Create and Connect
                connector.register(Uri.parse(ReceiptRegistrationProvider.CONTENT_URI_IMAGE.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());

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
                connector.unregister(Uri.parse(ReceiptRegistrationProvider.CONTENT_URI_IMAGE.toString()), new ReceiptRegistrationConnector.ReceiptRegistrationCallback<Void>());
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
    protected void onPause() {
        super.onPause();
        // disconnect();
    }



    private void connect() {
        disconnect();
        if (account != null) {
            connector = new ReceiptRegistrationConnector(this, account, new ServiceConnector.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> serviceConnector) {
                    if (connector != null) {

                        if (prefs.getBoolean("firstrun", true)) {
                            unregisterReceiptRegistration();
                            edt.putBoolean("firstrun", false);
                            edt.commit();
                        }
                        if (active) {
                            System.out.println("REGISTER CALLED");
                            registerReceiptRegistration();

                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> serviceConnector) {
                }
            });
            connector.connect();

        }
        if (account != null) {

            merchantConnector = new MerchantConnector(this, account, null);
            merchantConnector.connect();

        }
        if (account != null) {
            mEmployeeConnector = new EmployeeConnector(this, account, null);
            mEmployeeConnector.connect();
        }
    }

    private void disconnect() {
        if (connector != null) {
            connector.disconnect();
            connector = null;
        }
        if (mEmployeeConnector != null) {
            mEmployeeConnector.disconnect();
            mEmployeeConnector = null;
        }
        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }

    }











}