package me.yuge.springwebflux.core.configuration;

import com.google.common.io.Resources;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class FreeMarkerConfiguration {
    @Bean
    public Configuration freeMarkerConfiguration() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);

        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        configuration.setDirectoryForTemplateLoading(new File(Resources.getResource("templates").getFile()));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        configuration.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        configuration.setLogTemplateExceptions(false);

        // Wrap unchecked exceptions thrown during template processing into TemplateException-s.
        configuration.setWrapUncheckedExceptions(true);

        return configuration;
    }
}
