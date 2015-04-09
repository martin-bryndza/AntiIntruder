package eu.bato.anyoffice.backend.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Bato
 */
@Configuration
@PropertySource({"classpath:database.properties"})
@EnableJpaRepositories(basePackages = "eu.bato.anyoffice.backend.dao.*")
@EnableTransactionManagement
public class DataConfig {

    @Autowired
    private Environment env;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("eu.bato.anyoffice.backend.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        emf.setJpaProperties(additionalProperties());

        return emf;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("database.connection.driver_class", "org.apache.derby.jdbc.ClientDriver"));
        dataSource.setUrl(env.getProperty("database.connection.url", "jdbc:derby://localhost:1527/antiintruder_derby"));
        dataSource.setUsername(env.getProperty("database.connection.user", "A"));
        dataSource.setPassword(env.getProperty("database.connection.password", "a"));
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties additionalProperties() {
        return new Properties() {
            {  // Hibernate Specific:
                setProperty("hibernate.hbm2ddl.auto", env.getProperty("database.hibernate.hbm2ddl.auto", "create"));
                setProperty("hibernate.dialect", env.getProperty("database.hibernate.dialect", "org.hibernate.dialect.DerbyDialect"));
                setProperty("hibernate.show_sql", env.getProperty("database.hibernate.show_sql", "false"));
                setProperty("hibernate.format_sql", env.getProperty("database.hibernate.format_sql", "false"));
                setProperty("hibernate.use_sql_comments", env.getProperty("database.hibernate.use_sql_comments", "true"));
            }
        };
    }
}
