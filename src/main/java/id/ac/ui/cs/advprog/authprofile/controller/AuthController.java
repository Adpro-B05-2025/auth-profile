package id.ac.ui.cs.advprog.authprofile.controller;

import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.MessageResponse;
import id.ac.ui.cs.advprog.authprofile.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    @Autowired
    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register/pacillian")
    public ResponseEntity<MessageResponse> registerPacillian(@Valid @RequestBody RegisterPacillianRequest registerRequest) {
        String message = authService.registerPacillian(registerRequest);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/register/caregiver")
    public ResponseEntity<MessageResponse> registerCareGiver(@Valid @RequestBody RegisterCareGiverRequest registerRequest) {
        String message = authService.registerCareGiver(registerRequest);
        return ResponseEntity.ok(new MessageResponse(message));
    }
}