package zomifi.op27no2.printlogo;

import java.util.Map;

/**
 * Created by CristMac on 7/9/16.
 */
public class DoorOrder {
    public String uniqueID;
    public String merchantID;
    public Long total;
    public Long timestamp;
    public Long voidtimestamp;
    public Boolean voided;
    public Map<String, OrderItem> items;


    public DoorOrder(){
    }

    public DoorOrder(String uniqueID, String merchantID, Long total, Long timestamp,Long voidtimestamp, Boolean voided, Map<String, OrderItem> items){
        this.uniqueID = uniqueID;
        this.merchantID = merchantID;
        this.total = total;
        this.timestamp = timestamp;
        this.voidtimestamp = voidtimestamp;
        this.items = items;
        this.voided = voided;

    }

    public String gesMerchantID(){
        return merchantID;
    }
    public String gesUniqueID(){
        return uniqueID;
    }
    public Long gesTotal(){
        return total;
    }
    public Boolean gesVoided(){
        return voided;
    }
    public Long gesTimestamp(){
        return timestamp;
    }
    public Long gesVoidTimestamp(){
        return voidtimestamp;
    }
    public Map<String, OrderItem> items(){
        return items;
    }



}