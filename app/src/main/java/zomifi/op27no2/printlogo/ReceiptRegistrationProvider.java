package zomifi.op27no2.printlogo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.clover.sdk.util.Platform;
import com.clover.sdk.v1.printer.ReceiptContract;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ReceiptRegistrationProvider extends ContentProvider {
    public static final String AUTHORITY = "com.clover.example.receipteditexample";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final String CONTENT_DIRECTORY_TEXT = "text";
    public static final Uri CONTENT_URI_TEXT = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY_TEXT);

    public static final String CONTENT_DIRECTORY_IMAGE = "image";
    public static final Uri CONTENT_URI_IMAGE = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY_IMAGE);

    private static final int TEXT = 0;
    private static final int IMAGE = 1;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static String addOnText = "THIS IS MY  TEXT";

    private static String merchantID = "default";
    private Bitmap b;
    private static int pixelwidth = 384;
    private static Context mmContext;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor edt;



    final static Target mtarget = new Target(){
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            System.out.println("bitmap presave ");
            saveImage(mmContext, bitmap, "logo", "one");
            System.out.println("bitmap postsave ");
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            System.out.println("bitmap fail ");

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            System.out.println("bitmap prepare ");

        }
    };

    static {
        uriMatcher.addURI(AUTHORITY, CONTENT_DIRECTORY_TEXT, TEXT);
        uriMatcher.addURI(AUTHORITY, CONTENT_DIRECTORY_IMAGE, IMAGE);
    }

    @Override
    public boolean onCreate() {

    //test

        return true;
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

                cursor.addRow(new Object[]{Integer.valueOf(0), addOnText});
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

    public static void passMercID(String mercID, Context context){
        merchantID = mercID;
        System.out.println("ID PASSED: "+merchantID);
        mmContext = context;
        prefs = mmContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = mmContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

        int coeff = prefs.getInt("scalepercent",100);
        if(Platform.isCloverMobile() || Platform.isCloverMini()){
            pixelwidth = 384;
        }
        else if(Platform.isCloverStation()){
            pixelwidth = 576;
        }

        pixelwidth = (int) Math.floor(pixelwidth*.01*coeff);
        System.out.println("ADJUSTED PIXELWIDTH");
        setURL(context);
    }


    public static void setURL(Context context){
        final Context mContext = context;
        System.out.println("url to load: " + "https://zoomifi-logo.s3.amazonaws.com/" + merchantID + "/logo.png");
        System.out.println("pixelwidth: " + pixelwidth);

        Picasso.with(mContext).load("https://zoomifi-logo.s3.amazonaws.com/" + merchantID + "/logo.png")
                        .resize(pixelwidth, 0)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(mtarget);
                /*.into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    *//* Save the bitmap or do something with it here *//*
                        System.out.println("bitmap presave ");
                        saveImage(mContext, bitmap, "logo", "one");
                        System.out.println("bitmap postsave ");
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        System.out.println("bitmap fail ");

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });*/

    }


    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
       // Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.testlogo);
        Bitmap b = getImageBitmap(getContext(), "logo","one");

        OutputStream os = null;
        try {
            File f = File.createTempFile("jeff", ".png", new File("/sdcard"));
            os = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 100, os);
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
        }
        return null;
    }


}