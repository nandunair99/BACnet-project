package covchecker;

import listener.BacNetVariableListenerWrapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import wrapper.BacNetClientConfigurer;


public class ListenerJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            BacNetVariableListenerWrapper bacNetVariableListenerWrapper = (BacNetVariableListenerWrapper) dataMap.get("BacNetListenerWrapper");
            BacNetClientConfigurer client = (BacNetClientConfigurer) dataMap.get("Client");
            int unitObjectId = dataMap.getInt("DeviceId");
            int objectId = dataMap.getInt("ObjectId");

            Object newValue = client.getBacNetObjectValue(unitObjectId, objectId);
            Object oldValue = bacNetVariableListenerWrapper.getOldValue();
            System.out.println("Old Value " + oldValue + " -- " + "New Value " + newValue);
            if (!oldValue.equals(newValue)) {
                bacNetVariableListenerWrapper.getiBacNetVariableListener().onVariableChange(unitObjectId, objectId, newValue);
                bacNetVariableListenerWrapper.setOldValue(newValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

