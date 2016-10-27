package zomifi.op27no2.printlogo;

/**
 * Created by Andrew on 1/16/2016.
 * Used for Delivery Fee application for Zoomifi Inc.
 */

public interface CustomManagerListener
{
    /**
     * Interface for communication between instantiated classes and parent Services/Activities.
     */



    void managerCallback(int resourceID);

    /**
     * Bridge method to change the labels for the default (static) price entries on the dialog
     * that displays to an EU when an Order is first created.
     * @param buttonID : The internal ID of the button. Note: This does NOT refer to the R.id
     *                 value, but an internal ID used within the code to identify between the
     *                 two.
     * @param PRICE_STRING : The String representation of the price.
     */


}
