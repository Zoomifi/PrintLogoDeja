package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomIPEDialog extends Dialog
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private Order mOrder;
    private OrderConnector orderConnector;
    private String orderID;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);

    private Context context;
    private int position;
    private int mode;
    private static RelativeLayout pickLayout;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private static Long pickerTime;
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

    EditText edittexts[] = new EditText[11];
    private Spinner mStateSpinner;
    private Spinner mStatusSpinner;
    private Boolean isNew;
    private static String employeeUniqueID;
    private static String mercID;
    private static FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference myShiftRef;
    private static String lastClickedShift;
    private static String lastClickedPosition;
    private static LinearLayoutManager mManager;
    private LinearLayout shiftText;

    public CustomIPEDialog(Context context, Boolean isNew, String employeeUniqueID)
    {
        super(context);
        this.context = context;
        this.isNew = isNew;
        this.employeeUniqueID = employeeUniqueID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_employee);
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        mercID = prefs.getString("mercID", "");
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mHelper = new FirebaseHelper(context);
        mHelper.initialize();
        shiftText = (LinearLayout) findViewById(R.id.shifttext);
        if(isNew){
            shiftText.setVisibility(View.GONE);
        }


        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }

        // 0:name, 1:stagename, 2:street, 3:city, 4:zip, 5:notes, 6:balance, 7:custom
        //spinner1:status, spinner2:state
        for(int i=0; i<11; i++) {
            int resID = context.getResources().getIdentifier("edit"+i, "id", "zomifi.op27no2.printlogo");
            edittexts[i] = ((EditText) findViewById(resID));
        }
        mStateSpinner = (Spinner) findViewById(R.id.state_spinner);
        mStatusSpinner = (Spinner) findViewById(R.id.status_spinner);
        pickLayout = (RelativeLayout) findViewById(R.id.pickers);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        bottomButtons = (LinearLayout) findViewById(R.id.bottombuttons);
        submitButton = (Button) findViewById(R.id.customDialogSubmitButton);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNew) {
                    // create new IPE
                    mHelper.createEmployee(edittexts[0].getText().toString(), edittexts[1].getText().toString(), mStatusSpinner.getSelectedItem().toString(), edittexts[2].getText().toString(), edittexts[3].getText().toString(), mStateSpinner.getSelectedItem().toString(), edittexts[4].getText().toString(), edittexts[5].getText().toString(), edittexts[7].getText().toString(),edittexts[8].getText().toString(),edittexts[9].getText().toString(), edittexts[10].getText().toString(), edittexts[6].getText().toString(), false);
                }
                else{
                    //update IPE
                    mHelper.updateEmployee(employeeUniqueID, edittexts[0].getText().toString(), edittexts[1].getText().toString(), mStatusSpinner.getSelectedItem().toString(), edittexts[2].getText().toString(), edittexts[3].getText().toString(), mStateSpinner.getSelectedItem().toString(), edittexts[4].getText().toString(), edittexts[5].getText().toString(),edittexts[7].getText().toString(),edittexts[8].getText().toString(),edittexts[9].getText().toString(), edittexts[10].getText().toString(),  edittexts[6].getText().toString(), false);
                }

                dismiss();
            }
        });
        doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickLayout.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                pickerTime = calendar.getTimeInMillis();
                if(lastClickedPosition.equals("left")) {
                    mHelper.changeClockIn(lastClickedShift, employeeUniqueID, pickerTime);
                }
                else if(lastClickedPosition.equals("right")){
                    mHelper.changeClockOut(lastClickedShift, employeeUniqueID, pickerTime);
                }
            }
        });
        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickLayout.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

            if(isNew == false) {
                myRef = FirebaseDatabase.getInstance().getReference().child(mercID).child("Employees").child(employeeUniqueID);
                myRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get Post object and use the values to update the UI
                                Employee mEmployee = dataSnapshot.getValue(Employee.class);
                                ArrayList<String> myList = mEmployee.gesFields();
                                for (int i = 0; i < 11; i++) {
                                    edittexts[i].setText(myList.get(i));
                                }
                                selectValue(mStatusSpinner,mEmployee.gesStatus());
                                selectValue(mStateSpinner, mEmployee.gesAddressState());

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                Log.w("error", "loadPost:onCancelled", databaseError.toException());
                                Toast.makeText(context, "Failed to load post.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (6 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        RecyclerView recycler = (RecyclerView) findViewById(R.id.shift_recycler);
        recycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(context);
        mManager.setReverseLayout(true);
        recycler.setLayoutManager(mManager);
        myShiftRef = FirebaseDatabase.getInstance().getReference().child(mercID).child("Employees").child(employeeUniqueID).child("shifts");
        mAdapter = new FirebaseRecyclerAdapter<Shift, ShiftHolder>(Shift.class, zomifi.op27no2.printlogo.R.layout.list_item_shift, ShiftHolder.class, myShiftRef) {
            @Override
            public void populateViewHolder(ShiftHolder shiftViewHolder, Shift shift, int position) {
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                String start = df.format(shift.gesClockIn());
                String end = "in progress";
                if(shift.gesComplete()) {
                    end = df.format(shift.gesClockOut());
                }
                shiftViewHolder.setText1(start);
                shiftViewHolder.setText2(end);

                if(position % 2 == 0){
                    shiftViewHolder.setBackgroundDark();
                }
                else{
                    shiftViewHolder.setBackgroundLight();
                }
            }
        };
        recycler.setAdapter(mAdapter);


    //end oncreate
    }

    public static class ShiftHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        View mView;
        public Button myButton;
        public TextView tv1;
        public TextView tv2;

        public ShiftHolder(View itemView) {
            super(itemView);
            mView = itemView;
         //   myButton = (Button) itemView.findViewById(R.id.button1);
         //   myButton.setOnClickListener(this);
            tv1 = (TextView) itemView.findViewById(R.id.text1);
            tv1.setOnClickListener(this);
            tv2 = (TextView) itemView.findViewById(R.id.text2);
            tv2.setOnClickListener(this);
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
            Shift mShift = (Shift) mAdapter.getItem(position);
            lastClickedShift = mShift.gesUniqueID();

            if(v.getId() == tv1.getId()){
                pickLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                lastClickedPosition = "left";
            }
            else if(v.getId() == tv2.getId()){
                pickLayout.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);
                lastClickedPosition = "right";
            }
            else{
                System.out.println("not button");
            }
        }


    }



    private void selectValue(Spinner spinner, Object value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


}
