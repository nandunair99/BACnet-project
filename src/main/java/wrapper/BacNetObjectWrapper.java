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

    public BacNetObjectWrapper(BacNetObject bacNetObject) {
        super(bacNetObject.getDevice(), bacNetObject.getId(), bacNetObject.getType(), bacNetObject.getName(), bacNetObject.getDescription(), bacNetObject.getUnits());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
