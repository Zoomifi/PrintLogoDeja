package zomifi.op27no2.printlogo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CristMac on 7/9/16.
 */
public class FirebaseHelper {
    private FirebaseDatabase database;
    private DatabaseReference thisRef;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private Context mContext;
    private String mercID;
    private Handler handler = new Handler();


    public FirebaseHelper(Context context){
        this.mContext = context;
    }

    public void initialize(){
        prefs = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        database = FirebaseDatabase.getInstance();
        mercID = prefs.getString("mercID","");
        thisRef = database.getReference().child(mercID);
    }

    public void createEmployee(String name, String stagename, String status, String street, String city, String state, String zip, String notes,String phone1,String phone2,String ssn, String balance, String custom, Boolean clocked){
        DatabaseReference newRef =  thisRef.child("Employees").push();
        String key = newRef.getKey();
        Employee mEmployee = new Employee(name, stagename, key, status, street, city, state, zip, notes, phone1, phone2, ssn, balance, custom, clocked, null, null);
        newRef.setValue(mEmployee);
    }

    public void updateEmployee(String employeeUniqueID, String name, String stagename, String status, String street, String city, String state, String zip, String notes,String phone1,String phone2,String ssn, String balance, String custom, Boolean clocked){
        DatabaseReference ipeRef =  thisRef.child("Employees").child(employeeUniqueID);
        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", name);
        updates.put("stagename", stagename);
        updates.put("status", status);
        updates.put("addressStreet", street);
        updates.put("addressCity", city);
        updates.put("addressState", state);
        updates.put("addressZip", zip);
        updates.put("notes", notes);
        updates.put("phone1", phone1);
        updates.put("phone2", phone2);
        updates.put("ssn", ssn);
        updates.put("custom", custom);
        updates.put("balance", balance);

        ipeRef.updateChildren(updates);

    }

