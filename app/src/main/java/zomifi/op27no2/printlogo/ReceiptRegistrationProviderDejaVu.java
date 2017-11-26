package zomifi.op27no2.printlogo;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.printer.ReceiptContract;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ReceiptRegistrationProviderDejaVu extends ContentProvider {
    public static final String AUTHORITY = "com.clover.example2.receipteditexampleDejaVu";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final String CONTENT_DIRECTORY_TEXT = "text";
    public static final Uri CONTENT_URI_TEXT = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY_TEXT);

    public static final String CONTENT_DIRECTORY_IMAGE = "image";
    public static final Uri CONTENT_URI_IMAGE = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY_IMAGE);

    private static final int TEXT = 0;
    private static final int IMAGE = 1;
    public static final String PARAM_ORDER_ID = "order_id";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static String addOnText = "THIS IS MY  TEXT";

    private static String merchantID = "default";
    private Bitmap b;
    private static int pixelwidth = 384;
    private static Context mmContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor edt;
    public static String message;
    public static Context mContext;

    private Account account;
    private OrderConnector orderConnector;
    private Order mOrder;
    private static int ourWidth;
    private static int ourWidthLong;
    private float twidth = 250;
    private ArrayList<String> printStrings = new ArrayList();
    private ArrayList<String> textList = new ArrayList();
    private Bitmap rows;
    private String myString;
    private String orderId;
    private long timetrack;
    private static boolean timeout;
    private static ArrayList<ArrayList<Integer>> lineItems = new ArrayList<ArrayList<Integer>>();


    static {
        uriMatcher.addURI(AUTHORITY, CONTENT_DIRECTORY_TEXT, TEXT);
        uriMatcher.addURI(AUTHORITY, CONTENT_DIRECTORY_IMAGE, IMAGE);
    }

    @Override
    public boolean onCreate() {
        timetrack = System.currentTimeMillis();

        if(Platform.isCloverMobile() || Platform.isCloverMini()){
            pixelwidth = 384;
            ourWidth = 370;
            ourWidthLong = 250;
        }
        else if(Platform.isCloverStation()){
            pixelwidth = 576;
            ourWidth = 550;
            ourWidthLong = 375;
        }


        return true;
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

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        switch (uriMatcher.match(uri)) {

            case TEXT:

               MatrixCursor cursor = new MatrixCursor(new String[]{ReceiptContract.Text._ID, ReceiptContract.Text.TEXT});

                return cursor;

            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TEXT:
                return ReceiptContract.Text.CONTENT_TYPE;
            case IMAGE:
                return ReceiptContract.Image.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }



    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        mContext = MyApplication.getAppContext();
        //prefs = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        prefs = mContext.getSharedPreferences(mContext.getString(zomifi.op27no2.printlogo.R.string.donation_here_file), Context.MODE_PRIVATE);

        System.out.println("canprint: " + prefs.getBoolean("can_print", true) );

        if(prefs.getBoolean("can_print", true) == true) {

            //print line items created in MainActivity2
            Bitmap b = getImageBitmap(getContext(), "logo", "one");

            System.out.println("timestamp3: " + (System.currentTimeMillis() - timetrack));

            OutputStream os = null;
            try {
                File f = File.createTempFile("jeff", ".png", new File("/sdcard"));
                os = new FileOutputStream(f);
                b.compress(Bitmap.CompressFormat.PNG, 100, os);
                System.out.println("timestamp4: " + (System.currentTimeMillis() - timetrack));
                return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                }
                System.out.println("timestamp5: " + (System.currentTimeMillis() - timetrack));
                timeout = false;
            }
        }
        else{
            // do not print bitmap should be turned off
        }

        return null;
    }


    public static void setText(String text){
        message = text;
    }
    public static void setContext(Context context) {
        mContext = context;

    }




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

        Bitmap comboBitmap;

        int width, height;

        width = pixelwidth;
        height = fr.getHeight();

        comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(comboBitmap);


        comboImage.drawBitmap(fr, 0f, 0f, null);
        comboImage.drawBitmap(sc, fr.getWidth(), 0f, null);
        return comboBitmap;

    }
    public Bitmap constructBitmap(ArrayList<String> textItems){


        Bitmap holdBitmap;
        Bitmap rowspace = textAsBitmap("", 30, Color.BLACK,true);
        Bitmap row0 = textAsBitmap("Customer Info", 30, Color.BLACK,true);
        holdBitmap = mergeBitmapV(rowspace,row0);
        row0 = holdBitmap;


       /* for(int i=0;i<textItems.size();i++){
            Bitmap nextrow = textAsBitmap(textItems.get(i), 26, Color.BLACK, false);
            holdBitmap = mergeBitmapV(row0,nextrow);
            row0 = holdBitmap;
        }*/

        return row0;
    }

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

}




