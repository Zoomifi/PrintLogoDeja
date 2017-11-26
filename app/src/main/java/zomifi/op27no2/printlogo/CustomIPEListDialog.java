package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clover.sdk.util.CloverAccount;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomIPEListDialog extends Dialog
{
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Account account;
    private Context context;
    private int mPosition;
    private Set<Integer> mSet = new HashSet<Integer>();
    private FirebaseHelper mHelper;

    private Button submitButton;
    private String mercID;
    private FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference myRef;
    private EditText mEdit;
    private String editString = "";

    EditText edittexts[] = new EditText[6];



    public CustomIPEListDialog(Context context, int mPosition)
    {
        super(context);
        this.mPosition = mPosition;
        this.context = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_ipe_list);
        prefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        mercID = prefs.getString("mercID", "");
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mHelper = new FirebaseHelper(context);
        mHelper.initialize();

        mEdit = (EditText) findViewById(R.id.search_bar);
        ImageButton clearButton = (ImageButton) findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setText("");
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

        // Retrieve the Clover account
        if (account == null) {
            account = CloverAccount.getAccount(context);
        }

        for(int i=0; i<2; i++) {
            int resID = context.getResources().getIdentifier("edit"+i, "id", "zomifi.op27no2.printlogo");
            edittexts[i] = ((EditText) findViewById(resID));
        }

        submitButton = (Button) findViewById(R.id.customDialogSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapter !=null) {
                    mAdapter.cleanup();
                }
                RecyclerView recycler = (RecyclerView) findViewById(R.id.employee_recycler);
                recycler.setAdapter(null);
                dismiss();

            }
        });

        findViewById(R.id.customDialogCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapter !=null) {
                    mAdapter.cleanup();
                }
                RecyclerView recycler = (RecyclerView) findViewById(R.id.employee_recycler);
                recycler.setAdapter(null);
                dismiss();

            }
        });

        RecyclerView recycler = (RecyclerView) findViewById(R.id.employee_recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        myRef = FirebaseDatabase.getInstance().getReference().child(mercID).child("Employees");
        mAdapter = new FirebaseRecyclerAdapter<Employee, EmployeeHolder>(Employee.class, zomifi.op27no2.printlogo.R.layout.list_item_employee2, EmployeeHolder.class, myRef) {
            @Override
            public void populateViewHolder(EmployeeHolder employeeViewHolder, Employee employee, int position) {
                employeeViewHolder.setName(employee.gesName());
                employeeViewHolder.setText(employee.gesStageName());
                //employeeViewHolder.hideButton();

                if(position % 2 == 0){
                    employeeViewHolder.setBackgroundDark();
                }
                else{
                    employeeViewHolder.setBackgroundLight();
                }

                //conditions which will hide employee, adds to mSet which is counts offset for alternating gray/white list items
                if(!employee.gesName().toLowerCase().contains(editString.toLowerCase()) ||employee.gesStatus().equals("Inactive")){
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
        recycler.addOnItemTouchListener(
                new RecyclerItemClickListener(context, recycler, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        IPESelector mSelector = new IPESelector(context);
                        Employee employee = (Employee) mAdapter.getItem(position);
                        // mSelector.showEmployee(employee.gesName(), employee.gesUniqueID(), mPosition);
                        edt.putBoolean(employee.gesUniqueID()+"added", true);
                        edt.putBoolean(employee.gesUniqueID()+"active", true);
                        edt.commit();
                        if(prefs.getBoolean("autoClock", true)==true){
                            System.out.println("should be clocked");
                            Long time = Calendar.getInstance().getTimeInMillis();
                            mHelper.clockIn(myRef.child(employee.gesUniqueID()), employee.gesUniqueID(), time);
                        }
                        dismiss();
                        if(mAdapter !=null) {
                            mAdapter.cleanup();
                        }
                        RecyclerView recycler = (RecyclerView) findViewById(R.id.employee_recycler);
                        recycler.setAdapter(null);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );



        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setLayout((5 * width) / 7, (5 * height) / 7);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    public static class EmployeeHolder extends RecyclerView.ViewHolder {
        View mView;

        public EmployeeHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.text1);
            field.setText(name);
        }

        public void setText(String text) {
            TextView field = (TextView) mView.findViewById(R.id.text2);
            field.setText(text);
        }
    /*    public void hideButton() {
            Button myButton = (Button) mView.findViewById(R.id.button1);
            myButton.setVisibility(View.GONE);
        }*/

        public void setBackgroundDark(){
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.lines);
            layout.setBackgroundResource(R.drawable.lightgray_button);
        }
        public void setBackgroundLight(){
            LinearLayout layout = (LinearLayout) mView.findViewById(R.id.lines);
            layout.setBackgroundResource(R.drawable.whitegray_button);
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

}
