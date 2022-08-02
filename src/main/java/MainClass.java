import CustomException.BacNetConfigurationException;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import listener.IBacNetVariableListener;
import org.quartz.SchedulerException;
import wrapper.BacNetClientConfigurer;
import wrapper.BacNetObjectWrapper;

import java.util.Scanner;


public class MainClass {
    public static void main(String[] args) throws SchedulerException {
        try {
            int unitObjectId = 957701;
            int objectId = 2;
            BacNetClientConfigurer client = new BacNetClientConfigurer("2.234.210.62", 47808);
            BacNetObjectWrapper object = client.readBacNetObject(unitObjectId, objectId);
            System.out.println("Object: " +
                    " Device: " + object.getDevice().toString() +
                    " Name: " + object.getName().toString() +
                    " Description: " + object.getDescription().toString());

            Object propertyValue = client.getBacNetObjectValue(unitObjectId, objectId);
            System.out.println("Property Value Before " + propertyValue.toString());
            //client.setBacNetObjectValue(unitObjectId, objectId, 25f);
            Object propertyValue2 = client.getBacNetObjectValue(unitObjectId, objectId);
            System.out.println("Property Value After " + propertyValue2.toString());

            client.addValueChangeListener(unitObjectId, 1, new ThirdPartyCl());
            IBacNetVariableListener listener= client.addValueChangeListener(unitObjectId, 2, new ThirdPartyCl());
            client.addValueChangeListener(unitObjectId, 9, new ThirdPartyCl());

            Thread.sleep(Long.parseLong("15000"));
            System.out.println("after sleep");
            client.stopValueChangeListener(listener);

        } catch (BacNetConfigurationException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
