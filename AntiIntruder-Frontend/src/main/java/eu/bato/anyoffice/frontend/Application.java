package eu.bato.anyoffice.frontend;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.bato.anyoffice.core.person.PersonStateManager;
import eu.bato.anyoffice.core.scheduler.SchedulerService;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Bato
 */
@SpringBootApplication // adds @Configuration, @EnableAutoConfiguration, @ComponentScan
//@EnableWebMvc
@EnableAutoConfiguration
public class Application extends WebMvcConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    SchedulerService scheduler;

    @Autowired
    PersonStateManager personStateManager;

    public static void main(String[] args) {
        log.info("Any Office web aplication is starting...");
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    protected void startScheduler() {
        personStateManager.updateHipChatStatuses();
        scheduler.start();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Autowired
    private MessageSource messageSource;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converters.add(converter);
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource);
        return validatorFactoryBean;
    }

    @Override
    public Validator getValidator() {
        return validator();
    }

//    @Bean
//    public ITemplateResolver templateResolver(){
//        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
//        return resolver;
//    }
//
//    @Bean
//    public SpringTemplateEngine templateEngine() {
//        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.addTemplateResolver(templateResolver());
////        templateEngine.addDialect(new SpringSecurityDialect());
//        templateEngine.addDialect(new LayoutDialect());
//        return templateEngine;
//    }
}
