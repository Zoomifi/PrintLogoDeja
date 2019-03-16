package zomifi.op27no2.printlogo;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

/**
 * Created by CristMac on 3/10/17.
 */
public interface onGetEmployeesClocked {
        public void onStart();
        public void onSuccess(ArrayList<String> data);
        public void onFailed(DatabaseError databaseError);

}
