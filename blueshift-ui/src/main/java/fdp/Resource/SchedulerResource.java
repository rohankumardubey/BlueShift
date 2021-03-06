package fdp.Resource;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fdp.config.TransferScheduleConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by sushil.s
 * Date : 06/09/15
 * Time : 1:14 AM
 */
@Path("/scheduler")
public class SchedulerResource {

    private final Map<String, Scheduler> transferSchedulerMap = new HashMap<String, Scheduler>();

    public SchedulerResource() {
    }

    @Path("/post/check")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String test(String testStr){
        return testStr;
    }

    @Path("/get/check")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String test(){
        return "Bazinga!";
    }

    @Path("/transfer")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void scheduleTransfer(String jsonConfig) throws SchedulerException {
        Gson gson = new GsonBuilder().create();
        TransferScheduleConfig transferScheduleConfig = gson.fromJson(jsonConfig, TransferScheduleConfig.class);
        transferScheduleConfig.initialize();
        if (transferSchedulerMap.containsKey(transferScheduleConfig.getTransferGroupName()))
            scheduleJob(transferScheduleConfig);
        else
            scheduleNewJob(transferScheduleConfig);

    }

    @Path("/transfer/once")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void scheduleTransferOnce(String jsonConfig) throws SchedulerException {
        Gson gson = new GsonBuilder().create();
        TransferScheduleConfig transferScheduleConfig = gson.fromJson(jsonConfig, TransferScheduleConfig.class);
        transferScheduleConfig.initialize();
        if (transferSchedulerMap.containsKey(transferScheduleConfig.getTransferGroupName()))
            scheduleJob(transferScheduleConfig);
        else
            scheduleNewJob(transferScheduleConfig);

    }

    private boolean scheduleNewJob(TransferScheduleConfig config)  {
        Scheduler scheduler = null;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            if( scheduler.checkExists(config.getJobKey()) )
                scheduler.deleteJob(config.getJobKey());
            scheduler.scheduleJob(config.getJobDetail(), config.getTrigger());
            transferSchedulerMap.put(config.getTransferGroupName(), scheduler);
            if( !scheduler.isStarted() )
                scheduler.start();
            return true;
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean scheduleJob(TransferScheduleConfig config) throws SchedulerException {
        Scheduler scheduler = transferSchedulerMap.get(config.getTransferGroupName());
        scheduler.scheduleJob(config.getJobDetail(), config.getTrigger());
        transferSchedulerMap.put(config.getTransferGroupName(), scheduler);
        if (!scheduler.isStarted())
            scheduler.start();
        return true;
    }

    @Path("/transfer/update/cron")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateCronStr(String jsonConfig) throws SchedulerException, ParseException {
        Gson gson = new GsonBuilder().create();
        UpdateCronScheduleBean cronScheduleBean = gson.fromJson(jsonConfig, UpdateCronScheduleBean.class);
        Scheduler scheduler = transferSchedulerMap.get(cronScheduleBean.getNamespace());
        TriggerKey triggerKey = new TriggerKey(cronScheduleBean.getJobName(),cronScheduleBean.getNamespace());
        CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
        trigger.setCronExpression(cronScheduleBean.getCronExprStr());
        scheduler.rescheduleJob(triggerKey, trigger);
    }


    @Path("/status/pretty")
    @GET
    public String schedulerStatusPretty() throws SchedulerException {
        StringBuilder status = new StringBuilder();
        for (Scheduler scheduler : transferSchedulerMap.values()) {
            if (scheduler.isStarted()) {
                status.append("<table align=\"Center\" cellpadding=\"10\" border=\"1\">"
                        + "<tr> "
                        + "<td align=\"center\" ><strong>JOB NAME</strong></td>"
                        + "<td align=\"center\"><strong>GROUP NAME</strong></td>"
                        + "<td align=\"center\"><strong>NEXT TRIGGER TIME</strong></td>"
                        + "<td align=\"center\"><strong>CRON EXPRESSION</strong></td>"
                        + "</tr>");
                for (String groupName : scheduler.getJobGroupNames()) {
                    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                        String jobName = jobKey.getName();
                        String jobGroup = jobKey.getGroup();
                        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                        for (Trigger trigger : triggers) {
                            String cronExpression = null;
                            Date nextFireTime = null;
                            if (trigger instanceof CronTrigger) {
                                CronTrigger cronTrigger = (CronTrigger) trigger;
                                cronExpression = cronTrigger.getCronExpression();
                            }
                            nextFireTime = trigger.getNextFireTime();
                            status.append("<tr>"
                                    + "<td align=\"center\"><a href=\"#\">" + jobName + "</a></td>"
                                    + "<td align=\"center\">" + jobGroup + "</td>"
                                    + "<td align=\"center\">" + nextFireTime + "</td>"
                                    + "<td align=\"center\">" + cronExpression + "</td>"
                                    + "</tr>");
                        }
                    }
                }
                status.append("</table>");
            }
        }
        if (status.length() > 0)
            return status.toString();
        else
            return "<h3 align=\"center\"> Scheduler has not been started</h3>";

    }

