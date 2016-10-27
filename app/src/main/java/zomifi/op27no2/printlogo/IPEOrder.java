package zomifi.op27no2.printlogo;

import java.util.Map;

/**
 * Created by CristMac on 7/9/16.
 */
public class IPEOrder {
    public String uniqueID;
    public String merchantID;
    public String employeeID;
    public String employeeName;
    public Boolean open;
    public Boolean voided;
    public Long total;
    public Long balance;
    public Long timestamp;
    public Long voidtimestamp;
    public Map<String, OrderItem> items;


    public IPEOrder(){
    }

    public IPEOrder(String uniqueID, String merchantID, String employeeID,String employeeName, Boolean open, Boolean voided, Long total, Long balance, Long timestamp, Long voidtimestamp, Map<String, OrderItem> items){
        this.uniqueID = uniqueID;
        this.merchantID = merchantID;
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.open = open;
        this.voided = voided;
        this.total = total;
        this.balance = balance;
        this.timestamp = timestamp;
        this.voidtimestamp = voidtimestamp;
        this.items = items;
    }

    public String gesMerchantID(){
        return merchantID;
    }
    public String gesUniqueID(){
        return uniqueID;
    }
    public String gesEmployeeID(){
        return employeeID;
    }
    public String gesEmployeeName(){
        return employeeName;
    }
    public Boolean gesOpen(){
        return open;
    }
    public Boolean gesVoided(){
        return voided;
    }
    public Long gesTotal(){
        return total;
    }
    public Long gesBalance(){
        return balance;
    }
    public Long gesTimestamp(){
        return timestamp;
    }
    public Long gesVoidTimestamp(){
        return voidtimestamp;
    }
    public Map<String, OrderItem> gesItems(){
        return items;
    }


}