package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.config.MonitoringConfig;
import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;
import id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException;
import id.ac.ui.cs.advprog.authprofile.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.authprofile.factory.UserFactory;
import id.ac.ui.cs.advprog.authprofile.factory.UserFactoryProvider;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserFactoryProvider factoryProvider;
    private final MonitoringConfig monitoringConfig;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserFactoryProvider factoryProvider,
            MonitoringConfig monitoringConfig) {
        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.factoryProvider = factoryProvider;
        this.monitoringConfig = monitoringConfig;
    }

    @Override
    @Timed(value = "auth_login_duration", description = "Time taken to authenticate user")
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authentication attempt for email: {}", loginRequest.getEmail());

        // Increment login attempts counter
        monitoringConfig.getLoginAttempts().increment();

        Timer.Sample sample = Timer.start(monitoringConfig.meterRegistry);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Extract user ID from userDetails
            Long userId = Long.parseLong(userDetails.getUsername());

            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Increment successful login counter and active sessions
            monitoringConfig.getLoginSuccessful().increment();
            monitoringConfig.getActiveSessions().incrementAndGet();

            logger.info("Authentication successful for user: {}", user.getEmail());

            return new JwtResponse(jwt, user.getId(), user.getEmail(), user.getName(), roles);

        } catch (BadCredentialsException e) {
            // Increment failed login counter
            monitoringConfig.getLoginFailed().increment();
            logger.warn("Authentication failed for email: {} - Bad credentials", loginRequest.getEmail());
            throw e;
        } catch (Exception e) {
            // Increment failed login counter
            monitoringConfig.getLoginFailed().increment();
            logger.error("Authentication failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("auth_login_duration",
                    "status", "completed"));
        }
    }

    @Override
    @Timed(value = "auth_token_validation_duration", description = "Time taken to validate JWT token")
    public TokenValidationResponse validateToken(String token) {
        logger.debug("Validating JWT token");

        try {
            if (jwtUtils.validateJwtToken(token)) {
                String userId = jwtUtils.getUserIdFromJwtToken(token);
                Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());

                    logger.debug("Token validation successful for user: {}", user.getEmail());
                    return new TokenValidationResponse(true, user.getId(), user.getEmail(), roles);
                }
            }
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
        }

        return new TokenValidationResponse(false, null, null, null);
    }

    /**
     * Validates common aspects of registration requests
     */
    private void validateRegistrationRequest(BaseRegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email is already in use!");
        }

        if (userRepository.existsByNik(registerRequest.getNik())) {
            throw new ResourceNotFoundException("Error: NIK is already in use!");
        }
    }

    @Override
    @Transactional
    @Timed(value = "auth_registration_duration", description = "Time taken to register new user")
    public String registerUser(BaseRegisterRequest registerRequest) {
        logger.info("Registration attempt for email: {}", registerRequest.getEmail());

        // Increment registration attempts counter
        monitoringConfig.getRegistrationAttempts().increment();

        Timer.Sample sample = Timer.start(monitoringConfig.meterRegistry);
        String userType = "unknown";

        try {
            validateRegistrationRequest(registerRequest);

            // Get the appropriate factory for this request type
            UserFactory factory = factoryProvider.getFactory(registerRequest);

            // Use the factory to create the user entity
            User user = factory.createUser(registerRequest, encoder.encode(registerRequest.getPassword()));

            String resultMessage;

            // Save the user to the appropriate repository
            if (user instanceof Pacillian) {
                pacillianRepository.save((Pacillian) user);
                resultMessage = "Pacillian registered successfully!";
                userType = "pacillian";
                logger.info("Pacillian registration successful for email: {}", registerRequest.getEmail());
            } else if (user instanceof CareGiver) {
                careGiverRepository.save((CareGiver) user);
                resultMessage = "CareGiver registered successfully!";
                userType = "caregiver";
                logger.info("CareGiver registration successful for email: {}", registerRequest.getEmail());
            } else {
                throw new IllegalArgumentException("Unsupported user type");
            }

            // Record successful registration
            monitoringConfig.meterRegistry.counter("auth_registration_successful",
                    "userType", userType).increment();

            return resultMessage;

        } catch (Exception e) {
            // Record failed registration
            monitoringConfig.meterRegistry.counter("auth_registration_failed",
                    "userType", userType, "reason", e.getClass().getSimpleName()).increment();

            logger.error("Registration failed for email: {} - {}", registerRequest.getEmail(), e.getMessage());
            throw e;
        } finally {
            sample.stop(monitoringConfig.meterRegistry.timer("auth_registration_duration",
                    "userType", userType));
        }
    }
}