    @Path("/status")
    @GET
    public String schedulerStatus() throws SchedulerException {
                        List<String> statusList = null;
        for (Scheduler scheduler : transferSchedulerMap.values()) {
            statusList = new ArrayList<String>();
            if (scheduler.isStarted()) {
                for (String groupName : scheduler.getJobGroupNames()) {
                    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                        String jobName = jobKey.getName();
                        String jobGroup = jobKey.getGroup();
                        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                        for (Trigger trigger : triggers) {
                            String cronExpression = null;
                            Date nextFireTime = null;
                            if (trigger instanceof CronTrigger) {
                                CronTrigger cronTrigger = (CronTrigger) trigger;
                                cronExpression = cronTrigger.getCronExpression();
                            }
                            nextFireTime = trigger.getNextFireTime();
                            SchedulerStatusBean statusBean = new SchedulerStatusBean(jobGroup,jobName,cronExpression,nextFireTime);
                            statusList.add(statusBean.toString());
                        }
                    }
                }
            }
        }
        if (statusList.size() > 0)
            return Arrays.toString(statusList.toArray());
        else
            return "[]";

    }

    @Path("/start/{transferJobGroup}")
    @GET
    public String startJobGroup(@PathParam("transferJobGroup") String jobGroupName) {
        Scheduler scheduler = transferSchedulerMap.get(jobGroupName);
        try {
            scheduler.start();
            return "TRUE";
        } catch (SchedulerException e) {
            e.printStackTrace();
            return "FALSE";
        }
    }

    @Path("/stop/{transferJobGroup}")
    @GET
    public String stopJobGroup(@PathParam("transferJobGroup") String jobGroupName) {
        Scheduler scheduler = transferSchedulerMap.get(jobGroupName);
        try {
            if (scheduler.isStarted()) {
                scheduler.shutdown(true);
                return "TRUE";
            } else
                return "NOT_STARTED";
        } catch (SchedulerException e) {
            e.printStackTrace();
            return "FALSE";
        }
    }

    @Path("/remove/{transferJobGroup}")
    @GET
    public String deleteJobGroup(@PathParam("transferJobGroup") String jobGroupName) {
        if (transferSchedulerMap.containsKey(jobGroupName)) {
            transferSchedulerMap.remove(jobGroupName);
            return "TRUE";
        }
        return "FALSE";
    }


    @Path("/pause/{transferJobGroup}/{transferJobName}")
    @GET
    public String pauseJob(@PathParam("transferJobGroup") String jobGroupName,
                           @PathParam("transferJobName") String jobName) {
        Scheduler scheduler = transferSchedulerMap.get(jobGroupName);
        try {
            JobKey jobKey = new JobKey(jobName,jobGroupName);
            if ( scheduler.checkExists(jobKey) ){
                scheduler.pauseJob(jobKey);
                return "TRUE";
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return "FALSE";
    }

    @Path("/status/{transferJobGroup}/{transferJobName}")
    @GET
    public String jobStatus(@PathParam("transferJobGroup") String jobGroupName,
                           @PathParam("transferJobName") String jobName)  {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("/tmp/bs-"+new JobKey(jobName,jobGroupName).toString()+".log")));
            String line;
            while( (line = reader.readLine()) != null )
                output.append(line);
            return output.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Job has not yet triggered to fetch logs...";
    }


    @Path("/resume/{transferJobGroup}/{transferJobName}")
    @GET
    public String resumeJob(@PathParam("transferJobGroup") String jobGroupName,
                            @PathParam("transferJobName") String jobName) {
        Scheduler scheduler = transferSchedulerMap.get(jobGroupName);
        try {
            JobKey jobKey = new JobKey(jobName,jobGroupName);
            if ( scheduler.checkExists(jobKey) ){
                scheduler.resumeJob(jobKey);
                return "TRUE";
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return "FALSE";
        }
        return "FALSE";
    }

    @Path("/remove/{transferJobGroup}/{transferJobName}")
    @GET
    public String deleteJob(@PathParam("transferJobGroup") String jobGroupName,
                           @PathParam("transferJobName") String jobName) {
        Scheduler scheduler = transferSchedulerMap.get(jobGroupName);
        try {
            JobKey jobKey = new JobKey(jobName,jobGroupName);
            if ( scheduler.checkExists(jobKey) ) {
                scheduler.deleteJob(jobKey);
                return "TRUE";
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return "FALSE";
        }
        return "FALSE";
    }

}

