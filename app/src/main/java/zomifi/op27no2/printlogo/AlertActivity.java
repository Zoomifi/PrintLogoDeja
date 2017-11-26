package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CristMac on 1/10/16.
 */
public class AlertActivity extends Activity {
    private Account account;
    private MerchantConnector merchantConnector;
    private String mercID;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



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

        // Get the merchant object
        getMerchant();

    }
    private void displayAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have not yet set up a logo for the Print Your Logo app. Please email your logo to support@zoomifi.com").setCancelable(
                false).setPositiveButton("Dismiss",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                }).setNegativeButton("Disable Future Notifications",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        // DISABLE FUTURE NOTIFICATIONS!!
                        ComponentName receiver = new ComponentName(AlertActivity.this, BootReceiver.class);
                        PackageManager pm = getPackageManager();

                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);

                        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(AlertActivity.this, AlarmReceiver.class);
                        alarmIntent = PendingIntent.getBroadcast(AlertActivity.this, 0, intent, 0);
                        alarmMgr.cancel(alarmIntent);
                        finish();

                    }
                });
        AlertDialog alert = builder.create();
        if (!isFinishing()) {
            alert.show();
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

                    mercID = merchant.getId();


                } catch (RemoteException e) {
                    Toast.makeText(getApplicationContext(), "remote exception2", Toast.LENGTH_LONG).show();

                } catch (ClientException e) {
                    Toast.makeText(getApplicationContext(), "client exception2", Toast.LENGTH_LONG).show();

                } catch (ServiceException e) {
                    Toast.makeText(getApplicationContext(), "service exception2", Toast.LENGTH_LONG).show();
                } catch (BindingException e) {
                    Toast.makeText(getApplicationContext(), "binding exception2", Toast.LENGTH_LONG).show();
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
                        checkImage();

                    }


                }
            }
        }.execute();
    }

    private void checkImage(){
        final String myurl = "https://zoomifi-logo.s3.amazonaws.com/"+mercID+"/logo.png";
        /*if(!exists(myurl)){
            displayAlert();
        }*/
        final Handler mHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                //Display Alert
                displayAlert();

            }
        };

        new Thread() {

            public void run() {
                //your "file checking code" goes here like this
                //write your results to log cat, since you cant do Toast from threads without handlers also...

                try {
                    HttpURLConnection.setFollowRedirects(false);
                    // note : you may also need
                    //HttpURLConnection.setInstanceFollowRedirects(false)

                    HttpURLConnection con =  (HttpURLConnection) new URL(myurl).openConnection();
                    con.setRequestMethod("HEAD");
                    if( (con.getResponseCode() == HttpURLConnection.HTTP_OK) ) {
                        System.out.println("IMAGE EXISTS");
                        finish();
                    }
                    else{
                        System.out.println("IMAGE DOES NOT EXIST");
                        mHandler.sendEmptyMessage(0);

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }.start();

    }

    private void connect() {
        disconnect();

        if (account != null) {

            merchantConnector = new MerchantConnector(this, account, null);
            merchantConnector.connect();

        }

    }

    private void disconnect() {

        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean exists(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void makeToast(String message){
        final Handler mHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                Toast.makeText(getApplicationContext(), "remote exception2", Toast.LENGTH_LONG).show();
            }
        };
    }
}

