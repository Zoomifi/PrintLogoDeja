package zomifi.op27no2.printlogo;

/**
 * Created by CristMac on 7/9/16.
 */
public class Shift {
    public String uniqueID;
    public String employeeID;
    public Long clockin;
    public Long clockout;
    public Boolean complete;


    public Shift(){
    }

    public Shift(String uniqueID, String employeeID, Long clockin, Long clockout, Boolean complete){
        this.uniqueID = uniqueID;
        this.employeeID = employeeID;
        this.clockin = clockin;
        this.clockout = clockout;
        this.complete = complete;
    }

    public String gesEmployeeID(){
        return employeeID;
    }

    public String gesUniqueID(){
        return uniqueID;
    }
    public Long gesClockIn(){
        return clockin;
    }
    public Long gesClockOut(){
        if(clockout == null){
            clockout = 0l;
        }
        return clockout;
    }
    public Boolean gesComplete(){
        return complete;
    }


}