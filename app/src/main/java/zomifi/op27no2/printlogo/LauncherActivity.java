package zomifi.op27no2.printlogo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Boolean isTerminal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();



        if(prefs.getString("launchActivity", "navigation").equals("navigation")){
            System.out.println("launch nav");
            Intent myIntent = new Intent(LauncherActivity.this, NavigationActivity.class);
            LauncherActivity.this.startActivity(myIntent);
            finish();
        }
        else if(prefs.getString("launchActivity", "navigation").equals("clockin")){
              Intent myIntent = new Intent(LauncherActivity.this, ItemsActivity.class);
              LauncherActivity.this.startActivity(myIntent);
              finish();
        }
        else if(prefs.getString("launchActivity", "navigation").equals("door")){
              Intent myIntent = new Intent(LauncherActivity.this, DoorActivity.class);
              LauncherActivity.this.startActivity(myIntent);
              finish();
        }
        else if(prefs.getString("launchActivity", "navigation").equals("orders")){
              Intent myIntent = new Intent(LauncherActivity.this, ItemsActivity.class);
              LauncherActivity.this.startActivity(myIntent);
              finish();
        }
        else if(prefs.getString("launchActivity", "navigation").equals("bar")){
              Intent myIntent = new Intent(LauncherActivity.this, ItemsActivity.class);
              LauncherActivity.this.startActivity(myIntent);
              finish();
        }
        else{
            System.out.println("launch default nav");
            Intent myIntent = new Intent(LauncherActivity.this, NavigationActivity.class);
            LauncherActivity.this.startActivity(myIntent);
            finish();
        }


    }



}
