package eu.bato.anyoffice.frontend.config;

import eu.bato.anyoffice.frontend.Application;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author bryndza
 */
@Configuration
public class WebConfiguration extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
