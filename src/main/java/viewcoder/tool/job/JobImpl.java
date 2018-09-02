package viewcoder.tool.job;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Administrator on 2018/4/28.
 */
public class JobImpl {

    private static Logger logger = LoggerFactory.getLogger(JobImpl.class);

    //实例化后即刻run task
    public JobImpl() {

        //A. 每天午夜task去update用户的信息数据
        midNightTask();

        //B. 每天提醒即将过期客户信息job
        expireTask();
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
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 20 19 * * ?")) //每日凌晨3点去跑资源空间释放的job   0 0 3 * * ?
                    .build();

            //schedule it
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger); //开始触发run task
            JobImpl.logger.debug("Run midNightTask");

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
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 15 19 * * ?")) //每天下午10点跑提醒过期客户的job   0 0 10 * * ?
                    .build();

            //schedule it
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger); //开始触发run task
            JobImpl.logger.debug("Run expireTask");

        } catch (Exception e) {
            JobImpl.logger.warn("expireTask failure", e);
        }
    }
}












