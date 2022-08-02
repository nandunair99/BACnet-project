package wrapper;

import listener.IBacNetVariableListener;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;

/**
 * BacNetObjectWrapper class is a wrapper to BacNetObject to provide additional functionalities
 */
public class BacNetObjectWrapper extends BacNetObject {

    /**
     * @param bacNetObject BacNetObject of a device
     * @implNote Constructor for BacNetObject Wrapper
     */
    private IBacNetVariableListener iBacNetVariableListener = null;
    private Object currentValue;

    public BacNetObjectWrapper(BacNetObject bacNetObject) {
        super(bacNetObject.getDevice(), bacNetObject.getId(), bacNetObject.getType(), bacNetObject.getName(), bacNetObject.getDescription(), bacNetObject.getUnits());
    }

    public Object getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue;
    }

    public IBacNetVariableListener getiBacNetVariableListener() {
        return iBacNetVariableListener;
    }

    public void setiBacNetVariableListener(IBacNetVariableListener iBacNetVariableListener) {
        this.iBacNetVariableListener = iBacNetVariableListener;
    }

}
