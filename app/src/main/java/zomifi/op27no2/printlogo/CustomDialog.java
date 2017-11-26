package zomifi.op27no2.printlogo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Created by Andrew on 3/4/2016.
 * This code is written for Scrap Workouts LLC.
 */
public class CustomDialog extends Dialog
{
    private String  headerText;
    private String  bodyText;
    private Context context;
    private String  orderId = "";

    public static final int MAIN_ACTIVITY_INSTRUCTIONS          = 1001;
    public static final int CUSTOMER_FACING_SIDE_INVALID_INPUT  = 1002;
    public static final int RECEIPT_ACTIVITY                    = 1003;

    private int dialogFrom = 1001;

    public CustomDialog(Context context)
    {
        super(context);

        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(zomifi.op27no2.printlogo.R.layout.dialog_custom);

        ((TextView)findViewById(zomifi.op27no2.printlogo.R.id.customDialogHeader)).setText(this.headerText);
        ((TextView)findViewById(zomifi.op27no2.printlogo.R.id.customeDialogBody )).setText(this.bodyText);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        switch(this.dialogFrom)
        {
            case MAIN_ACTIVITY_INSTRUCTIONS:

                findViewById(zomifi.op27no2.printlogo.R.id.customDialogCancelButton).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dismiss();
                    }
                });

                findViewById(zomifi.op27no2.printlogo.R.id.customDialogSubmitButton).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dismiss();

                        Intent intent = new Intent(context, SetupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });

                DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                getWindow().setLayout((5 * width) / 7, (5 * height) / 7);

                findViewById(zomifi.op27no2.printlogo.R.id.instructionsImageView).setVisibility(View.VISIBLE);

                instantiateCheckBoxDoNotShowAgain();

                break;
            case CUSTOMER_FACING_SIDE_INVALID_INPUT:


                Button button = (Button) findViewById(zomifi.op27no2.printlogo.R.id.customDialogCancelButton);
                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dismiss();
                    }
                });
                button.setText("OK");

                findViewById(zomifi.op27no2.printlogo.R.id.customDialogSubmitButton).setVisibility(View.GONE);

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                break;
            case RECEIPT_ACTIVITY:

                initializeReceiptDialog();

                break;
        }
    }

    public void initializeReceiptDialog()
    {
        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        getWindow().setLayout((4 * width) / 7, (4 * height) / 7);


        Button button = (Button) findViewById(zomifi.op27no2.printlogo.R.id.customDialogCancelButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        button.setText("Decline");

        button = (Button) findViewById(zomifi.op27no2.printlogo.R.id.customDialogSubmitButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

        //        ((SetupActivity) context).launchReceipts(orderId);



            }
        });
        button.setText("Print or Email Receipt");

        TextView body = (TextView)findViewById(zomifi.op27no2.printlogo.R.id.customeDialogBody);
        body.setTextSize(30);
        body.setGravity(Gravity.CENTER);

    }

    public void setHeaderText(String headerText)
    {
        this.headerText = headerText;
    }

    public void setBodyText(String bodyText)
    {
        this.bodyText = bodyText;
    }

    public void setDialogFrom(int dialogFrom)
    {
        this.dialogFrom = dialogFrom;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public void instantiateCheckBoxDoNotShowAgain()
    {
        CheckBox checkBox = (CheckBox)findViewById(zomifi.op27no2.printlogo.R.id.doNotShowMessageAgainCheckBox);
        checkBox.setVisibility(View.VISIBLE);

        final SharedPreferences prefs = this.context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(context.getString(zomifi.op27no2.printlogo.R.string.doNotShowDialogAgain), !isChecked);

                editor.commit();
            }
        });
    }




}
