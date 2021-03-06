/*
 * Copyright (c) [2016-2017] [University of Minnesota]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.grouplens.samantha.server.scheduler;

import org.grouplens.samantha.server.config.ConfigKey;
import org.grouplens.samantha.server.exception.ConfigurationException;
import org.quartz.*;
import play.Configuration;
import play.inject.Injector;

import java.text.ParseException;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzSchedulerConfig implements SchedulerConfig {
    private final Configuration config;
    private final Injector injector;
    private final String engineName;

    private QuartzSchedulerConfig(Injector injector, Configuration config, String engineName) {
        this.config = config;
        this.injector = injector;
        this.engineName = engineName;
    }

    public static SchedulerConfig getSchedulerConfig(String engineName,
                                                     Configuration schedulerConfig,
                                                     Injector injector) {
        return new QuartzSchedulerConfig(injector, schedulerConfig, engineName);
    }

    public void scheduleJobs() {
        String cronExprStr = config.getString("cronExpression");
        CronExpression cronExpr;
        try {
            cronExpr = new CronExpression(cronExprStr);
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }
        String name = config.getString(ConfigKey.ENGINE_COMPONENT_NAME.get());
        Trigger trigger = newTrigger().withIdentity(name).withSchedule(cronSchedule(cronExpr)).build();
        Configuration jobConfig = config.getConfig("jobConfig");
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobConfig", jobConfig);
        jobDataMap.put("injector", injector);
        jobDataMap.put("engineName", engineName);
        String jobClass = config.getString("jobClass");
        try {
            JobDetail jobDetail = newJob(Class.forName(jobClass).asSubclass(Job.class))
                    .withIdentity(name).usingJobData(jobDataMap).build();
            QuartzSchedulerService schedulerService = injector.instanceOf(QuartzSchedulerService.class);
            schedulerService.scheduleJob(trigger, jobDetail);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    public void runJobs() {
        String name = config.getString(ConfigKey.ENGINE_COMPONENT_NAME.get());
        QuartzSchedulerService schedulerService = injector.instanceOf(QuartzSchedulerService.class);
        schedulerService.triggerJob(new JobKey(name));
    }
}