    public void clockIn(final DatabaseReference myRef, final String employeeID, final Long time) {
        myRef.child("clocked").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("clock in save failure");
                } else {
                    System.out.println("clockin success");
                    DatabaseReference newRef = myRef.child("shifts").push();
                    String key = newRef.getKey();
                    Shift mShift = new Shift(key, employeeID, time, null, false);
                    newRef.setValue(mShift, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                logError("clock in time save failure");
                            } else {
                                System.out.println("shift time save success");
                            }
                        }

                    });
                }
            }
        });
        myRef.child("lastTime").setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("time save failure");
                } else {
                    System.out.println("lasttime creation success");
                }
            }
        });

    }

    public void changeClockIn(final String shiftID, final String employeeID, final Long time) {
        DatabaseReference shiftRef = thisRef.child("Employees").child(employeeID).child("shifts").child(shiftID).child("clockin");
        shiftRef.setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("clock in save failure");

                } else {
                    System.out.println("clockin change success");
                }
            }
        });

    }

    public void clockOut(final DatabaseReference myRef, final String employeeID, final Long time){
        myRef.child("clocked").setValue(false, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("clock in save failure");
                } else {
                    System.out.println("clockout success");
                    setLastShift(myRef, time);
                }
            }
        });
    }

    public void changeClockOut(final String shiftID, final String employeeID, final Long time) {
        DatabaseReference shiftRef = thisRef.child("Employees").child(employeeID).child("shifts").child(shiftID).child("clockout");
        shiftRef.setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("clock in save failure");

                } else {
                    System.out.println("clockout change success");
                }
            }
        });

    }

    public void logError(String error){
       // FirebaseCrash.log(error);
        Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();

    }

    public void setLastShift(DatabaseReference myRef, final Long time){
        //takes employee reference, gives last shift references to use with setValue()
        final DatabaseReference[] myShiftRef = {null};
        Query recentShiftsQuery = myRef.child("shifts").limitToLast(1);
        recentShiftsQuery.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myShiftRef[0] = dataSnapshot.getChildren().iterator().next().getRef();
                        myShiftRef[0].child("clockout").setValue(time, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    logError("shift clock out time save failure");
                                } else {
                                    System.out.println("shift clock out time save  success");
                                }
                            }

                        });
                        myShiftRef[0].child("complete").setValue(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        logError("no shift data found");
                    }
                });

    }

    public void createOrder(String childType, Long total, Long timestamp, Map<String, OrderItem> items){
/*
        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        DoorOrder mDoorOrder = new DoorOrder(key, mercID, total, timestamp, false, items);
        newRef.setValue(mDoorOrder);
*/

        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        Map<String, OrderItem> newitems = new HashMap<String, OrderItem>();
        final BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, newitems);
        newRef.setValue(mBarOrder);

        for (Object value : items.values()) {
            DatabaseReference itemRef = newRef.child("items").push();
            OrderItem mOrderItem = (OrderItem) value;
            itemRef.setValue(mOrderItem);
        }

    }

    public void createChange(String childType, Long total, Long timestamp){
/*
        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        DoorOrder mDoorOrder = new DoorOrder(key, mercID, total, timestamp, false, items);
        newRef.setValue(mDoorOrder);
*/

        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        Map<String, OrderItem> newitems = new HashMap<String, OrderItem>();
        final BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, newitems);
        newRef.setValue(mBarOrder);

            DatabaseReference itemRef = newRef.child("items").push();
            OrderItem mOrderItem = new OrderItem("Change:"+ Long.toString(total), 0l, timestamp, 0l, false);
            itemRef.setValue(mOrderItem);

    }


    public void createBarOrder(String childType, Long total, Long timestamp, Map<String, OrderItem> items){
        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, items);
        newRef.setValue(mBarOrder);
    }

    public void createOrUpdateIPEOrders(final String childType, final ArrayList<Long> mOrderTotals, final ArrayList<Long> mOrderTimestamps, final ArrayList<Map<String, OrderItem>> mOrderItems, ArrayList<String> mOrderIds, final ArrayList<String> ipeIDList, final ArrayList<String> ipeNameList){

        for(int i=0; i<ipeIDList.size(); i++) {
            Query preQuery = null;
            preQuery = thisRef.child("Employees").child(ipeIDList.get(i)).child("orders").limitToLast(1);
            final int finalI = i;
            final int finalI1 = i;
            preQuery.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                    final String orderID = dataSnapshot.getChildren().iterator().next().getKey();
                                    System.out.println("filter order ref: " + orderID);

                                    DatabaseReference myRef = thisRef.child("IPEOrders");
                                    Query newquery = myRef.orderByKey().equalTo(orderID);
                                    System.out.println("filter order query: " + newquery.getRef() + "  orderID: " + orderID);
                                    newquery.addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    System.out.println("filter datasnap"+ dataSnapshot);
                                                    IPEOrder tOrder = dataSnapshot.getChildren().iterator().next().getValue(IPEOrder.class);
                                                    DatabaseReference tRef = dataSnapshot.getChildren().iterator().next().getRef();

                                                    System.out.println("filter order"+ tOrder);

                                                    //if recent order is open, update with additions
                                                    if (tOrder.gesOpen()) {
                                                        System.out.println("filter order open");

                                                        for (Object value : mOrderItems.get(finalI1).values()) {
                                                            DatabaseReference itemRef = thisRef.child("IPEOrders").child(orderID).child("items").push();
                                                                OrderItem mOrderItem = (OrderItem) value;
                                                                itemRef.setValue(mOrderItem);
                                                         }

                                                    }
                                                    //otherwise, create new
                                                    else {
                                                        System.out.println("filter order closed, create new");
                                                        DatabaseReference newRef = thisRef.child(childType).push();
                                                            String key = newRef.getKey();
                                                            //final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), mOrderItems.get(finalI));
                                                            Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                                                            final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), 0l, items);
                                                            newRef.setValue(mIPEOrder);

                                                        for (Object value : mOrderItems.get(finalI1).values()) {
                                                            DatabaseReference itemRef = thisRef.child("IPEOrders").child(key).child("items").push();
                                                            OrderItem mOrderItem = (OrderItem) value;
                                                            itemRef.setValue(mOrderItem);
                                                        }

                                                        DatabaseReference orderRef = thisRef.child("Employees").child(ipeIDList.get(finalI)).child("orders").child(key);
                                                            orderRef.setValue(key, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError != null) {
                                                                        logError("ipe order save failure");
                                                                    } else {
                                                                        System.out.println("ipe order save success");
                                                                    }
                                                                }

                                                            });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    System.out.println("ipe order not found to match employee:");
                                                }
                                    });

                            }
                            //else if no orders exist, create new
                            else
                            {
                                System.out.println("filter no orders, create new");
                                DatabaseReference newRef = thisRef.child(childType).push();
                                String key = newRef.getKey();
                                Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                                final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), 0l, items);
                                newRef.setValue(mIPEOrder);

                                for (Object value : mOrderItems.get(finalI1).values()) {
                                    DatabaseReference itemRef = thisRef.child("IPEOrders").child(key).child("items").push();
                                    OrderItem mOrderItem = (OrderItem) value;
                                    itemRef.setValue(mOrderItem);
                                }

                                    DatabaseReference orderRef = thisRef.child("Employees").child(ipeIDList.get(finalI)).child("orders").child(key);
                                    orderRef.setValue(key, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                logError("ipe order save failure");
                                            } else {
                                                System.out.println("ipe order save success");
                                            }
                                        }

                                    });

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("last order query cancelled");
                        }
                    });



