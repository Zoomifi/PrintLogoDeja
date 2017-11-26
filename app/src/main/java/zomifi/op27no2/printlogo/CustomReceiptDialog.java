package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.printer.job.PrintJob;
import com.clover.sdk.v1.printer.job.StaticBillPrintJob;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomReceiptDialog extends Dialog
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private OrderConnector orderConnector;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private String  headerText;
    private String  mercId;
    private String  orderId;
    private String  email;
    private String  phoneNumber;


    private Context context;
    private Button cancelButton;
    private Button printButton;
    private Button textButton;
    private Button emailButton;
    private Button submitButton;
    private Button backButton;

    private LinearLayout textLayout;
    private LinearLayout emailLayout;
    private RelativeLayout receiptLayout;

    private EditText emailEdit;
    private EditText textEdit;
    private Order mOrder = null;

    private String requestType = "";

    public CustomReceiptDialog(Context context, String mercId, String orderId, String email, String phoneNumber)
    {
        super(context);

        this.context = context;
        this.mercId = mercId;
        this.orderId = orderId;
        this.email = email;
        this.phoneNumber = phoneNumber;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_receipt);


        ((TextView)findViewById(R.id.customDialogHeader)).setText(this.headerText);
        prefs = context.getSharedPreferences(context.getString(R.string.donation_here_file), Context.MODE_PRIVATE);
        edt = prefs.edit();
        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }
        orderConnector = new OrderConnector(context, account, new ServiceConnector.OnServiceConnectedListener()
        {

            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> connector)
            {
                try {
                    mOrder = new GetOrderAsync().execute(orderId).get();
                } catch (InterruptedException e) {
                    Toast.makeText(context, "Error Retrieving Order", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Toast.makeText(context, "Error Retrieving Order", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {

            }
        });
        orderConnector.connect();

        receiptLayout = (RelativeLayout) findViewById(R.id.receiptlayout);
        textLayout = (LinearLayout) findViewById(R.id.textlayout);
        emailLayout = (LinearLayout) findViewById(R.id.emaillayout);

        emailEdit = (EditText) findViewById(R.id.email_edittext);
        textEdit = (EditText) findViewById(R.id.text_edittext);
        backButton = (Button) findViewById(R.id.backButton);
        submitButton = (Button) findViewById(R.id.submitButton);
        emailButton = (Button) findViewById(R.id.emailreceipt);
        cancelButton = (Button) findViewById(R.id.customDialogCancelButton);

        emailLayout.setVisibility(View.GONE);
        textLayout.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.VISIBLE);
        receiptLayout.setVisibility(View.VISIBLE);

        printButton = (Button) findViewById(R.id.printreceipt);
        textButton = (Button) findViewById(R.id.textreceipt);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiptLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                emailLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = textEdit.getText().toString();
                email = emailEdit.getText().toString();
                getAuth();
                dismiss();
            }
        });


        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   new ReceiptPrintJob.Builder().orderId(orderId).build().print(context, account);

                if(mOrder != null) {
                    PrintJob pj = new StaticBillPrintJob.Builder().order(mOrder).build();
                    pj.print(context, CloverAccount.getAccount(context));
                }
                else{
                    Toast.makeText(context, "Error Order Does Not YET Exist", Toast.LENGTH_LONG).show();
                }

                dismiss();
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEdit.setText(phoneNumber);
                requestType  = "text";
                textLayout.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                receiptLayout.setVisibility(View.GONE);

            }
        });
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEdit.setText(email);
                requestType  = "email";
                emailLayout.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                receiptLayout.setVisibility(View.GONE);

            }
        });


        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (5 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }
    public void setHeaderText(String headerText)
    {
        this.headerText = headerText;
    }




    private void getAuth() {
        System.out.println("GETAUTH CALLED");

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                CloverAuth.AuthResult authResult = null;
                {
                    try {
                        authResult = CloverAuth.authenticate(context, account);
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
        paymentsUri = "/v2/merchant/" + mercId + "/orders/" + orderId + "/send_receipt/";

        String url = authResult.baseUrl + paymentsUri + "?access_token=" + authResult.authToken;

        System.out.println("request uri"+ paymentsUri);
        System.out.println("request url"+ url);


        JSONObject payment = new JSONObject();

            try {
                if(requestType.equals("text")) {
                    payment.put("phone", phoneNumber);
                }
                else if(requestType.equals("email")){
                    payment.put("email", email);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        String result = "default";
        System.out.println("request json"+ payment.toString());

        //   result = apiHelper.getInstance().doPostRequest(url, payment.toString());
        try {
            apiHelper.getInstance().doPostRequest(url, payment.toString(), new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    // Something went wrong
                    System.out.println("request fail");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            // do stuff on ui thread
                            Toast.makeText(context, "Receipt Failure", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        System.out.println("request success" + responseStr);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            public void run() {
                                // do stuff on ui thread
                                Toast.makeText(context, "Receipt Sent!", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        // Request not successful
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            public void run() {
                                // do stuff on ui thread
                                Toast.makeText(context, "Receipt Failure", Toast.LENGTH_SHORT).show();

                            }
                        });
                        System.out.println("request unsuccessful");
                    }


                }

            });

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("request try error");

        }
    }

    private class GetOrderAsync extends AsyncTask<String, Void, Order>
    {
        @Override
        protected Order doInBackground(String... params)
        {
            String Id = params[0];
            Order order = null;

            try
            {


                order = orderConnector.getOrder(Id);
                System.out.println("orderid: " + order.getId());
            }
            catch (ServiceException e)
            {
                Log.e("ERROR: ServiceException", e.getMessage());
            }
            catch (RemoteException e)
            {
                Log.e("ERROR: RemoteException", e.getMessage());
            }
            catch (BindingException e)
            {
                Log.e("ERROR: BindingException", e.getMessage());
            }
            catch (ClientException e)
            {
                Log.e("ERROR: ClientException", e.getMessage());
            }

            return order;
        }

        @Override
        public void onPostExecute(Order order)
        {
            System.out.println("ORDER CREATED");

            if(order == null) {
                Toast.makeText(context, "Order Does Not Exist", Toast.LENGTH_LONG).show();
            }
        }
    }





}
