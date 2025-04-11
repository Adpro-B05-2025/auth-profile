package id.ac.ui.cs.advprog.authprofile.config;

import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.AuthEntryPointJwt;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import id.ac.ui.cs.advprog.authprofile.security.services.UserDetailsServiceImpl;
import id.ac.ui.cs.advprog.authprofile.service.IAuthService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class AuthTestConfig {

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public PacillianRepository pacillianRepository() {
        return Mockito.mock(PacillianRepository.class);
    }

    @Bean
    public CareGiverRepository careGiverRepository() {
        return Mockito.mock(CareGiverRepository.class);
    }

    @Bean
    public RoleRepository roleRepository() {
        return Mockito.mock(RoleRepository.class);
    }

    @Bean
    public AuthEntryPointJwt unauthorizedHandler() {
        return Mockito.mock(AuthEntryPointJwt.class);
    }

    @Bean
    public JwtUtils jwtUtils() {
        return Mockito.mock(JwtUtils.class);
    }

    @Bean
    public UserDetailsServiceImpl userDetailsService() {
        return Mockito.mock(UserDetailsServiceImpl.class);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public IAuthService authService() {
        return Mockito.mock(IAuthService.class);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());

        return http.build();
    }
}