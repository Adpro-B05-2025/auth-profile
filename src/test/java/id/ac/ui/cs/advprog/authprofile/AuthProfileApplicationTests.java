package id.ac.ui.cs.advprog.authprofile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@ActiveProfiles("test") // This will use application-test.properties
class AuthProfileApplicationTests {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot be loaded
        // We don't need any assertions here as the test will fail if Spring context fails to load
    }

    @Test
    void mainMethodShouldCallSpringApplicationRun() {
        // Mock the SpringApplication class to avoid actually starting the application
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            // Set up the mock to do nothing when run is called
            mocked.when(() -> SpringApplication.run(
                            eq(AuthProfileApplication.class),
                            any(String[].class)))
                    .thenReturn(null);

            // Call the main method
            AuthProfileApplication.main(new String[]{});

            // Verify that SpringApplication.run was called with the correct parameters
            mocked.verify(() -> SpringApplication.run(
                            eq(AuthProfileApplication.class),
                            any(String[].class)),
                    Mockito.times(1));
        }
    }
}