package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.model.CareGiver;
import id.ac.ui.cs.advprog.authprofile.model.Pacillian;
import id.ac.ui.cs.advprog.authprofile.model.Role;
import id.ac.ui.cs.advprog.authprofile.model.User;
import id.ac.ui.cs.advprog.authprofile.model.WorkingSchedule;
import id.ac.ui.cs.advprog.authprofile.repository.CareGiverRepository;
import id.ac.ui.cs.advprog.authprofile.repository.PacillianRepository;
import id.ac.ui.cs.advprog.authprofile.repository.RoleRepository;
import id.ac.ui.cs.advprog.authprofile.repository.UserRepository;
import id.ac.ui.cs.advprog.authprofile.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PacillianRepository pacillianRepository;

    @Autowired
    private CareGiverRepository careGiverRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

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

    @Transactional
    public String registerPacillian(RegisterPacillianRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException("Error: Email is already in use!");
        }

        if (userRepository.existsByNik(registerRequest.getNik())) {
            throw new id.ac.ui.cs.advprog.authprofile.exception.ResourceNotFoundException("Error: NIK is already in use!");
        }

        // Create new pacillian's account
        Pacillian pacillian = new Pacillian(
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword()),
                registerRequest.getName(),
                registerRequest.getNik(),
                registerRequest.getAddress(),
                registerRequest.getPhoneNumber(),
                registerRequest.getMedicalHistory()
        );

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.ERole.ROLE_PACILLIAN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        pacillian.setRoles(roles);

        pacillianRepository.save(pacillian);

        return "Pacillian registered successfully!";
    }

    @Transactional
    public String registerCareGiver(RegisterCareGiverRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new id.ac.ui.cs.advprog.authprofile.exception.EmailAlreadyExistsException("Error: Email is already in use!");
        }

        if (userRepository.existsByNik(registerRequest.getNik())) {
            throw new id.ac.ui.cs.advprog.authprofile.exception.ResourceNotFoundException("Error: NIK is already in use!");
        }

        // Create new caregiver's account
        CareGiver careGiver = new CareGiver(
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword()),
                registerRequest.getName(),
                registerRequest.getNik(),
                registerRequest.getAddress(),
                registerRequest.getPhoneNumber(),
                registerRequest.getSpeciality(),
                registerRequest.getWorkAddress()
        );

        // Add working schedules if provided
        if (registerRequest.getWorkingSchedules() != null) {
            for (RegisterCareGiverRequest.WorkingScheduleRequest scheduleRequest : registerRequest.getWorkingSchedules()) {
                WorkingSchedule schedule = new WorkingSchedule(
                        scheduleRequest.getDayOfWeek(),
                        scheduleRequest.getStartTime(),
                        scheduleRequest.getEndTime(),
                        careGiver
                );
                careGiver.addWorkingSchedule(schedule);
            }
        }

        Set<Role> roles = new HashSet<>();
        Role doctorRole = roleRepository.findByName(Role.ERole.ROLE_CAREGIVER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(doctorRole);
        careGiver.setRoles(roles);

        careGiverRepository.save(careGiver);

        return "CareGiver registered successfully!";
    }
}