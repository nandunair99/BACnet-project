import bacnetexception.BacNetConfigurationException;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import covchecker.schedulerexception.COVSchedulerException;
import listener.IBacNetVariableListener;
import wrapper.BacNetClientConfigurer;
import wrapper.BacNetObjectWrapper;


public class MainClass {
    private static BacNetClientConfigurer client;

    public static void main(String[] args) throws COVSchedulerException {
        try {
            int unitObjectId = 957701;
            int objectId = 2;
            startBacNetClient("2.234.210.62", 47808, 20000);
            getBacNetObject(unitObjectId, objectId);
            getBacNetObjectValue(unitObjectId, objectId);

            setBacNetObjectValue(unitObjectId, 9, BinaryPV.inactive);
            getBacNetObjectValue(unitObjectId, objectId);
            IBacNetVariableListener listener = startValueChangeListener(unitObjectId, 2, new ThirdPartyCl());
            startValueChangeListener(unitObjectId, 1, new ThirdPartyCl());
            Thread.sleep(15000);
            stopValueChangeListener(listener);
            //stopBacNetClient();

        } catch (BacNetConfigurationException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static void startBacNetClient(String broadcastId, int portNo, int timeout) throws BacNetConfigurationException {
        //Initalizing the BACnetClient with default timeout of 20 seconds
        client = new BacNetClientConfigurer(broadcastId, portNo);
        //Initalizing the BACnetClient with timeout of 20 seconds
//            BacNetClientConfigurer client = new BacNetClientConfigurer("2.234.210.62", 47808,timeout);
    }

    private static void getBacNetObject(int unitObjectId, int objectId) throws BacNetConfigurationException {
        //Reads properties of BACnetObjects
        BacNetObjectWrapper object = client.readBacNetObject(unitObjectId, objectId);
        System.out.println("Object: " +
                " Device: " + object.getDevice() +
                " Name: " + object.getName() +
                " Description: " + object.getDescription());
    }

    private static void getBacNetObjectValue(int unitObjectId, int objectId) throws BacNetConfigurationException {
        //Gets value for the specified BacNetObject of the specified Device
        Object propertyValue = client.getBacNetObjectValue(unitObjectId, objectId);
        System.out.println("Property Value " + propertyValue.toString());
    }

    private static void setBacNetObjectValue(int unitObjectId, int objectId, Object value) throws BacNetConfigurationException {
        //Sets Value for specified BacNetObject for the specified Device
        client.setBacNetObjectValue(unitObjectId, objectId, value);
    }

    private static IBacNetVariableListener startValueChangeListener(int unitObjectId, int objectId, IBacNetVariableListener bacNetVariableListener) throws BacNetConfigurationException, COVSchedulerException {
        //Add value change listener for the specified BacNetObject of the specified Device
        return client.addValueChangeListener(unitObjectId, objectId, bacNetVariableListener);
    }

    private static void stopValueChangeListener(IBacNetVariableListener bacNetVariableListener) throws COVSchedulerException, BacNetConfigurationException {
        //stop Change of value listener
        client.stopValueChangeListener(bacNetVariableListener);
    }

    private static void stopBacNetClient() throws BacNetConfigurationException {
        //close the BacNetClient resource
        client.close();
    }
}
