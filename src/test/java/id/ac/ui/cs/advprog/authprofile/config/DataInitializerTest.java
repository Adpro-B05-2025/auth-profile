package id.ac.ui.cs.advprog.authprofile.config;

import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void initialize_ShouldCreateRoles_WhenRepositoryIsEmpty() {
        // given
        when(roleRepository.count()).thenReturn(0L);

        // when
        dataInitializer.initialize();

        // then
        verify(roleRepository).count();
        verify(roleRepository, times(2)).save(any(Role.class));
    }

    @Test
    void initialize_ShouldNotCreateRoles_WhenRepositoryHasData() {
        // given
        when(roleRepository.count()).thenReturn(2L);

        // when
        dataInitializer.initialize();

        // then
        verify(roleRepository).count();
        verify(roleRepository, never()).save(any(Role.class));
    }
}
