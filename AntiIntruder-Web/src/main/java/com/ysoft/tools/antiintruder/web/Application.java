package com.ysoft.tools.antiintruder.web;

import com.ysoft.tools.antiintruder.core.scheduler.SchedulerService;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Bato
 */
@ComponentScan(value = "com.ysoft.tools.antiintruder.*")
@EnableAutoConfiguration
@EnableTransactionManagement // to make @Transactional work in services
@ImportResource("classpath:applicationContext.xml")
public class Application extends WebMvcConfigurerAdapter{
        
    @Autowired
    SchedulerService scheduler;
    
    public static void main(String[] args) {
        /*ConfigurableApplicationContext context =*/ SpringApplication.run(Application.class, args);
    }
    
    @PostConstruct
    protected void startScheduler(){
        scheduler.start();
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:lang/messages/messages");
        return messageSource;
    }
    
}
