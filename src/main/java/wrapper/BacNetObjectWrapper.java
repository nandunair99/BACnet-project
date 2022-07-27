package wrapper;

import org.code_house.bacnet4j.wrapper.api.BacNetObject;

/**
 * BacNetObjectWrapper class is a wrapper to BacNetObject to provide additional functionalities
 */
public class BacNetObjectWrapper extends BacNetObject {

    /**
     * @param bacNetObject BacNetObject of a device
     * @implNote Constructor for BacNetObject Wrapper
     */
    private static Object currentValue;
    public BacNetObjectWrapper(BacNetObject bacNetObject) {
        super(bacNetObject.getDevice(), bacNetObject.getId(), bacNetObject.getType(), bacNetObject.getName(), bacNetObject.getDescription(), bacNetObject.getUnits());
    }

    public static Object getCurrentValue() {
        return currentValue;
    }

    public static void setCurrentValue(Object currentValue) {
        BacNetObjectWrapper.currentValue = currentValue;
    }
}
