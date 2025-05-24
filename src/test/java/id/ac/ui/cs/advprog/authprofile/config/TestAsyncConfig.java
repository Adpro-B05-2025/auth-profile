package id.ac.ui.cs.advprog.authprofile.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Test configuration for async operations
 * This ensures that async methods complete synchronously during testing
 */
@TestConfiguration
@EnableAsync
public class TestAsyncConfig {

    @Bean
    @Primary
    public Executor testTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("test-async-");
        executor.initialize();
        return executor;
    }

    @Bean("searchTaskExecutor")
    public Executor searchTaskExecutor() {
        return testTaskExecutor();
    }

    @Bean("autocompleteTaskExecutor")
    public Executor autocompleteTaskExecutor() {
        return testTaskExecutor();
    }
}