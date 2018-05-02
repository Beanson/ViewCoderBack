package viewcoder.helper.job;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Created by Administrator on 2018/4/28.
 */
public class JobImpl {

    private static Logger logger = Logger.getLogger(JobImpl.class);

    static {
        //每天午夜task去update用户的信息数据
        midNightTask();
    }


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


}












