package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;

import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by CristMac on 5/20/16.
 */
public class apiHelper {
    private static apiHelper mInstance = null;
    private Context context;
    private Account account;
    private SharedPreferences.Editor edt;
    private SharedPreferences prefs;
    public OkHttpClient client = new OkHttpClient();
    private MerchantConnector merchantConnector;
    private String  mercID;

    private apiHelper() {

    }

    public static apiHelper getInstance() {
        if (mInstance == null) {
            mInstance = new apiHelper();
        }
        return mInstance;
    }

    public void setContext(Context c) {
        context = c;
    }

    public void setAccount(Account a) {
        // context = c;
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    void doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                       /* runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                            }
                        });*/
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String res = response.body().string();
                        System.out.println("myresponse: " + res);
                        // Do something with the response
                    }
                });
    }

    public Call doPostRequest(String url, String json, Callback callback) throws IOException {

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    public static void infoexecute() {

        Map<String, String> comment = new HashMap<String, String>();
        comment.put("email", "testemail");
        comment.put("cloverID", "KJY6ST0D4YQE6");
        String json = new GsonBuilder().create().toJson(comment, Map.class);
        // makeRequest("https://ops.zoomifi.com/appinstall.php", json);
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

    public void authThis() {


    CloverAuth.AuthResult authResult = null;

    {
        try {
            authResult = CloverAuth.authenticate(context, account);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //   CustomHttpClient httpClient = CustomHttpClient.getHttpClient();
  /*  String paymentsUri = "/v3/merchants/" + mercID+ "/orders/" + mOrderId + "/payments/";
    String url = authResult.baseUrl + paymentsUri + "?access_token=" + authResult.authToken;
    JSONObject payment = new JSONObject();
    payment.put("amount",amount);

    JSONObject tender = new JSONObject();
    tender.put("id",mTender.getId());
    payment.put("tender",tender);

    String result = client.post(url, payment.toString());
        */

    }




}
