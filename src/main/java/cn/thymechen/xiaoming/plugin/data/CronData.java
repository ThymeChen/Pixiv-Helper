package cn.thymechen.xiaoming.plugin.data;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.ZonedDateTime;

public class CronData {
    Cron cron;
    ExecutionTime executionTime;
    ZonedDateTime nextExecution;

    public void parseCron(String expression) {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                    .supportsHash().supportsL().supportsW().and()
                .withMonth().and()
                .withDayOfWeek()
                    .withIntMapping(7, 0) // non-standard non-zero numbers
                    .supportsHash().supportsL().supportsW().and()
                .instance();

        CronParser parser = new CronParser(cronDefinition);
        this.cron = parser.parse(expression);
        this.cron.validate();

        executionTime = ExecutionTime.forCron(this.cron);
        nextExecution = executionTime.nextExecution(ZonedDateTime.now()).get();
    }

    public boolean shouldInvoke() {
        ZonedDateTime time = ZonedDateTime.now();
        if (nextExecution.isBefore(time)) {
            nextExecution = executionTime.nextExecution(time).get();
            return true;
        } else {
            return false;
        }
    }
}
