package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;

public interface IAuthService {

    /**
     * Authenticates a user with the given login credentials
     * @param loginRequest the login credentials
     * @return JWT response containing token and user details
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);

    /**
     * Registers a new Pacillian (patient) user
     * @param registerRequest the registration details
     * @return success message
     */
    String registerPacillian(RegisterPacillianRequest registerRequest);

    /**
     * Registers a new CareGiver (doctor) user
     * @param registerRequest the registration details
     * @return success message
     */
    String registerCareGiver(RegisterCareGiverRequest registerRequest);

    /**
     * Validates a JWT token and returns user information if valid
     * @param token the JWT token to validate
     * @return token validation response with user details if valid
     */
    TokenValidationResponse validateToken(String token);
}