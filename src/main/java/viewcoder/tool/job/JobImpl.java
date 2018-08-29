package viewcoder.tool.job;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Created by Administrator on 2018/4/28.
 */
public class JobImpl {

    private static Logger logger = Logger.getLogger(JobImpl.class);

    static {
        //A. 每天午夜task去update用户的信息数据
        //midNightTask();

        //B. 每天提醒即将过期客户信息job
        //expireTask();
    }


    /**
     * 每天晚上更新数据空间等
     */
    private static void midNightTask() {
        try {
            //设置午夜job的task
            JobDetail job = JobBuilder.newJob(MidNightJob.class)
                    .withIdentity("MidNightJob", "version1").build();

            //设置触发器
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("ThreeAMTrigger", "version1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?")) //每日凌晨3点去跑job
                    .build();

            //schedule it
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger); //开始触发run task

        } catch (Exception e) {
            JobImpl.logger.warn("midNightTask failure", e);
        }
    }


    /**
     * 每天提醒即将过期客户信息job
     */
    private static void expireTask() {
        try {
            //设置每天下午3点半job的task
            JobDetail job = JobBuilder.newJob(ExpireJob.class)
                    .withIdentity("ExpireJob", "version1").build();

            //设置触发器
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("HalfPassThreePMTrigger", "version1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 30 15 * * ?")) //每日凌晨3点去跑job
                    .build();

            //schedule it
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger); //开始触发run task

        } catch (Exception e) {
            JobImpl.logger.warn("expireTask failure", e);
        }
    }
}












