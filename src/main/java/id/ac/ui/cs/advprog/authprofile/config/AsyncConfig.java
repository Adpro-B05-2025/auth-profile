package id.ac.ui.cs.advprog.authprofile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Optimized for high-load search operations
     * - Small core pool to save memory
     * - Large max pool for peak loads
     * - Large queue to handle bursts
     */
    @Bean("searchTaskExecutor")
    public Executor searchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool - keep these threads alive
        executor.setCorePoolSize(10);

        // Max pool - scale up to this under high load
        executor.setMaxPoolSize(50);

        // Queue capacity - buffer requests during spikes
        executor.setQueueCapacity(200);

        // Thread naming for monitoring
        executor.setThreadNamePrefix("Search-");


        // Shutdown handling
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    /**
     * Separate executor for lightweight operations (autocomplete)
     */
    @Bean("autocompleteTaskExecutor")
    public Executor autocompleteTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Smaller pool for cached operations
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Autocomplete-");

        executor.initialize();
        return executor;
    }
}