package edu.cpp.campusapps.FeedsAggregator.config;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import edu.cpp.campusapps.FeedsAggregator.CacheJob;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfiguration {

    @Value("${caching-interval-mins:60}")
    private int cachingIntervalMins;

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob()
                .ofType(CacheJob.class)
                .storeDurably()
                .withIdentity("Cache Job Detail")
                .build();
    }

    @Bean
    public Trigger trigger(JobDetail job) {
        return TriggerBuilder.newTrigger()
                .forJob(job)
                .withIdentity("Cache Job Trigger")
                .withSchedule(
                        simpleSchedule()
                                .repeatForever()
                                .withIntervalInMinutes(this.cachingIntervalMins))
                .build();
    }
}
