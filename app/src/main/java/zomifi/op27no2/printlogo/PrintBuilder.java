package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.IInterface;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.printer.Category;
import com.clover.sdk.v1.printer.Printer;
import com.clover.sdk.v1.printer.PrinterConnector;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrintBuilder  {

    private static String merchantID = "default";
    private Bitmap b;
    private static int pixelwidth = 384;
    private static Context mmContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor edt;
    public static String message;
    public static Context mContext;

    private static Account account;
    private OrderConnector orderConnector;
    private Order mOrder;
    private static int ourWidth;
    private static int ourWidthLong;
    private float twidth = 250;
    private  int pricewidth;
    private ArrayList<String> printStrings = new ArrayList();
    private ArrayList<String> textList = new ArrayList();
    private Bitmap rows;
    private String myString;
    private String orderId;
    private String mPaymentId;
    private long timetrack;
    private static boolean timeout;
    private static final NumberFormat mCurrencyFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    private static PrinterConnector printerConnector;
    public int mScreen;

    public void initialize(Context context, int screen) {
        mContext = context;
        prefs = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        mScreen = screen;

        if(Platform.isCloverMobile() || Platform.isCloverMini()){
            pixelwidth = 384;
            ourWidth = 370;

        }
        else if(Platform.isCloverStation()){
            pixelwidth = 576;
            ourWidth = 550;
        }
        System.out.println("our width1 " + ourWidth);
        Paint paint = new Paint();
        paint.setTextSize(30);
        pricewidth = (int) Math.floor(paint.measureText(" $10,000"));
        ourWidth =  ourWidth - pricewidth;


        if (account == null) {
            account = CloverAccount.getAccount(mContext);

            if (account == null) {
                Toast.makeText(mContext, "failed to retrieve clover account", Toast.LENGTH_SHORT).show();

                return;
            }
        }

    }


    public Bitmap getImageBitmap(Context context,String name,String extension){
        name=name+"."+extension;
        try{
            System.out.println("get ");
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
            System.out.println("get error");
        }
        return null;
    }

    public void PrintItems(final Map<String, OrderItem> items){
        printerConnector  = new PrinterConnector(mContext, account, new ServiceConnector.OnServiceConnectedListener()
        {
            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> connector)
            {
                PrintReceipt(items);
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
            }
        });
        printerConnector.connect();
    }

    public void PrintReceipt(Map<String, OrderItem> items) {

        ArrayList<Bitmap> myRows = new ArrayList<Bitmap>();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get("item" + i);
            String name = item.gesName();
            if (name.length() > 30) {
                name = name.substring(0, 30);
            }
            String price = formatPrice(Long.toString(item.gesPrice()),1);
        //    Bitmap row = mergeNameAndPrice(textAsBitmap(name, 12, Color.BLACK, false), textAsBitmap(price, 12, Color.BLACK, false));
        //    myRows.add(row);
        }
    }
    public void PrintLineItemsReceipt(ArrayList<ArrayList<Integer>> lineItems, Long total){

        Bitmap holdBitmap;
        Bitmap rowspace = textAsBitmap("", 30, Color.BLACK, true);
        Bitmap row0 = textAsBitmap("Line Items:", 30, Color.BLACK,true);
        holdBitmap = mergeBitmapV(rowspace,row0);
        row0 = holdBitmap;


        for(int i=0;i<lineItems.size();i++){
            int mult2 = lineItems.get(i).get(2);
            int tpage = lineItems.get(i).get(0);
            int tbutton = lineItems.get(i).get(1);

            String id = prefs.getString(mScreen+"_"+tpage+"button"+tbutton+"_id", "");
            System.out.println("line item id:"+id);
            String getitem = Integer.toString(mult2)+" "+prefs.getString(id+"_name","");
            System.out.println("line item name:"+getitem);

            Bitmap pricepiece = priceTextAsBitmap(formatPrice(prefs.getString(id + "_price"+mScreen, ""), mult2), 26, Color.BLACK, false);
            System.out.println("line item price:"+prefs.getString(id+"_price"+mScreen," "));

            Bitmap nextrow = textAsBitmap(getitem, 26, Color.BLACK, false);
            Bitmap together = mergeBitmapH(nextrow, pricepiece);
            holdBitmap = mergeBitmapV(row0,together);
            row0 = holdBitmap;


            if(prefs.getBoolean(id+"_isvcash",false) == true){
                String getitem2 = Integer.toString(mult2)+" "+prefs.getString(id+"_name","")+" FEE";
                Long fee = Long.parseLong(prefs.getString(id + "_price" + mScreen, ""))/10;
                Bitmap pricepiece2 = priceTextAsBitmap(formatPrice(Long.toString(fee), mult2), 26, Color.BLACK, false);
                Bitmap nextrow2 = textAsBitmap(getitem2, 26, Color.BLACK, false);
                Bitmap together2 = mergeBitmapH(nextrow2, pricepiece2);
                holdBitmap = mergeBitmapV(row0,together2);
                row0 = holdBitmap;
            }

        }

        if(prefs.getBoolean("customPresent",false) == true){
            Bitmap nextrow = textAsBitmap("Custom", 26, Color.BLACK, false);
            Bitmap pricepiece = priceTextAsBitmap(formatPrice(prefs.getString("customPrice", "0"),1), 26, Color.BLACK, false);
            Bitmap together = mergeBitmapH(nextrow, pricepiece);
            holdBitmap = mergeBitmapV(row0,together);
            row0 = holdBitmap;
        }

        //space
        Bitmap nextrow = textAsBitmap("", 26, Color.BLACK, false);
        holdBitmap = mergeBitmapV(row0,nextrow);
        row0 = holdBitmap;

        // total
        String left5 = "Total";
        Bitmap pricepiece5 = priceTextAsBitmap(formatPrice(Long.toString(total), 1), 26, Color.BLACK, true);
        Bitmap nextrow5 = textAsBitmap(left5, 26, Color.BLACK, true);
        Bitmap together5 = mergeBitmapH(nextrow5, pricepiece5);
        holdBitmap = mergeBitmapV(row0,together5);
        row0 = holdBitmap;

        saveImage(mContext, row0, "logo", "one");
    }

    public void PrintChangeReceipt(Long total){

        Bitmap holdBitmap;
        Bitmap row0 = textAsBitmap("", 30, Color.BLACK,true);

        Bitmap pricepiece = priceTextAsBitmap(formatPrice(Long.toString(total), 1), 26, Color.BLACK, false);
        Bitmap nextrow = textAsBitmap("Change Issued:", 30, Color.BLACK,true);
        Bitmap together = mergeBitmapH(nextrow, pricepiece);
        holdBitmap = mergeBitmapV(row0,together);
        row0 = holdBitmap;


        saveImage(mContext, row0, "logo", "one");
    }

        public void PrintOrderItemsReceipt(ArrayList<OrderItem> mItems, Long balance, Long total, Boolean voided){

        Bitmap holdBitmap;
        Bitmap row0 = textAsBitmap("", 30, Color.BLACK, true);

        if(voided) {
            Bitmap rowspace = textAsBitmap("ORDER VOID", 36, Color.BLACK, true);
            holdBitmap = mergeBitmapV(row0, rowspace);
            row0 = holdBitmap;
        }

        Bitmap rowspace3 = textAsBitmap("Line Items:", 30, Color.BLACK,true);
        holdBitmap = mergeBitmapV(row0,rowspace3);
        row0 = holdBitmap;


        for(int i=0;i<mItems.size();i++){

            String getitem = mItems.get(i).gesName();
            if(mItems.get(i).gesVoided()){
                getitem = getitem+"(VOID)";
            }
            System.out.println("line item name:"+getitem);

            Bitmap pricepiece = priceTextAsBitmap(formatPrice(Long.toString(mItems.get(i).gesPrice()), 1), 26, Color.BLACK, false);

            Bitmap nextrow = textAsBitmap(getitem, 26, Color.BLACK, false);
            Bitmap together = mergeBitmapH(nextrow, pricepiece);
            holdBitmap = mergeBitmapV(row0,together);
            row0 = holdBitmap;
        }


        //space
        Bitmap nextrow = textAsBitmap("", 26, Color.BLACK, false);
        holdBitmap = mergeBitmapV(row0,nextrow);
        row0 = holdBitmap;

        // total
        String left5 = "Total";
        Bitmap pricepiece5 = priceTextAsBitmap(formatPrice(Long.toString(total), 1), 26, Color.BLACK, true);
        Bitmap nextrow5 = textAsBitmap(left5, 26, Color.BLACK, true);
        Bitmap together5 = mergeBitmapH(nextrow5, pricepiece5);
        holdBitmap = mergeBitmapV(row0,together5);
        row0 = holdBitmap;

        //space
        Bitmap nextrow2 = textAsBitmap("", 26, Color.BLACK, false);
        holdBitmap = mergeBitmapV(row0,nextrow2);
        row0 = holdBitmap;

        // total
        String left6 = "Balance";
        Bitmap pricepiece6 = priceTextAsBitmap(formatPrice(Long.toString(balance), 1), 26, Color.BLACK, true);
        Bitmap nextrow6 = textAsBitmap(left6, 26, Color.BLACK, true);
        Bitmap together6 = mergeBitmapH(nextrow6, pricepiece6);
        holdBitmap = mergeBitmapV(row0,together6);
        row0 = holdBitmap;

            saveImage(mContext, row0, "logo", "one");
    }




    // Printing Stuff
    public Bitmap textAsBitmap(String text, float textSize, int textColor, Boolean bold) {
        printStrings.clear();
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        int j= 0;
        int k=0;
        int l=0;
        Boolean set1 = false;
        Boolean set2 = false;
        Boolean set3 = false;
        Boolean set4 = false;


        for(int i=0;i<text.length();i++){
            if(set1==false) {
                twidth = paint.measureText(text.substring(0, i));
            }
            else if(set2== false && set1 == true){
                twidth = paint.measureText(text.substring(j, i));
            }
            else if(set3 == false && set2== true && set1 == true) {
                twidth = paint.measureText(text.substring(k, i));
            }
            else if(set4 == false && set3== true && set2 == true && set1 == true) {
                twidth = paint.measureText(text.substring(l, i));
            }

            if(twidth > ourWidth && set1 == false){
                //check previous 10 for space and break there instead
                for(int i2=0;i2<10;i2++){
                    if(set1==false) {
                        if (text.substring(i - i2, (i - i2 + 1)).equals(" ")) {
                            j = (i-i2);
                            printStrings.add(0, text.substring(0, (i-i2)));
                            set1 = true;
                            twidth = paint.measureText(text.substring(j, i));
                        }
                    }
                }
                //if we didn't set space after finding break, proceed to set line here
                if(set1==false) {
                    printStrings.add(0, text.substring(0, i));
                    System.out.println("LENGTH EXCEEDED1: " + i);
                    j = i;
                    set1 = true;
                    twidth = paint.measureText(text.substring(j, i));
                }
            }


            if(twidth > (ourWidth) && set2 == false){
                //check previous 10 for space and break there instead
                for(int i2=0;i2<10;i2++){
                    if(set2==false) {
                        if (text.substring(i - i2, (i - i2 + 1)).equals(" ")) {
                            k = (i-i2);
                            printStrings.add(1, text.substring(j, (i-i2)));
                            set2 = true;
                            twidth = paint.measureText(text.substring(k, i));
                        }
                    }
                }
                //if we didn't set space after finding break, proceed to set line here
                if(set2==false) {
                    printStrings.add(1, text.substring(j, i));
                    System.out.println("LENGTH EXCEEDED2: " + i);
                    k = i;
                    set2 = true;
                    twidth = paint.measureText(text.substring(k, i));
                }

            }
            if(twidth > (ourWidth) && set3 == false){
                //check previous 10 for space and break there instead
                for(int i2=0;i2<10;i2++){
                    if(set3==false) {
                        if (text.substring(i - i2, (i - i2 + 1)).equals(" ")) {
                            l = (i-i2);
                            printStrings.add(2, text.substring(k, (i-i2)));
                            set3 = true;
                            twidth = paint.measureText(text.substring(l, i));
                        }
                    }
                }
                //if we didn't set space after finding break, proceed to set line here
                if(set3==false) {
                    printStrings.add(2, text.substring(k, i));
                    System.out.println("LENGTH EXCEEDED2: " + i);
                    l = i;
                    set3 = true;
                    twidth = paint.measureText(text.substring(l, i));
                }

            }
            if(twidth > (ourWidth) && set4 == false){

                printStrings.add(3, text.substring(l,i));
                System.out.println("LENGTH EXCEEDED3: "+i);
                set4 = true;
                twidth = paint.measureText(text.substring(l, i));
            }
        }

        // if the length wasn't exceeded, we need to add the partial string
        if(set1==false){
            printStrings.add(0,text);
        }
        if(set2== false && set1 == true){
            printStrings.add(1,text.substring(j,text.length()));
        }
        if(set3==false && set1==true && set2==true){
            printStrings.add(2,text.substring(k,text.length()));
        }
        if(set4 == false && set3==true && set1==true && set2==true){
            printStrings.add(3,text.substring(l,text.length()));
        }


        paint.setColor(textColor);
        if(bold){
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        else{
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        // int width = (int) (paint.measureText(printStrings.get(0)) + 0.5f); // round
        int width = pixelwidth;
        int height = (int) (baseline + paint.descent() + 0.5f)*printStrings.size();
        System.out.println("printsize:"+printStrings.size()+": "+printStrings.get(0));
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(printStrings.get(0), 0, baseline, paint);
        if(printStrings.size() > 1) {
            String holdstring = printStrings.get(1);
            if(holdstring.substring(0,1).equals(" ")){
                printStrings.set(1, holdstring.substring(1,holdstring.length()));
            }
            canvas.drawText(printStrings.get(1), 0, baseline + 30, paint) ;
        }
        if(printStrings.size() > 2) {
            String holdstring2 = printStrings.get(2);
            if(holdstring2.substring(0,1).equals(" ")){
                printStrings.set(2, holdstring2.substring(1,holdstring2.length()));
            }
            canvas.drawText(printStrings.get(2), 0, baseline + 60, paint);
        }
        if(printStrings.size() > 3) {
            String holdstring3 = printStrings.get(3);
            if(holdstring3.substring(0,1).equals(" ")){
                printStrings.set(3, holdstring3.substring(1,holdstring3.length()));
            }
            canvas.drawText(printStrings.get(3), 0, baseline + 90, paint);
        }
        return image;
    }

    public Bitmap priceTextAsBitmap(String text, float textSize, int textColor, Boolean bold) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        if(bold){
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        else{
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        // int width = (int) (paint.measureText(printStrings.get(0)) + 0.5f); // round
        int width =  (int) Math.ceil(paint.measureText(text));
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);

        return image;


    }

    public Bitmap mergeBitmapV(Bitmap fr, Bitmap sc)
    {

        Bitmap comboBitmap;

        int width, height;

        width = fr.getWidth();
        height = fr.getHeight() + sc.getHeight();;

        comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(comboBitmap);


        comboImage.drawBitmap(fr, 0f, 0f, null);
        comboImage.drawBitmap(sc, 0f, fr.getHeight(), null);
        return comboBitmap;

    }

    public Bitmap mergeBitmapH(Bitmap fr, Bitmap sc)
    {
        Paint paint = new Paint();
        paint.setTextSize(30);

        Bitmap comboBitmap;

        int width, height;

        width = pixelwidth;
        height = fr.getHeight();

        comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(comboBitmap);


        comboImage.drawBitmap(fr, 0f, 0f, null);
        comboImage.drawBitmap(sc, width - pricewidth + (pricewidth - sc.getWidth()), 0f, null);
        return comboBitmap;

    }

    /*public Bitmap constructBitmap(){


        Bitmap holdBitmap;
        Bitmap rowspace = textAsBitmap("", 30, Color.BLACK, true);
        Bitmap row0 = textAsBitmap("Line Items:", 30, Color.BLACK,true);
        holdBitmap = mergeBitmapV(rowspace,row0);
        row0 = holdBitmap;


        for(int i=0;i<lineItems.size();i++){
            int mult2 = lineItems.get(i).get(2);
            int tpage = lineItems.get(i).get(0);
            int tbutton = lineItems.get(i).get(1);
            String getitem = Integer.toString(mult2)+" "+sharedPreferences.getString(tpage+"button"+(tbutton)+"_name","");
            Bitmap pricepiece = priceTextAsBitmap(formatPrice(sharedPreferences.getString(tpage + "button" + (tbutton) + "_price", "")), 26, Color.BLACK, false);
            Bitmap nextrow = textAsBitmap(getitem, 26, Color.BLACK, false);
            Bitmap together = mergeBitmapH(nextrow, pricepiece);
            holdBitmap = mergeBitmapV(row0,together);
            row0 = holdBitmap;
        }

        //space
        Bitmap nextrow = textAsBitmap("", 26, Color.BLACK, false);
        holdBitmap = mergeBitmapV(row0,nextrow);
        row0 = holdBitmap;

        //subtotal
        String left2 = "Subtotal";
        Bitmap pricepiece2 = priceTextAsBitmap(formatPrice(Long.toString(subtotal)), 26, Color.BLACK, true);
        Bitmap nextrow2 = textAsBitmap(left2, 26, Color.BLACK, true);
        Bitmap together2 = mergeBitmapH(nextrow2, pricepiece2);
        holdBitmap = mergeBitmapV(row0,together2);
        row0 = holdBitmap;

        //fee total
        String left3 = "Fee";
        Bitmap pricepiece3 = priceTextAsBitmap(formatPrice(Long.toString(fee)), 26, Color.BLACK, true);
        Bitmap nextrow3 = textAsBitmap(left3, 26, Color.BLACK, true);
        Bitmap together = mergeBitmapH(nextrow3, pricepiece3);
        holdBitmap = mergeBitmapV(row0,together);
        row0 = holdBitmap;

        // space
        Bitmap nextrow4 = textAsBitmap("", 26, Color.BLACK, false);
        holdBitmap = mergeBitmapV(row0,nextrow4);
        row0 = holdBitmap;

        // total
        String left5 = "Total";
        Bitmap pricepiece5 = priceTextAsBitmap(formatPrice(Long.toString(total)), 26, Color.BLACK, true);
        Bitmap nextrow5 = textAsBitmap(left5, 26, Color.BLACK, true);
        Bitmap together5 = mergeBitmapH(nextrow5, pricepiece5);
        holdBitmap = mergeBitmapV(row0,together5);
        row0 = holdBitmap;

        return row0;
    }*/

    public static void saveImage(Context context, Bitmap b,String name,String extension){
        name=name+"."+extension;
        FileOutputStream out;
        try {
            System.out.println("save ");
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("save error");
        }
    }




    @NonNull
    private String formatPrice(String PRICE_STRING, int multiple) {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String currency = sharedPreferences.getString("currencyCode", "USD");
        mCurrencyFormat.setCurrency(Currency.getInstance(currency));
        String price = "";
        if(!PRICE_STRING.equals("")) {
            long value = multiple* Long.valueOf(PRICE_STRING);
            price = mCurrencyFormat.format(value / 100.0);
        }
        return price;

    }

    private Printer getPrinter() {

            try {
                List<Printer> printers = printerConnector.getPrinters(Category.RECEIPT);
                if (printers != null && !printers.isEmpty()) {
                    return printers.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

    }



}




