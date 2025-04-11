package id.ac.ui.cs.advprog.authprofile.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "id.ac.ui.cs.advprog.authprofile.model")
@EnableJpaRepositories(basePackages = "id.ac.ui.cs.advprog.authprofile.repository")
@EnableTransactionManagement
@Profile("integration-test") // Add this to use only for integration tests
@ComponentScan(
        basePackages = "id.ac.ui.cs.advprog.authprofile",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {AuthTestConfig.class, ProfileServiceTestConfig.class}
        )
)
public class TestContextConfig {
    // This configuration class helps with test context setup
}