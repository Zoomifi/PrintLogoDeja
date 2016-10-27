package zomifi.op27no2.printlogo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by CristMac on 7/9/16.
 */
public class Employee {
    public String name;
    public String stagename;
    public String uniqueID;
    public String status;
    public String addressStreet;
    public String addressCity;
    public String addressState;
    public String addressZip;
    public String phone1;
    public String phone2;
    public String ssn;
    public String notes;
    public String balance;
    public String custom;
    public Boolean clocked;
    public Long lastTime;
    public Map<String, Shift> shifts;

    public Employee(){
    }

    public Employee(String name, String stagename, String uniqueID, String status, String addressStreet, String addressCity, String addressState, String addressZip, String notes, String phone1, String phone2, String ssn,  String balance, String custom, Boolean clocked, Long lastTime, Map<String, Shift> shifts){
        this.name = name;
        this.stagename = stagename;
        this.uniqueID = uniqueID;
        this.status = status;
        this.addressStreet = addressStreet;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressZip = addressZip;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.ssn = ssn;
        this.notes = notes;
        this.balance = balance;
        this.custom = custom;
        this.clocked = clocked;
        this.lastTime = lastTime;
        this.shifts = shifts;
    }

    public String gesName(){
        return name;
    }
    public String gesStageName(){
        return stagename;
    }
    public String gesUniqueID(){
        return uniqueID;
    }
    public String gesStatus(){
        return status;
    }
    public String gesAddressStreet(){
        return addressStreet;
    }
    public String gesAddressCity(){
        return addressCity;
    }
    public String gesAddressState(){
        return addressState;
    }
    public String gesAddressZip(){
        return addressZip;
    }
    public String gesNotes(){
        return notes;
    }
    public String gesPhone1(){
        return phone1;
    }
    public String gesPhone2(){
        return phone2;
    }
    public String gesSSN(){
        return ssn;
    }
    public String gesBalance(){
        return balance;
    }
    public String gesCustom(){
        return custom;
    }
    public Boolean gesClocked(){
        return clocked;
    }
    public Map<String, Shift> gesShifts(){
        return shifts;
    }
    public String gesLastTime(){
        String mTime = "";
        if(lastTime != null) {
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss a");
            mTime = df.format(lastTime);
        }
        return mTime;
    }

    public ArrayList<String> gesFields(){
        ArrayList<String> myList = new ArrayList<String>();
        myList.add(name);
        myList.add(stagename);
        myList.add(addressStreet);
        myList.add(addressCity);
        myList.add(addressZip);
        myList.add(notes);
        myList.add(custom);
        myList.add(phone1);
        myList.add(phone2);
        myList.add(ssn);
        myList.add(balance);
        return myList;
    }


}
