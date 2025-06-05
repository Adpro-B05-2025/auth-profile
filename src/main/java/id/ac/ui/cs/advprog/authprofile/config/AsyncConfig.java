package id.ac.ui.cs.advprog.authprofile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private ThreadPoolTaskExecutor createExecutor(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            String threadNamePrefix,
            int keepAliveSeconds,
            int awaitTerminationSeconds,
            RejectedExecutionHandler rejectionPolicy) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setRejectedExecutionHandler(rejectionPolicy);
        executor.initialize();
        return executor;
    }

    /**
     * Optimized for t2.large EC2 instance with Docker container limits:
     * - Container: 1GB RAM, 0.5 CPU (half of 2 vCPUs)
     * - Conservative thread counts to prevent CPU thrashing
     * - Optimized for containerized environment
     */
    @Bean("searchTaskExecutor")
    public Executor searchTaskExecutor() {
        return createExecutor(
                4,   // corePoolSize (was 10)
                12,  // maxPoolSize (was 50)
                500, // queueCapacity (was 200)
                "Search-",
                30,  // keepAliveSeconds
                30,  // awaitTerminationSeconds
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * Lightweight executor for cached autocomplete operations
     * - Very small footprint for cached operations
     * - Quick response for UI interactions
     */
    @Bean("autocompleteTaskExecutor")
    public Executor autocompleteTaskExecutor() {
        return createExecutor(
                2,   // corePoolSize (was 5)
                6,   // maxPoolSize (was 15)
                100, // queueCapacity (was 50)
                "Autocomplete-",
                15,  // keepAliveSeconds
                15,  // awaitTerminationSeconds
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * Dedicated executor for rating service integration
     * - Handles external API calls to rating service
     * - Optimized for I/O bound operations
     */
    @Bean("ratingServiceExecutor")
    public Executor ratingServiceExecutor() {
        return createExecutor(
                2,   // corePoolSize
                8,   // maxPoolSize
                200, // queueCapacity
                "Rating-",
                60,  // keepAliveSeconds
                45,  // awaitTerminationSeconds
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}