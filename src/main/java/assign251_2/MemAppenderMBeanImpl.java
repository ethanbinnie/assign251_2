
package assign251_2;

import java.util.ArrayList;
import java.util.List;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class MemAppenderMBeanImpl
        extends StandardMBean
        implements MemAppenderMBean {

    private final MemAppender appender;

    public MemAppenderMBeanImpl(MemAppender appender)
            throws NotCompliantMBeanException {
        super(MemAppenderMBean.class, false);
        this.appender = appender;
    }

    @Override
    public String[] getLogMessages() {
        List<String> messages = new ArrayList<>();

        for (org.apache.logging.log4j.core.LogEvent event
                : appender.getCurrentLogs()) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        return messages.toArray(new String[0]);
    }

    @Override
    public long getEstimatedCachedCharacters() {
        long total = 0;

        for (String message : getLogMessages()) {
            total += message.length();
        }

        return total;
    }

    @Override
    public long getDiscardedLogCount() {
        return appender.getDiscardedLogCount();
    }
}
