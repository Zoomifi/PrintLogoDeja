package zomifi.op27no2.printlogo;

import java.util.Map;

/**
 * Created by CristMac on 7/9/16.
 */
public class BarOrder {
    public String uniqueID;
    public String merchantID;
    public Long total;
    public Long timestamp;
    public Long voidtimestamp;
    public Boolean voided;
    public Map<String, OrderItem> items;


    public BarOrder(){
    }

    public BarOrder(String uniqueID, String merchantID, Long total, Long timestamp, Long voidtimestamp, Boolean voided, Map<String, OrderItem> items){
        this.uniqueID = uniqueID;
        this.merchantID = merchantID;
        this.total = total;
        this.timestamp = timestamp;
        this.voidtimestamp = voidtimestamp;
        this.voided = voided;
        this.items = items;
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
    public Long gesTimestamp(){
        return timestamp;
    }
    public Long gesVoidTimestamp(){
        return voidtimestamp;
    }
    public Boolean gesVoided(){
        return voided;
    }
    public Map<String, OrderItem> items(){
        return items;
    }



}