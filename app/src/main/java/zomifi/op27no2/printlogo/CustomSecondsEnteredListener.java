package zomifi.op27no2.printlogo;

/**
 * Created by Andrew on 1/16/2016.
 * Used for Delivery Fee application for Zoomifi Inc.
 */

public interface CustomSecondsEnteredListener
{
    /**
     * Interface for communication between instantiated classes and parent Services/Activities.
     */

    /**
     * Bridge method to set the price of the delivery fee of the current Order.
     */

    void setSeconds(long seconds);

    /**
     * Bridge method to change the labels for the default (static) price entries on the dialog
     * that displays to an EU when an Order is first created.
     * @param PRICE_STRING : The String representation of the price.
     */

    void changeButton(String PRICE_STRING);
}
