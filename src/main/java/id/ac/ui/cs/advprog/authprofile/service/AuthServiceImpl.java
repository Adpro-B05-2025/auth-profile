package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
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
import id.ac.ui.cs.advprog.authprofile.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PacillianRepository pacillianRepository;
    private final CareGiverRepository careGiverRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserFactoryProvider factoryProvider;

    @Autowired
    public AuthServiceImpl(
            UserRepository userRepository,
            PacillianRepository pacillianRepository,
            CareGiverRepository careGiverRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserFactoryProvider factoryProvider) {
        this.userRepository = userRepository;
        this.pacillianRepository = pacillianRepository;
        this.careGiverRepository = careGiverRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.factoryProvider = factoryProvider;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new JwtResponse(jwt, user.getId(), user.getEmail(), user.getName(), roles);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        try {
            // Validate JWT token
            boolean isValid = jwtUtils.validateJwtToken(token);
            if (!isValid) {
                return new TokenValidationResponse(false, null, null, null);
            }

            // Extract username from token
            String username = jwtUtils.getUserNameFromJwtToken(token);

            // Find the user
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

            // Extract roles
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());

            // Return validation response
            return new TokenValidationResponse(true, user.getId(), username, roles);

        } catch (Exception e) {
            return new TokenValidationResponse(false, null, null, null);
        }
    }

    @Override
    public JwtResponse regenerateToken(User user) {
        // Skip authentication and directly generate a token
        String jwt = jwtUtils.generateJwtTokenFromUsername(user.getEmail());

        // Get user roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        // Return JWT response
        return new JwtResponse(jwt, user.getId(), user.getEmail(), user.getName(), roles);
    }

    /**
     * Validates common aspects of registration requests
     * @param registerRequest the request to validate
     */
    private void validateRegistrationRequest(id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email is already in use!");
        }

        if (userRepository.existsByNik(registerRequest.getNik())) {
            throw new ResourceNotFoundException("Error: NIK is already in use!");
        }
    }

    @Override
    @Transactional
    public String registerUser(BaseRegisterRequest registerRequest) {
        validateRegistrationRequest(registerRequest);

        // Get the appropriate factory for this request type
        UserFactory factory = factoryProvider.getFactory(registerRequest);

        // Use the factory to create the user entity
        User user = factory.createUser(registerRequest, encoder.encode(registerRequest.getPassword()));

        // Save the user to the appropriate repository
        if (user instanceof Pacillian) {
            pacillianRepository.save((Pacillian) user);
            return "Pacillian registered successfully!";
        } else if (user instanceof CareGiver) {
            careGiverRepository.save((CareGiver) user);
            return "CareGiver registered successfully!";
        }

        throw new IllegalArgumentException("Unsupported user type");
    }
}