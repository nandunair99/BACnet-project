package wrapper;

import bacnetexception.BacNetConfigurationException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import covchecker.COVScheduler;
import covchecker.JobIdentifier;
import covchecker.schedulerexception.COVSchedulerException;
import enums.DeviceType;
import listener.BacNetVariableListenerWrapper;
import listener.IBacNetVariableListener;
import org.code_house.bacnet4j.wrapper.api.BacNetObject;
import org.code_house.bacnet4j.wrapper.api.BypassBacnetConverter;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.quartz.SchedulerException;
import util.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * BacNetClientConfigurer class is a wrapper to BacNetIpClient which can be used to-
 * <ul>
 *     <li>Configure BacNetClient to connect the BacNet Gateway</li>
 *     <li>Access Indoor Devices connected to the gateway</li>
 *     <li>Access BacNetObjects of the Indoor devices</li>
 *     <li>Get and set values to those BacNetObjects</li>
 * </ul>
 */
public class BacNetClientConfigurer extends BacNetIpClient implements AutoCloseable {

    private Map<Integer, Device> devices = new HashMap<>();
    private COVScheduler notifier = null;


    /**
     * @param broadcastId String value for BroadcastId of the BACnet gateway
     * @param portNo      Integer Value for Port no of the BACnet Gateway
     * @throws BacNetConfigurationException
     * @throws SchedulerException
     * @implNote Constructor to configure the BacNetClient with required credentials
     */
    public BacNetClientConfigurer(String broadcastId, int portNo) throws BacNetConfigurationException {
        super(broadcastId, portNo);
        this.start();
        setDevices(20000);
        this.notifier = new COVScheduler(this);
    }

    /**
     * @param broadcastId String value for BroadcastId of the BACnet gateway
     * @param portNo      Integer Value for Port no of the BACnet Gateway
     * @param timeout     given number is timeout in millis
     * @throws BacNetConfigurationException
     * @throws SchedulerException
     * @implNote Constructor to configure the BacNetClient with required credentials
     */
    public BacNetClientConfigurer(String broadcastId, int portNo, long timeout) throws BacNetConfigurationException {
        super(broadcastId, portNo);
        this.start();
        setDevices(timeout);
        this.notifier = new COVScheduler(this);
    }

    public COVScheduler getNotifier() {
        return notifier;
    }


