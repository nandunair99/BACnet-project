package COVchecker;

import listener.BacNetVariableListenerWrapper;
import listener.IBacNetVariableListener;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import wrapper.BacNetClientConfigurer;

import java.util.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class COVScheduler {

    private static Map<JobIdentifier, List<BacNetVariableListenerWrapper>> listenerRegistry = new HashMap<>();
    private BacNetClientConfigurer client;
    private Scheduler scheduler;

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static Map<JobIdentifier, List<BacNetVariableListenerWrapper>> getListenerRegistry() {
        return listenerRegistry;
    }

    public COVScheduler(BacNetClientConfigurer client) {
        this.client = client;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void addListenerToListenerRegistry(JobIdentifier jobIdentifier, BacNetVariableListenerWrapper bacNetVariableListenerWrapper) {
        Map<JobIdentifier, List<BacNetVariableListenerWrapper>> listenerRegistry = COVScheduler.getListenerRegistry();
        if (listenerRegistry.containsKey(jobIdentifier.toString())) {
            listenerRegistry.get(jobIdentifier.toString()).add(bacNetVariableListenerWrapper);
        } else {
            List<BacNetVariableListenerWrapper> listenerList = new ArrayList<>();
            listenerList.add(bacNetVariableListenerWrapper);
            listenerRegistry.put(jobIdentifier, listenerList);
        }
    }

    public JobIdentifier getListenerJobIdentifier(IBacNetVariableListener iBacNetVariableListener) {
        Map<JobIdentifier, List<BacNetVariableListenerWrapper>> listenerRegistry = COVScheduler.getListenerRegistry();
        for (Iterator<Map.Entry<JobIdentifier, List<BacNetVariableListenerWrapper>>> mapIterator = listenerRegistry.entrySet().iterator(); mapIterator.hasNext(); ) {
            Map.Entry<JobIdentifier, List<BacNetVariableListenerWrapper>> entry = mapIterator.next();
            for (Iterator<BacNetVariableListenerWrapper> listIterator = entry.getValue().iterator(); listIterator.hasNext(); ) {
                BacNetVariableListenerWrapper object = listIterator.next();
                if (object.getiBacNetVariableListener().equals(iBacNetVariableListener)) {
                    listIterator.remove();
                    return entry.getKey();
                }
            }
            if (entry.getValue().size() == 0) {
                mapIterator.remove();
            }
        }
        return null;
    }

    public boolean stopJob(JobIdentifier jobIdentifier) throws SchedulerException {
        return scheduler.deleteJob(new JobKey(jobIdentifier.getName(), jobIdentifier.getGroupId()));
    }

    public JobIdentifier scheduleJob(int unitObjectId, int objectId, BacNetVariableListenerWrapper bacNetVariableListenerWrapper) throws SchedulerException {

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
                        .withRepeatCount(10))
                .build();

        scheduler.start();
        scheduler.scheduleJob(job, trigger);
        JobIdentifier jobIdentifier = new JobIdentifier();
        jobIdentifier.setGroupId(groupId);
        jobIdentifier.setName(name);
        return jobIdentifier;
    }


}
