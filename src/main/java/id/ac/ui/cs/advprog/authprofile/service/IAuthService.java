package id.ac.ui.cs.advprog.authprofile.service;

import id.ac.ui.cs.advprog.authprofile.dto.request.BaseRegisterRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterCareGiverRequest;
import id.ac.ui.cs.advprog.authprofile.dto.request.RegisterPacillianRequest;
import id.ac.ui.cs.advprog.authprofile.dto.response.JwtResponse;
import id.ac.ui.cs.advprog.authprofile.dto.response.TokenValidationResponse;
import id.ac.ui.cs.advprog.authprofile.model.User;

public interface IAuthService {

    /**
     * Authenticates a user with the given login credentials
     * @param loginRequest the login credentials
     * @return JWT response containing token and user details
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);



    /**
     * Validates a JWT token and returns user information if valid
     * @param token the JWT token to validate
     * @return token validation response with user details if valid
     */
    TokenValidationResponse validateToken(String token);

    /**
     * Regenerates a JWT token for a user after a profile update (like email change)
     * @param user the updated user
     * @return JWT response containing the new token and user details
     */
    JwtResponse regenerateToken(User user);

    String registerUser(BaseRegisterRequest registerRequest);

}