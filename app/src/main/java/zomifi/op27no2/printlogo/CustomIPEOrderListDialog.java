package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomIPEOrderListDialog extends Dialog
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private Order mOrder;
    private OrderConnector orderConnector;
    private String orderID;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private static Context context;
    private static LinearLayout bottomButtons;
    private String orderId = "";

    private TextView wordText;

    private static Button submitButton;
    private static Button doneButton;
    private static Button backButton;
    private static Button cancelButton;

    private Boolean isEditing;

    private static FirebaseHelper mHelper;
    private DatabaseReference myRef;
    private ValueEventListener myEmployeeListener;

    EditText edittexts[] = new EditText[8];
    private Spinner mStateSpinner;
    private Spinner mStatusSpinner;
    private Boolean isNew;
    private static String employeeUniqueID;
    private static FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference myItemsRef;
    private static String lastClickedShift;
    private static String lastClickedPosition;
    private String mercID;
    private static LinearLayoutManager mManager;
    private DatabaseReference thisRef;

    private RecyclerView recycler;

    public CustomIPEOrderListDialog(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_ipeorderlist);
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mercID = prefs.getString("mercID", "");
        thisRef = database.getReference().child(mercID);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mHelper = new FirebaseHelper(context);
        mHelper.initialize();

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }

        bottomButtons = (LinearLayout) findViewById(R.id.bottombuttons);
        submitButton = (Button) findViewById(R.id.customDialogSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        getWindow().setLayout((6 * width) / 7, (6 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        recycler = (RecyclerView) findViewById(R.id.shift_recycler);
        recycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(context);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        recycler.setLayoutManager(mManager);
        DatabaseReference myOrdersRef = thisRef.child("IPEOrders");
        mAdapter = new FirebaseRecyclerAdapter<IPEOrder, OrderHolder>(IPEOrder.class, R.layout.list_item_ipeorder, OrderHolder.class, myOrdersRef) {
            @Override
            public void populateViewHolder(OrderHolder orderViewHolder, IPEOrder mIPEOrder, int position) {
                SimpleDateFormat df = new SimpleDateFormat("MM/dd       hh:mm:ss a");
                String time = df.format(mIPEOrder.gesTimestamp());
                orderViewHolder.setText1(time);
                orderViewHolder.setText2(formatPrice(mIPEOrder.gesTotal()));
                orderViewHolder.setText3(mIPEOrder.gesEmployeeName());
                if(mIPEOrder.gesOpen()) {
                    orderViewHolder.setText4("Open");
                }
                else{
                    orderViewHolder.setText4("Closed");

                }
                if(mIPEOrder.gesVoided()) {
                    orderViewHolder.setText5("Void");
                }
                else{
                    orderViewHolder.setText5("");
                }

                if(position % 2 == 0){
                    orderViewHolder.setBackgroundDark();
                }
                else{
                    orderViewHolder.setBackgroundLight();
                }
            }
        };
        recycler.setAdapter(mAdapter);

        //end oncreate
    }





    public static class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View mView;
        public TextView tv1;
        public TextView tv2;
        public TextView tv3;
        public TextView tv4;
        public TextView tv5;

        public OrderHolder(View itemView) {
            super(itemView);
            mView = itemView;
         //   myButton = (Button) itemView.findViewById(R.id.button1);
         //   myButton.setOnClickListener(this);
            tv1 = (TextView) itemView.findViewById(R.id.text1);
            tv2 = (TextView) itemView.findViewById(R.id.text2);
            tv3 = (TextView) itemView.findViewById(R.id.text3);
            tv4 = (TextView) itemView.findViewById(R.id.text4);
            tv4 = (TextView) itemView.findViewById(R.id.text5);
            itemView.setOnClickListener(this);
        }

        public void setText1(String name) {
            TextView field = (TextView) mView.findViewById(R.id.text1);
            field.setText(name);
        }

        public void setText2(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text2);
            field.setText(text);
        }
        public void setText3(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text3);
            field.setText(text);
        }
        public void setText4(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text4);
            field.setText(text);
        }
        public void setText5(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text5);
            field.setText(text);
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
            IPEOrder mIPEOrder = (IPEOrder) mAdapter.getItem(position);

/*            if(v.getId() == tv1.getId()){

            }*/
            CustomIPEOrderItemsListDialog customDialog = new CustomIPEOrderItemsListDialog(context, mIPEOrder.gesEmployeeID(),mIPEOrder.gesUniqueID());
            customDialog.setCancelable(false);
            customDialog.show();

        }
    }


    @NonNull
    private String formatPrice(Long value) {
        //test format code
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        //default USD from prefs
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));
        String price = mCurrencyFormat.format(value / 100.0);
        return price;

    }



}
