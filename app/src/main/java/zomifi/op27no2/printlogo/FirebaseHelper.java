package zomifi.op27no2.printlogo;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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
   // private Handler handler = new Handler();


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
        newRef.setValue(mEmployee, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("new employee success");
                }
            }
        });

        EmployeeLight mEmployeeLight = new EmployeeLight(stagename, key);
        DatabaseReference newRef2 =  thisRef.child("EmployeeNames");
        newRef2.child(name).setValue(mEmployeeLight, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("new employee success");
                }
            }
        });

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

        ipeRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("update employee success");
                }
            }
        });

    }

    public void clockIn(final DatabaseReference myRef, final String employeeID, final Long time) {
        myRef.child("clocked").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("clock in save failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("clockin success");
                    DatabaseReference newRef = myRef.child("shifts").push();
                    String key = newRef.getKey();
                    Shift mShift = new Shift(key, employeeID, time, null, false);
                    newRef.setValue(mShift, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                logError("clock in time save failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                            } else {
                                System.out.println("shift time save success");
                                edt.putBoolean(employeeID + "clocked", true);
                                edt.commit();
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
                    logError("time save failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());
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
                    logError("clock in save failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());

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
                    logError("clock in save failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());
                } else {
                    System.out.println("clockout success");
                    setLastShift(myRef, time);
                    edt.putBoolean(employeeID+"clocked", false);
                    edt.commit();
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
                    logError("clock in save failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());

                } else {
                    System.out.println("clockout change success");
                }
            }
        });

    }

    public void logError(String error){

        Crashlytics.logException(new Exception(error));
        CustomErrorDialog customDialog = new CustomErrorDialog(error,mContext);
        customDialog.setCancelable(false);
        customDialog.show();

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
                                    logError("shift clock out time save failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());
                                } else {
                                    System.out.println("shift clock out time save  success");
                                }
                            }

                        });
                        myShiftRef[0].child("complete").setValue(true, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    logError("shift complete failure reference:" + databaseReference.toString() + " error:" + databaseError.toString());
                                } else {
                                    System.out.println("shift complete sucess");
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        logError("no shift data found" + " error:" + databaseError.toString());
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
        final BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, newitems, false);
        newRef.setValue(mBarOrder, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("creat bar order errorreference:" + databaseReference.toString() + " error:" + databaseError.toString());
                } else {
                    System.out.println("bar order success");
                }
            }
        });

        for (Object value : items.values()) {
            DatabaseReference itemRef = newRef.child("items").push();
            OrderItem mOrderItem = (OrderItem) value;
            itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        logError("bar order items error reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                    } else {
                        System.out.println("bar order items success");
                    }
                }
            });
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
        final BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, newitems, true);
        newRef.setValue(mBarOrder, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("bar order change error reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("change success");
                }
            }
        });

            DatabaseReference itemRef = newRef.child("items").push();
            OrderItem mOrderItem = new OrderItem("Change:"+Long.toString(total), "Change", 0l, timestamp, 0l, 0l, false, false, false, false);
            itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        logError("change items errorreference:"+databaseReference.toString() + " error:" +databaseError.toString());
                    } else {
                        System.out.println("change items success");
                        Toast.makeText(mContext, "Change Tracked", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }


    public void createBarOrder(String childType, Long total, Long timestamp, Map<String, OrderItem> items){
        DatabaseReference newRef =  thisRef.child(childType).push();
        String key = newRef.getKey();
        BarOrder mBarOrder = new BarOrder(key, mercID, total, timestamp, 0l, false, items, false);
        newRef.setValue(mBarOrder, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("create bar order errorreference:"+databaseReference.toString() + " error:" +databaseError.toString());
                } else {
                    System.out.println("bar order success");
                }
            }
        });
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

                                    DatabaseReference myRef = thisRef.child("IPEOrders");
                                    Query newquery = myRef.orderByKey().equalTo(orderID);
                                    newquery.addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    //System.out.println("filter datasnap"+ dataSnapshot);
                                                    IPEOrder tOrder = dataSnapshot.getChildren().iterator().next().getValue(IPEOrder.class);
                                                    DatabaseReference tRef = dataSnapshot.getChildren().iterator().next().getRef();


                                                    //if recent order is open, update with additions
                                                    if (tOrder.gesOpen()) {

                                                        for (Object value : mOrderItems.get(finalI1).values()) {
                                                            DatabaseReference itemRef = thisRef.child("IPEOrders").child(orderID).child("items").push();
                                                                OrderItem mOrderItem = (OrderItem) value;
                                                                itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        if (databaseError != null) {
                                                                            logError("ipe order item error reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                                                                        } else {
                                                                        }
                                                                    }
                                                                });
                                                         }

                                                    }
                                                    //otherwise, create new
                                                    else {
                                                        DatabaseReference newRef = thisRef.child(childType).push();
                                                            String key = newRef.getKey();
                                                            //final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), mOrderItems.get(finalI));
                                                            Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                                                            final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), 0l, items);
                                                            newRef.setValue(mIPEOrder, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError != null) {
                                                                        logError("new ipe order item error reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                                                                    } else {

                                                                    }
                                                                }
                                                            });

                                                        for (Object value : mOrderItems.get(finalI1).values()) {
                                                            DatabaseReference itemRef = thisRef.child("IPEOrders").child(key).child("items").push();
                                                            OrderItem mOrderItem = (OrderItem) value;
                                                            itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError != null) {
                                                                        logError("ipe order items error 2 reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                                                                    } else {

                                                                    }
                                                                }
                                                            });
                                                        }

                                                        DatabaseReference orderRef = thisRef.child("Employees").child(ipeIDList.get(finalI)).child("orders").child(key);
                                                            orderRef.setValue(key, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError != null) {
                                                                        logError("ipe order save failure: "+databaseReference.toString() + " error:" +databaseError.toString());
                                                                    } else {

                                                                    }
                                                                }

                                                            });
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    System.out.println("ipe order not found to match employee:");
                                                    logError("ipe order not found to match employee error:" +databaseError.toString());
                                                }
                                    });

                            }
                            //else if no orders exist, create new
                            else
                            {

                                DatabaseReference newRef = thisRef.child(childType).push();
                                String key = newRef.getKey();
                                Map<String, OrderItem> items = new HashMap<String, OrderItem>();
                                final IPEOrder mIPEOrder = new IPEOrder(key, mercID, ipeIDList.get(finalI), ipeNameList.get(finalI), true, false, mOrderTotals.get(finalI),mOrderTotals.get(finalI), mOrderTimestamps.get(finalI), 0l, items);
                                newRef.setValue(mIPEOrder, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            logError("ipe order save failure2: "+databaseReference.toString() + " error:" +databaseError.toString());
                                        } else {

                                        }
                                    }

                                });

                                for (Object value : mOrderItems.get(finalI1).values()) {
                                    DatabaseReference itemRef = thisRef.child("IPEOrders").child(key).child("items").push();
                                    OrderItem mOrderItem = (OrderItem) value;
                                    itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                logError("ipe order item save failure: "+databaseReference.toString() + " error:" +databaseError.toString());
                                            } else {

                                            }
                                        }

                                    });
                                }

                                    DatabaseReference orderRef = thisRef.child("Employees").child(ipeIDList.get(finalI)).child("orders").child(key);
                                    orderRef.setValue(key, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                logError("ipe order save failure3"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());
                                            } else {

                                            }
                                        }

                                    });

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("last order query cancelled");
                            logError("last order query error:" +databaseError.toString());
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
                    logError("void item failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("void timestamp ipe item failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

                } else {
                    System.out.println("void timestamp ipe item success");
                    DatabaseReference orderRef = itemRef.getParent().getParent();
                    System.out.println("update orderRef: " + orderRef);
                    updateOrderTotal(orderRef);
                }
            }
        });

    }

    public void setIPELineItemDiscount(long discount, final DatabaseReference itemRef, long price, long previousDiscount){

        long newprice;
        if((price - discount)<0){
            newprice = 0;
            discount = price;
        }
        else{

            newprice = price - discount;

        }

        itemRef.child("hasDiscount").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("discount item failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

                } else {
                    System.out.println("discount item success");
                    DatabaseReference orderRef = itemRef.getParent().getParent();
                    System.out.println("update orderRef: " + orderRef);
                    updateOrderTotal(orderRef);
                }
            }
        });

        itemRef.child("price").setValue(newprice, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("discount ipe item price failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

                } else {
                    System.out.println("discount ipe item success");
                    DatabaseReference orderRef = itemRef.getParent().getParent();
                    System.out.println("update orderRef: " + orderRef);
                    updateOrderTotal(orderRef);
                }
            }
        });

        itemRef.child("discount").setValue((discount+previousDiscount), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("discount ipe item price failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

                } else {
                    System.out.println("discount ipe item success");
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
                                    logError("order total update failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());

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
                                    logError("order balance update failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());

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
        OrderItem mOrderItem = new OrderItem("payment", "Payment", payment, time, 0l, 0l, false, false, false, false);
        itemRef.setValue(mOrderItem, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    logError("ipe payment failure: " + databaseReference.toString() + " error:" + databaseError.toString());
                } else {
                    System.out.println("ipe payment success");
                }
            }

        });
    }

    public void voidIPEOrder(String orderUniqueID){
        Long time = Calendar.getInstance().getTimeInMillis();

        thisRef.child("IPEOrders").child(orderUniqueID).child("voided").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference
                    databaseReference) {
                if (databaseError != null) {
                    logError("void order failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("void timestamp ipeorder failure" + " reference:" + databaseReference.toString() + " error:" + databaseError.toString());

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
                    logError("void order failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("void timestamp ipeorder failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("close order failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("void order failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

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
                    logError("void timestamp doororder failure"+" reference:"+databaseReference.toString() + " error:" +databaseError.toString());

                } else {
                    System.out.println("void timestamp doororder success");
                }
            }
        });

    }


    public  Map<String,String> retrieveIPEs(final onGetEmployees listener){
        System.out.println("retrieve IPE start");

        final Long start = System.currentTimeMillis();
        listener.onStart();

        final Map<String,String> myUniqueIDs = new HashMap<String, String>();

        DatabaseReference myRef = thisRef.child("Employees");
        Query newquery = myRef.orderByKey();
        System.out.println("employee query: " + newquery.getRef());
        newquery.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       // System.out.println("filter datasnap" + dataSnapshot);
                        for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                            Employee mEmployee = mSnapshot.getValue(Employee.class);
                            String id = mEmployee.gesUniqueID();
                            myUniqueIDs.put(mEmployee.gesName(),id );
                            if (mEmployee.gesClocked() == true) {
                                edt.putBoolean(id + "clocked", true);
                            } else {
                                edt.putBoolean(id + "clocked", false);
                            }
                            System.out.println("employee id" + id);
                            edt.putString(id+"name", mEmployee.gesName());
                            edt.commit();
                        }
                        listener.onSuccess(myUniqueIDs);
                        Long finish = System.currentTimeMillis();
                        System.out.println("retrieve IPE duration: "+(finish-start)+" milliseconds");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("error looking for employees" + databaseError.toString());
                        logError("retrieving employee list error:" + databaseError.toString());
                        listener.onFailed(databaseError);
                    }
                });




        return myUniqueIDs;
    }

    public  ArrayList<String> retrieveIPENames(final onGetEmployeeNames listener){
        listener.onStart();

        final ArrayList<String> myNames = new ArrayList<String>();

        DatabaseReference myRef = thisRef.child("Employees");
        Query newquery = myRef.orderByKey();
        System.out.println("employee name query: " + newquery.getRef());
        newquery.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    //    System.out.println("filter datasnap" + dataSnapshot);
                        for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                            Employee mEmployee = mSnapshot.getValue(Employee.class);
                            myNames.add(mEmployee.gesName());
                            System.out.println("employee name:" + mEmployee.gesName());

                        }
                        listener.onSuccess(myNames);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("error looking for employee names" + databaseError.toString());
                        logError("retrieving employee name list error:" + databaseError.toString());
                        listener.onFailed(databaseError);
                    }
                });


        return myNames;
    }

    public  Map<String,String> retrieveIPENamesLight(final onGetEmployeeNamesLight listener){
        listener.onStart();

        final Long start = System.currentTimeMillis();

        final Map<String,String> mEmployees = new HashMap<String, String>();

        DatabaseReference myRef = thisRef.child("EmployeeNames");
        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                    //    System.out.println("filter datasnap" + dataSnapshot);
                        for (DataSnapshot mSnapshot : dataSnapshot.getChildren()) {
                            EmployeeLight mEmployee = mSnapshot.getValue(EmployeeLight.class);

                            mEmployees.put(mEmployee.gesStageName(), mEmployee.gesUniqueID());


                        }
                        listener.onSuccess(mEmployees);
                        Long finish = System.currentTimeMillis();
                        System.out.println("retrieve IPE duration: "+(finish-start)+" milliseconds");

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("error looking for employee names" + databaseError.toString());
                        logError("retrieving employee name list error:" + databaseError.toString());
                        listener.onFailed(databaseError);
                    }
                });


        return mEmployees;
    }

    public void addEmployeeNameList(String name, final String uniqueID){
        System.out.println("adding employee: "+name+" "+uniqueID);
        if(name !=null && !name.equals("") && uniqueID !=null) {
            DatabaseReference newRef2 = thisRef.child("EmployeeNames").child(name);
            EmployeeLight mEmployeeLight = new EmployeeLight(name, uniqueID);
            newRef2.setValue(mEmployeeLight, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        logError("reference:" + databaseReference.toString() + " error:" + databaseError.toString());
                    } else {
                        System.out.println("new employee success");
                        edt.putBoolean("updated" + uniqueID, true);
                        edt.commit();
                    }
                }
            });
        }

    }

}