    /**
     * @param unitObjectId Object ID of the device
     * @param objectId     BacNetObject ID for a particular device
     * @param listener     IBacNetVariableListener object
     * @return IBacNetVariableListener
     * @throws BacNetConfigurationException
     * @throws COVSchedulerException
     * @implNote Adds a Change of Value listener for the specified BacNetObject of the specified Device
     */
    public IBacNetVariableListener addValueChangeListener(int unitObjectId, int objectId, IBacNetVariableListener listener) throws BacNetConfigurationException, COVSchedulerException {

        try {
            BacNetVariableListenerWrapper bacNetVariableListenerWrapper = new BacNetVariableListenerWrapper();
            bacNetVariableListenerWrapper.setiBacNetVariableListener(listener);
            bacNetVariableListenerWrapper.setOldValue(getBacNetObjectValue(unitObjectId, objectId));
            JobIdentifier jobIdentifier = notifier.scheduleJob(unitObjectId, objectId, bacNetVariableListenerWrapper);
            notifier.addListenerToListenerRegistry(jobIdentifier, bacNetVariableListenerWrapper);
            return listener;
        } catch (COVSchedulerException e) {
            throw new COVSchedulerException(e);
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    /**
     * @param iBacNetVariableListener
     * @throws COVSchedulerException
     * @throws BacNetConfigurationException
     * @implNote Stops the listener from listening for change of value for the specified BacNetObject of the specified Device
     */
    public boolean stopValueChangeListener(IBacNetVariableListener iBacNetVariableListener) throws COVSchedulerException, BacNetConfigurationException {
        try {
            JobIdentifier jobIdentifier = notifier.getListenerJobIdentifier(iBacNetVariableListener);
            return notifier.stopJob(jobIdentifier);
        } catch (COVSchedulerException e) {
            throw new COVSchedulerException(e);
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    /**
     * @param timeout-given number is timeout in millis
     * @throws BacNetConfigurationException
     * @implNote Collects all the detected Indoor unit devices during the timeout into a Map<Integer,Device>
     */
    private void setDevices(long timeout) throws BacNetConfigurationException {
        try {
            for (Device device : this.discoverDevices(timeout)) {
                if (device.getName().equalsIgnoreCase(DeviceType.INDOOR_UNIT.getType())) {
                    devices.put(device.getInstanceNumber(), device);
                }
            }
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    /**
     * @param device Indoor Units whose BacNetObjects are to be accessed
     * @return Map<Integer, BacNetObjectWrapper>
     * @throws BacNetConfigurationException This will exclude the BacNetObjects with ObjectType=device and considers the rest
     * @implNote Collects all BacNetObjects of the particular device into a Map<Integer,BacNetObjectWrapper>
     */
    private Map<Integer, BacNetObjectWrapper> getUnitObjects(Device device) throws BacNetConfigurationException {
        try {
            Map<Integer, BacNetObjectWrapper> bacNetObjectWrapperMap = new HashMap<>();
            for (BacNetObject object : this.getDeviceObjects(device)) {
                if (!object.getType().getBacNetType().equals(ObjectType.device)) {
                    bacNetObjectWrapperMap.put(object.getId(), new BacNetObjectWrapper(object));
                }
            }
            return bacNetObjectWrapperMap;
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    /**
     * @param unitObjectId Object ID of the device
     * @param objectId     BacNetObject ID for a particular device
     * @return BacNetObjectWrapper
     * @throws BacNetConfigurationException
     * @implNote Returns the caller with an Object of BacNetObjectWrapper with matching objectId, if exists
     */
    public BacNetObjectWrapper readBacNetObject(int unitObjectId, int objectId) throws BacNetConfigurationException {
        try {
            Device device = discoverDevice(unitObjectId);
            Map<Integer, BacNetObjectWrapper> map = this.getUnitObjects(device);
            if (map.keySet().contains(objectId)) {
                return map.get(objectId);
            } else {
                throw new BacNetConfigurationException(Constant.DEVICE_OBJECT_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }

    }

    /**
     * @param unitObjectId Object ID of the device
     * @param objectId     BacNetObject ID for a particular device
     * @return Encodable
     * @throws BacNetConfigurationException
     * @implNote Returns the value for a particular BacNetObject with matching objectId, if exists
     */
    public Encodable getBacNetObjectValue(int unitObjectId, int objectId) throws BacNetConfigurationException {
        try {
            Device device = discoverDevice(unitObjectId);
            Map<Integer, BacNetObjectWrapper> map = this.getUnitObjects(device);
            if (map.keySet().contains(objectId)) {
                return this.getPresentValue(map.get(objectId), new BypassBacnetConverter());
            } else {
                throw new BacNetConfigurationException(Constant.DEVICE_OBJECT_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    /**
     * @param unitObjectId Object ID of the device
     * @param objectId     BacNetObject ID for a particular device
     * @param value        Value to be set, can be of following type- <ul>
     *                     <li>Float- for BacNetObject IDs 1,2,3,4,5,6,7,8,19,22,23,24 </li>
     *                     <li>BinaryPV- for BacNetObject IDs 9,10,11,12,13 </li>
     *                     <li>Integer- for BacNetObject IDs 14,15,16,17,18 </li>
     *                     </ul>
     * @return Object
     * @throws BacNetConfigurationException
     * @implNote Sets the value for a particular BacNetObject with matching objectId, if exists
     * @see <a href="https://github.com/NREL/BACnet/blob/master/src/main/java/com/serotonin/bacnet4j/type/enumerated/BinaryPV.java">BinaryPV Reference</a>
     */
    public Object setBacNetObjectValue(int unitObjectId, int objectId, Object value) throws BacNetConfigurationException {
        try {
            Device device = discoverDevice(unitObjectId);
            Map<Integer, BacNetObjectWrapper> map = this.getUnitObjects(device);
            if (map.keySet().contains(objectId)) {
                if ((objectId >= 1 && objectId <= 8) || (objectId >= 22 && objectId <= 24) || objectId == 19) {
                    java.lang.Float obj = (java.lang.Float) value;
                    this.setPresentValue(map.get(objectId), obj, Real::new);
                } else if ((objectId >= 9 && objectId <= 13)) {
                    BinaryPV obj = (BinaryPV) value;
                    this.setPresentValue(map.get(objectId), obj, object -> object);
                } else {
                    java.lang.Integer obj = (java.lang.Integer) value;
                    this.setPresentValue(map.get(objectId), obj, UnsignedInteger::new);
                }
                return value;
            } else {
                throw new BacNetConfigurationException(Constant.DEVICE_OBJECT_NOT_FOUND);
            }

        } catch (BacNetConfigurationException e) {
            throw new BacNetConfigurationException(e.getMessage());
        } catch (Exception e) {
            throw new BacNetConfigurationException(Constant.UNABLE_TO_WRITE_VALUE, e);
        }
    }

    /**
     * @return Map<Integer, Device>
     * @implNote Returns a Map<Integer,Device> of Indoor Devices collected
     */
    public Map<Integer, Device> getDevices() {
        return devices;
    }

    /**
     * @param unitObjectId Object ID of the device
     * @return Device
     * @throws BacNetConfigurationException
     * @implNote Returns a Device object with matching unitObjectId i.e. Device Object Id
     */
    public Device discoverDevice(int unitObjectId) throws BacNetConfigurationException {
        if (devices.containsKey(unitObjectId)) {
            return devices.get(unitObjectId);
        } else {
            throw new BacNetConfigurationException(Constant.DEVICE_NOT_FOUND);
        }
    }

    /**
     * @throws BacNetConfigurationException
     * @implNote Shuts down the scheduler after all the currently executing jobs are complete, and then closes the BacNetClient resource
     */
    @Override
    public void close() throws BacNetConfigurationException {
        try {
            this.notifier.getScheduler().shutdown(true);
            this.stop();
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }
}