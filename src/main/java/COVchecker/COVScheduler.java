package COVchecker;

import listener.BacNetVariableListenerWrapper;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import wrapper.BacNetClientConfigurer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class COVScheduler {
    private static Map<String, List<BacNetVariableListenerWrapper>> listenerRegistry = new HashMap<>();
    private BacNetClientConfigurer client;

    public static Map<String, List<BacNetVariableListenerWrapper>> getListenerRegistry() {
        return listenerRegistry;
    }

    public COVScheduler(BacNetClientConfigurer client) {
        this.client = client;
    }

//    public COVScheduler(String broadcastId, int portNo) {
//        this.client = new BacNetClientConfigurer(broadcastId, portNo);
//    }
//
//    public COVScheduler(String broadcastId, int portNo, long timeout) {
//        this.client = new BacNetClientConfigurer(broadcastId, portNo, timeout);
//    }

//    public void createScheduler(int unitObjectId,int objectId,BacNetVariableListenerWrapper bacNetVariableListenerWrapper) {
////        try {
////            for (Map.Entry<String, List<BacNetVariableListenerWrapper>> entry : listenerRegistry.entrySet()) {
////                for (BacNetVariableListenerWrapper bacNetVariableListenerWrapper : entry.getValue()) {
////                    String[] ids = entry.getKey().split("-");
////                    scheduleJob(entry.getKey(), this.client, bacNetVariableListenerWrapper);
////                }
////            }
////        } catch (SchedulerException e) {
////            e.printStackTrace();
////        }
//        try {
//            scheduleJob(unitObjectId,objectId, this.client, bacNetVariableListenerWrapper);
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
//    }

    public JobIdentifier scheduleJob(int unitObjectId, int objectId, BacNetVariableListenerWrapper bacNetVariableListenerWrapper) throws SchedulerException {

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        String name = String.valueOf(Math.random());
        String groupId = unitObjectId + "-" + objectId;
        JobDetail job = JobBuilder.newJob(SampleJob.class)
                .withIdentity(name, groupId)
                .usingJobData("DeviceId", unitObjectId)
                .usingJobData("ObjectId", objectId)
                .build();

        JobDataMap jobDataMap = job.getJobDataMap();
        jobDataMap.put("BacNetListenerWrapper", bacNetVariableListenerWrapper);
        jobDataMap.put("Client", this.client);

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger1", String.valueOf(Math.random()))
                .startAt(new Date())
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(10)
                        .withRepeatCount(20))
                .build();

        scheduler.start();
        scheduler.scheduleJob(job, trigger);
        JobIdentifier jobIdentifier = new JobIdentifier();
        jobIdentifier.setGroupId(groupId);
        jobIdentifier.setName(name);
        return jobIdentifier;
    }


}
