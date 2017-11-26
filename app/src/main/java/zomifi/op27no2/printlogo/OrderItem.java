package zomifi.op27no2.printlogo;

/**
 * Created by CristMac on 7/9/16.
 */
public class OrderItem {
    public String name;
    public String category;
    public long price;
    public long discount;
    public long timestamp;
    public long voidtimestamp;
    public Boolean voided;
    public Boolean isvcash;
    public Boolean isfee;
    public Boolean hasDiscount;


    public OrderItem(){
    }

    public OrderItem(String name, String category, Long price, Long timestamp, Long voidtimestamp, Long discount, Boolean voided, Boolean isvcash, Boolean isfee, Boolean hasDiscount){
        this.name = name;
        this.category = category;
        this.price = price;
        this.timestamp = timestamp;
        this.voidtimestamp = voidtimestamp;
        this.voided = voided;
        this.isvcash = isvcash;
        this.isfee = isfee;
        this.hasDiscount = hasDiscount;
        this.discount = discount;
    }

    public String gesName(){
        return name;
    }
    public String gesCategory(){
        return category;
    }

    public Long gesPrice(){
        return price;
    }
    public Long gesDiscount(){
        return discount;
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
    public Boolean gesIsVcash(){
        return isvcash;
    }
    public Boolean gesIsFee(){
        return isvcash;
    }
    public Boolean gesHasDiscount(){
        return hasDiscount;
    }



}