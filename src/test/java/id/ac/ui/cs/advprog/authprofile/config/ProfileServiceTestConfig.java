package id.ac.ui.cs.advprog.authprofile.config;

import id.ac.ui.cs.advprog.authprofile.service.IProfileService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class ProfileServiceTestConfig {
    @Bean
    @Primary
    public IProfileService profileService() {
        return Mockito.mock(IProfileService.class);
    }
}