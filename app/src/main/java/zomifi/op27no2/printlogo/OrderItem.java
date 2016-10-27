package zomifi.op27no2.printlogo;

/**
 * Created by CristMac on 7/9/16.
 */
public class OrderItem {
    public String name;
    public long price;
    public long timestamp;
    public long voidtimestamp;
    public Boolean voided;


    public OrderItem(){
    }

    public OrderItem(String name, Long price, Long timestamp, Long voidtimestamp, Boolean voided){
        this.name = name;
        this.price = price;
        this.timestamp = timestamp;
        this.voidtimestamp = voidtimestamp;
        this.voided = voided;
    }

    public String gesName(){
        return name;
    }

    public Long gesPrice(){
        return price;
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



}