package id.ac.ui.cs.advprog.authprofile.factory;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFactoryProviderTest {

    @Mock
    private PacillianFactory pacillianFactory;

    @Mock
    private CareGiverFactory careGiverFactory;

    private UserFactoryProvider factoryProvider;
    private RegisterPacillianRequest pacillianRequest;
    private RegisterCareGiverRequest careGiverRequest;

    @BeforeEach
    void setUp() {
        // Set up factory mocks to return their supported types
        Set<Class<? extends BaseRegisterRequest>> pacillianTypes = new HashSet<>();
        pacillianTypes.add(RegisterPacillianRequest.class);
        when(pacillianFactory.getSupportedRequestTypes()).thenReturn(pacillianTypes);

        Set<Class<? extends BaseRegisterRequest>> careGiverTypes = new HashSet<>();
        careGiverTypes.add(RegisterCareGiverRequest.class);
        when(careGiverFactory.getSupportedRequestTypes()).thenReturn(careGiverTypes);

        // Create factory provider with list of mocked factories
        List<UserFactory> factories = Arrays.asList(pacillianFactory, careGiverFactory);
        factoryProvider = new UserFactoryProvider(factories);

        // Set up pacillian request
        pacillianRequest = new RegisterPacillianRequest();
        pacillianRequest.setEmail("pacillian@example.com");
        pacillianRequest.setPassword("password");
        pacillianRequest.setName("Test Pacillian");
        pacillianRequest.setNik("1234567890123456");
        pacillianRequest.setAddress("Test Address");
        pacillianRequest.setPhoneNumber("081234567890");
        pacillianRequest.setMedicalHistory("No significant history");


        // Set up caregiver request
        careGiverRequest = new RegisterCareGiverRequest();
        careGiverRequest.setEmail("caregiver@example.com");
        careGiverRequest.setPassword("password");
        careGiverRequest.setName("Dr. Test");
        careGiverRequest.setNik("6543210987654321");
        careGiverRequest.setAddress("Doctor Address");
        careGiverRequest.setPhoneNumber("089876543210");
        careGiverRequest.setSpeciality("General");
        careGiverRequest.setWorkAddress("Test Hospital");
    }

    @Test
    void getFactory_WithPacillianRequest_ShouldReturnPacillianFactory() {
        // when
        UserFactory factory = factoryProvider.getFactory(pacillianRequest);

        // then
        assertThat(factory).isEqualTo(pacillianFactory);
    }

    @Test
    void getFactory_WithCareGiverRequest_ShouldReturnCareGiverFactory() {
        // when
        UserFactory factory = factoryProvider.getFactory(careGiverRequest);

        // then
        assertThat(factory).isEqualTo(careGiverFactory);
    }

    @Test
    void getFactory_WithNullRequest_ShouldThrowException() {
        // when/then
        assertThatThrownBy(() -> factoryProvider.getFactory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Register request cannot be null");
    }

    @Test
    void getFactory_WithUnsupportedRequestType_ShouldThrowException() {
        // Create a mock unsupported request
        BaseRegisterRequest unsupportedRequest = new BaseRegisterRequest() {
            // Anonymous inner class that extends BaseRegisterRequest
        };

        // when/then
        assertThatThrownBy(() -> factoryProvider.getFactory(unsupportedRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported registration request type");
    }

    @Test
    void construction_WithNoFactories_ShouldCreateEmptyRegistry() {
        // when
        UserFactoryProvider emptyProvider = new UserFactoryProvider(Collections.emptyList());

        // then - should not throw an exception, but any request should be unsupported
        assertThatThrownBy(() -> emptyProvider.getFactory(pacillianRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported registration request type");
    }
}