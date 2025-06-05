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
     * Optimized for t2.large EC2 instance with Docker container limits:
     * - Container: 1GB RAM, 0.5 CPU (half of 2 vCPUs)
     * - Conservative thread counts to prevent CPU thrashing
     * - Optimized for containerized environment
     */
    @Bean("searchTaskExecutor")
    public Executor searchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Reduced core pool for container environment (was 10)
        executor.setCorePoolSize(4);

        // Conservative max pool to prevent CPU oversubscription (was 50)
        executor.setMaxPoolSize(12);

        // Increased queue to handle bursts without creating threads (was 200)
        executor.setQueueCapacity(500);

        // Thread naming for monitoring
        executor.setThreadNamePrefix("Search-");

        // Aggressive core thread timeout to free memory
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(30);

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Rejection policy for overload scenarios
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    /**
     * Lightweight executor for cached autocomplete operations
     * - Very small footprint for cached operations
     * - Quick response for UI interactions
     */
    @Bean("autocompleteTaskExecutor")
    public Executor autocompleteTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Minimal core pool for lightweight operations (was 5)
        executor.setCorePoolSize(2);

        // Small max pool for cached operations (was 15)
        executor.setMaxPoolSize(6);

        // Moderate queue for suggestion requests (was 50)
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("Autocomplete-");

        // Quick timeout for lightweight operations
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(15);

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);

        // Fast-fail for overload (suggestions should be quick)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.AbortPolicy());

        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for rating service integration
     * - Handles external API calls to rating service
     * - Optimized for I/O bound operations
     */
    @Bean("ratingServiceExecutor")
    public Executor ratingServiceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Small pool for external service calls
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);

        executor.setThreadNamePrefix("Rating-");

        // Longer timeout for external service calls
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);

        // Retry strategy - caller runs to prevent data loss
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}