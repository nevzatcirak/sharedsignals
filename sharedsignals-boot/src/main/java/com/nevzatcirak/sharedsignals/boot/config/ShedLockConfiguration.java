package com.nevzatcirak.sharedsignals.boot.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration for Distributed Scheduling Locking using ShedLock.
 * <p>
 * Ensures that scheduled tasks are executed only once at a time in a cluster.
 * defaultLockAtMostFor: Failsafe. If a node dies, lock is released after this time (10 min).
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfiguration {

    /**
     * Configures the LockProvider using JDBC Template.
     *
     * @param dataSource the Spring data source
     * @return the lock provider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName("ssf_shedlock")
                        .build()
        );
    }
}