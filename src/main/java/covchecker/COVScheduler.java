package covchecker;

import bacnetexception.BacNetConfigurationException;
import covchecker.schedulerexception.COVSchedulerException;
import listener.BacNetVariableListenerWrapper;
import listener.IBacNetVariableListener;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import wrapper.BacNetClientConfigurer;

import java.util.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class COVScheduler {

    /**
     * Listener registry holds list of listeners active, in a Map<JobIdentifier, List<BacNetVariableListenerWrapper> structure
     */
    private static Map<JobIdentifier, List<BacNetVariableListenerWrapper>> listenerRegistry = new HashMap<>();
    private BacNetClientConfigurer client;
    private Scheduler scheduler;

    /**
     * @param client BACnetClient Object to start listener on
     * @throws SchedulerException
     */
    public COVScheduler(BacNetClientConfigurer client) throws BacNetConfigurationException {
        try {
            this.client = client;
            scheduler = new StdSchedulerFactory().getScheduler();
        } catch (Exception e) {
            throw new BacNetConfigurationException(e);
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private static Map<JobIdentifier, List<BacNetVariableListenerWrapper>> getListenerRegistry() {
        return listenerRegistry;
    }

    /**
     * @param jobIdentifier                 holds Job Name and group Id
     * @param bacNetVariableListenerWrapper
     * @throws COVSchedulerException
     * @implNote Adds a listener to the Listener Registry Map
     */
    public void addListenerToListenerRegistry(JobIdentifier jobIdentifier, BacNetVariableListenerWrapper bacNetVariableListenerWrapper) throws COVSchedulerException {
        try {
            Map<JobIdentifier, List<BacNetVariableListenerWrapper>> listenerRegistry = COVScheduler.getListenerRegistry();
            if (listenerRegistry.containsKey(jobIdentifier)) {
                listenerRegistry.get(jobIdentifier).add(bacNetVariableListenerWrapper);
            } else {
                List<BacNetVariableListenerWrapper> listenerList = new ArrayList<>();
                listenerList.add(bacNetVariableListenerWrapper);
                listenerRegistry.put(jobIdentifier, listenerList);
            }
        } catch (Exception e) {
            throw new COVSchedulerException(e);
        }
    }

    /**
     * @param iBacNetVariableListener IbacNetVariable listener instance
     * @return JobIdentifier Job identifier having Job name and Group Id
     * @throws COVSchedulerException
     * @implNote Iterates through the map to search for the listener and returns the respective Job identifier
     */
    public JobIdentifier getListenerJobIdentifier(IBacNetVariableListener iBacNetVariableListener) throws COVSchedulerException {
        try {
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
                if (entry.getValue().isEmpty()) {
                    mapIterator.remove();
                }
            }
            return null;
        } catch (Exception e) {
            throw new COVSchedulerException(e);
        }
    }

    /**
     * @param jobIdentifier Job identifier having Job name and Group Id
     * @return boolean true if Job is found and deleted, else return false
     * @throws COVSchedulerException
     */
    public boolean stopJob(JobIdentifier jobIdentifier) throws COVSchedulerException {
        try {
            return scheduler.deleteJob(new JobKey(jobIdentifier.getName(), jobIdentifier.getGroupId()));
        } catch (Exception e) {
            throw new COVSchedulerException(e);
        }
    }

    /**
     * @param unitObjectId                  Object ID of the device
     * @param objectId                      BacNetObject ID for a particular device
     * @param bacNetVariableListenerWrapper Wrapper to IBacNetListener thats holds Old value and New Value of the BacNet Object with the listener
     * @return JobIdentifier Job identifier having Job name and Group Id
     * @throws COVSchedulerException
     */
    public JobIdentifier scheduleJob(int unitObjectId, int objectId, BacNetVariableListenerWrapper bacNetVariableListenerWrapper) throws COVSchedulerException {
        try {
            String name = String.valueOf(Math.random());
            String groupId = unitObjectId + "-" + objectId;
            JobDetail job = JobBuilder.newJob(ListenerJob.class)
                    .withIdentity(name, groupId)
                    .usingJobData("DeviceId", unitObjectId)
                    .usingJobData("ObjectId", objectId)
                    .build();

            JobDataMap jobDataMap = job.getJobDataMap();
            jobDataMap.put("BacNetListenerWrapper", bacNetVariableListenerWrapper);
            jobDataMap.put("Client", this.client);

            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger", String.valueOf(Math.random()))
                    .startAt(new Date())
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            scheduler.start();
            scheduler.scheduleJob(job, trigger);
            JobIdentifier jobIdentifier = new JobIdentifier();
            jobIdentifier.setGroupId(groupId);
            jobIdentifier.setName(name);
            return jobIdentifier;
        } catch (Exception e) {
            throw new COVSchedulerException(e);
        }
    }


}
