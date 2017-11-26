package zomifi.op27no2.printlogo;

/**
 * Created by Andrew on 1/16/2016.
 * Used for Delivery Fee application for Zoomifi Inc.
 */

public interface CustomPriceEnteredListener
{
    /**
     * Interface for communication between instantiated classes and parent Services/Activities.
     */

    /**
     * Bridge method to set the price of the delivery fee of the current Order.
     * @param orderID : Order ID of the current Order.
     * @param price : Price that the EU entered.
     */

    void setPrice(String orderID, String customName, int mode, long price, Boolean isPayment);

    /**
     * Bridge method to change the labels for the default (static) price entries on the dialog
     * that displays to an EU when an Order is first created.
     * @param buttonID : The internal ID of the button. Note: This does NOT refer to the R.id
     *                 value, but an internal ID used within the code to identify between the
     *                 two.
     * @param PRICE_STRING : The String representation of the price.
     */

    void changeButton(int buttonID, String PRICE_STRING);
}
