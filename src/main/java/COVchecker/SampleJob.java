package COVchecker;

import CustomException.BacNetConfigurationException;
import listener.BacNetVariableListenerWrapper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import wrapper.BacNetClientConfigurer;


public class SampleJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        try {

            BacNetVariableListenerWrapper bacNetVariableListenerWrapper = (BacNetVariableListenerWrapper) dataMap.get("BacNetListenerWrapper");
            BacNetClientConfigurer client = (BacNetClientConfigurer) dataMap.get("Client");
            int unitObjectId = dataMap.getInt("DeviceId");
            int objectId = dataMap.getInt("ObjectId");

            Object newValue = client.getBacNetObjectValue(unitObjectId, objectId);
            Object oldValue = bacNetVariableListenerWrapper.getOldValue();
            System.out.println(oldValue + "--" + newValue);
            if (!oldValue.equals(newValue)) {
                bacNetVariableListenerWrapper.getiBacNetVariableListener().onVariableChange(unitObjectId, objectId, newValue);
                bacNetVariableListenerWrapper.setOldValue(newValue);
            }

        } catch (BacNetConfigurationException e) {
            e.printStackTrace();
        }


    }
}