/*            DatabaseReference newRef = thisRef.child(childType).push();
            String key = newRef.getKey();
            final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(i), ipeNameList.get(i),true, false, mOrderTotals.get(i), mOrderTimestamps.get(i), mOrderItems.get(i));
            newRef.setValue(mIPEOrder);

            DatabaseReference orderRef = thisRef.child("Employees").child(ipeIDList.get(i)).child("orders").child(key);
            orderRef.setValue(key, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        logError("ipe order save failure");
                    } else {
                        System.out.println("ipe order save success");
                    }
                }

            });*/


        // close iteration for i...
        }

    }

    public void voidIPEOrderItem(final DatabaseReference itemRef){
        Long time = Calendar.getInstance().getTimeInMillis();

        itemRef.child("voided").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void item failure");

                } else {
                    System.out.println("void item success");
                    DatabaseReference orderRef = itemRef.getParent().getParent();
                    System.out.println("update orderRef: " + orderRef);
                    updateOrderTotal(orderRef);
                }
            }
        });

        itemRef.child("voidtimestamp").setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void timestamp ipe item failure");

                } else {
                    System.out.println("void timestamp ipe item success");
                    DatabaseReference orderRef = itemRef.getParent().getParent();
                    System.out.println("update orderRef: " + orderRef);
                    updateOrderTotal(orderRef);
                }
            }
        });

    }



    public void updateOrderTotal(final DatabaseReference orderRef){
        final long[] totalPrice = {0};
        final long[] balance = {0};
        System.out.println("total start: " + totalPrice[0]);
        orderRef.child("items").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                            OrderItem mItem = mSnapshot.getValue(OrderItem.class);
                            if (!mItem.gesVoided()) {
                                balance[0] = balance[0] + mItem.gesPrice();
                                if (mItem.gesPrice() > 0) {
                                    totalPrice[0] = totalPrice[0] + mItem.gesPrice();
                                }
                            }

                        }
                        orderRef.child("total").setValue(totalPrice[0], new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference
                                    databaseReference) {
                                if (databaseError != null) {
                                    logError("order total update failure");

                                } else {
                                    System.out.println("order total update success");
                                }
                            }
                        });
                        orderRef.child("balance").setValue(balance[0], new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference
                                    databaseReference) {
                                if (databaseError != null) {
                                    logError("order balance update failure");

                                } else {
                                    System.out.println("order balance update success");
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    public void addIPEPaymentItem(String orderUniqueID, long payment){
        DatabaseReference itemRef = thisRef.child("IPEOrders").child(orderUniqueID).child("items").push();
        String key = itemRef.getKey();
        Long time = Calendar.getInstance().getTimeInMillis();
        OrderItem mOrderItem = new OrderItem("payment", payment, time, 0l, false);
        itemRef.setValue(mOrderItem);
    }

    public void voidIPEOrder(String orderUniqueID){
        Long time = Calendar.getInstance().getTimeInMillis();

        thisRef.child("IPEOrders").child(orderUniqueID).child("voided").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void order failure");

                } else {
                    System.out.println("void order success");
                }
            }
        });
        thisRef.child("IPEOrders").child(orderUniqueID).child("voidtimestamp").setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void timestamp ipeorder failure");

                } else {
                    System.out.println("void timestamp ipeorder success");
                }
            }
        });

    }


    public void voidBarOrder(String orderUniqueID){
        Long time = Calendar.getInstance().getTimeInMillis();

        thisRef.child("BarOrders").child(orderUniqueID).child("voided").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void order failure");

                } else {
                    System.out.println("void order success");
                }
            }
        });
        thisRef.child("BarOrders").child(orderUniqueID).child("voidtimestamp").setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void timestamp ipeorder failure");

                } else {
                    System.out.println("void timestamp ipeorder success");
                }
            }
        });

    }


    public void closeIPEOrder(String orderUniqueID){
        thisRef.child("IPEOrders").child(orderUniqueID).child("open").setValue(false, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("close order failure");

                } else {
                    System.out.println("close order success");
                }
            }
        });

    }

    public void voidDoorOrder(DatabaseReference doorRef){
        Long time = Calendar.getInstance().getTimeInMillis();

        doorRef.child("voided").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void order failure");

                } else {
                    System.out.println("void order success");
                }
            }
        });
        doorRef.child("voidtimestamp").setValue(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void timestamp doororder failure");

                } else {
                    System.out.println("void timestamp doororder success");
                }
            }
        });

    }



}
