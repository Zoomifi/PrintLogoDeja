package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.Query;
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
public class CustomBarOrderListDialog extends Dialog {

    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private Order mOrder;
    private OrderConnector orderConnector;
    private String orderID;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private static Context context;
    private static LinearLayout bottomButtons;
    private String  orderId = "";

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

    private static LinearLayout voidDoorLayout;

    private Button cancelVoidButton;
    private static Button voidDoorButton;
    private int page=1;
    private Boolean readyToScroll = true;


    public CustomBarOrderListDialog(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_barorderlist);
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

        voidDoorLayout = (LinearLayout) findViewById(R.id.void_door_buttons);
        voidDoorButton = (Button) findViewById(R.id.void_door_confirm);
        cancelVoidButton = (Button) findViewById(R.id.void_door_cancel);

        bottomButtons = (LinearLayout) findViewById(R.id.bottombuttons);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapter !=null) {
                    mAdapter.cleanup();
                }
                RecyclerView recycler = (RecyclerView) findViewById(R.id.shift_recycler);
                recycler.setAdapter(null);
                dismiss();
            }
        });
        cancelVoidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapter !=null) {
                    mAdapter.cleanup();
                }
                RecyclerView recycler = (RecyclerView) findViewById(R.id.shift_recycler);
                recycler.setAdapter(null);
                dismiss();
            }
        });


        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        getWindow().setLayout((5 * width) / 7, (6 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        final RecyclerView recycler = (RecyclerView) findViewById(R.id.shift_recycler);
        recycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(context);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        recycler.setLayoutManager(mManager);
        DatabaseReference myOrdersRef = thisRef.child("BarOrders");

        Query query = thisRef.child("BarOrders").limitToLast(page*30);

        mAdapter = new FirebaseRecyclerAdapter<BarOrder, OrderHolder>(BarOrder.class, R.layout.list_item_barorderitem, OrderHolder.class, query) {
            @Override
            public void populateViewHolder(OrderHolder orderViewHolder, BarOrder mBarOrder, int position) {
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                String time = df.format(mBarOrder.gesTimestamp());
                orderViewHolder.setText1(time);
                orderViewHolder.setText0(Integer.toString((30)-position)+": ");
                orderViewHolder.setText2(formatPrice(mBarOrder.gesTotal()));
                if(mBarOrder.gesVoided()){
                    orderViewHolder.setText3("VOID");
                }
                else{
                    orderViewHolder.setText3("");
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
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    System.out.println("reached bottom");
                    page++;

                    Query query = thisRef.child("BarOrders").limitToLast(page*30);

                    mAdapter.cleanup();
                    mAdapter = new FirebaseRecyclerAdapter<BarOrder, OrderHolder>(BarOrder.class, R.layout.list_item_barorderitem, OrderHolder.class, query) {
                        @Override
                        public void populateViewHolder(OrderHolder orderViewHolder, BarOrder mBarOrder, int position) {
                            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                            String time = df.format(mBarOrder.gesTimestamp());
                            orderViewHolder.setText1(time);
                            orderViewHolder.setText0(Integer.toString((page*30)-position)+": ");
                            orderViewHolder.setText2(formatPrice(mBarOrder.gesTotal()));
                            if(mBarOrder.gesVoided()){
                                orderViewHolder.setText3("VOID");
                            }
                            else{
                                orderViewHolder.setText3("");
                            }

                            if(position % 2 == 0){
                                orderViewHolder.setBackgroundDark();
                            }
                            else{
                                orderViewHolder.setBackgroundLight();
                            }
                        }
                    };
                    readyToScroll = true;
                    recycler.setAdapter(mAdapter);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("scroll called");
                                recycler.scrollToPosition(30);
                                readyToScroll = false;
                            }

                    }, 1000);


                }
            }
        });


        //end oncreate
    }

    public static class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CustomManagerListener {
        View mView;
        public TextView tv0;
        public TextView tv1;
        public TextView tv2;
        public TextView tv3;
        public Button bt1;
        BarOrder mBarOrder;
        DatabaseReference myDoorRef;

        public OrderHolder(View itemView) {
            super(itemView);
            mView = itemView;
         //   myButton = (Button) itemView.findViewById(R.id.button1);
         //   myButton.setOnClickListener(this);
            tv0 = (TextView) itemView.findViewById(R.id.textposition);
            tv1 = (TextView) itemView.findViewById(R.id.text1);
            tv2 = (TextView) itemView.findViewById(R.id.text2);
            tv3 = (TextView) itemView.findViewById(R.id.text3);
            bt1 = (Button) itemView.findViewById(R.id.void_door_button);
            bt1.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }
        public void setText0(String name) {
            TextView field = (TextView) mView.findViewById(R.id.textposition);
            field.setText(name);
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

            mBarOrder = (BarOrder) mAdapter.getItem(position);
            myDoorRef = mAdapter.getRef(position);

            if(v.getId() == bt1.getId()){
                CustomManagerDialog managerDialog = new CustomManagerDialog(context, false);
                managerDialog.setCustomManagerListener(this);
                managerDialog.setButtonID(bt1.getId());
                managerDialog.show();
            }
            else{
                BarOrder mBarOrder = (BarOrder) mAdapter.getItem(position);
                CustomBarOrderItemsListDialog customDialog = new CustomBarOrderItemsListDialog(context , mBarOrder.gesUniqueID());
                customDialog.setCancelable(false);
                customDialog.show();
            }

        }

        @Override
        public void managerCallback(int resourceID) {
            showVoid(myDoorRef);
        }
    }

    private static void showVoid(final DatabaseReference doorOrderRef){
        voidDoorLayout.setVisibility(View.VISIBLE);
        bottomButtons.setVisibility(View.GONE);
        voidDoorButton.setOnClickListener(null);
        voidDoorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // void clicked item update order total
                mHelper.voidDoorOrder(doorOrderRef);
                hideVoid();
                System.out.println("will void item");
            }
        });
    }

    private static void hideVoid(){
        voidDoorLayout.setVisibility(View.GONE);
        bottomButtons.setVisibility(View.VISIBLE);
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
