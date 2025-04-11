package id.ac.ui.cs.advprog.authprofile.security;

import id.ac.ui.cs.advprog.authprofile.security.jwt.AuthTokenFilter;
import id.ac.ui.cs.advprog.authprofile.security.jwt.AuthEntryPointJwt;
import id.ac.ui.cs.advprog.authprofile.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WebSecurityConfigTest {

    private WebSecurityConfig webSecurityConfig;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthEntryPointJwt unauthorizedHandler;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig();
        // Set the mocked dependencies (assuming you changed the access modifiers)
        webSecurityConfig.userDetailsService = userDetailsService;
        webSecurityConfig.unauthorizedHandler = unauthorizedHandler;
    }

    @Test
    void passwordEncoderShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = webSecurityConfig.passwordEncoder();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void authenticationJwtTokenFilterShouldReturnAuthTokenFilter() {
        AuthTokenFilter filter = webSecurityConfig.authenticationJwtTokenFilter();
        assertThat(filter).isNotNull();
        assertThat(filter).isInstanceOf(AuthTokenFilter.class);
    }

    @Test
    void authenticationProviderShouldNotBeNull() {
        // When
        DaoAuthenticationProvider provider = webSecurityConfig.authenticationProvider();

        // Then
        assertThat(provider).isNotNull();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }
}