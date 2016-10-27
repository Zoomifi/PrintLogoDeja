package zomifi.op27no2.printlogo;

/**
 * Created by CristMac on 7/9/16.
 */
public class BarOrderItem {
    public String name;
    public long price;
    public long timestamp;

    //don;t need this class?

    public BarOrderItem(){
    }

    public BarOrderItem(String name, Long price, Long timestamp){
        this.name = name;
        this.price = price;
        this.timestamp = timestamp;
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



}