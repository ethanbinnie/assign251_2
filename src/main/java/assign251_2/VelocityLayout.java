package assign251_2;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityLayout extends AbstractStringLayout {

    private String template;
    private final VelocityEngine velocityEngine;

    public VelocityLayout(String template) {
        super(StandardCharsets.UTF_8);

        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }

        this.template = template;
        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.init();
    }

    public void setPattern(String template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }

        this.template = template;
    }

    public String getPattern() {
        return template;
    }

    @Override
    public String toSerializable(LogEvent event) {
        VelocityContext context = new VelocityContext();

        context.put("c", event.getLoggerName());
        context.put("d", new Date(event.getTimeMillis()).toString());
        context.put("m", event.getMessage().getFormattedMessage());
        context.put("p", event.getLevel().toString());
        context.put("t", event.getThreadName());
        context.put("n", System.lineSeparator());

        java.io.StringWriter writer = new java.io.StringWriter();
        velocityEngine.evaluate(
                context,
                writer,
                "VelocityLayout",
                template
        );

        return writer.toString();
    }
}