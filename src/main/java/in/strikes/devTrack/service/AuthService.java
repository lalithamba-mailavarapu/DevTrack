
package in.strikes.devTrack.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.strikes.devTrack.dto.ChangePasswordRequest;
import in.strikes.devTrack.dto.CurrentUserResponse;
import in.strikes.devTrack.dto.LoginRequest;
import in.strikes.devTrack.dto.LoginResponse;
import in.strikes.devTrack.dto.RegisterRequest;
import in.strikes.devTrack.dto.RegisterResponse;
import in.strikes.devTrack.dto.UpdateProfileRequest;
import in.strikes.devTrack.entity.User;
import in.strikes.devTrack.repository.UserRepository;


@Service
public class AuthService {

    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "Registration successful"
        );
    }

    
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }

    public CurrentUserResponse getCurrentUser(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    return new CurrentUserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt()
    );
}


@Transactional
public CurrentUserResponse updateCurrentUser(
        String email,
        UpdateProfileRequest request) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    String username = request.getUsername().trim();

    if (userRepository.existsByUsernameAndIdNot(
            username, user.getId())) {
        throw new IllegalArgumentException("Username already exists");
    }

    user.setUsername(username);

    User updatedUser = userRepository.save(user);

    return new CurrentUserResponse(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getCreatedAt()
    );
}

@Transactional
public void changePassword(
        String email,
        ChangePasswordRequest request) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    // Verify the current password
    if (!passwordEncoder.matches(
            request.getCurrentPassword(),
            user.getPasswordHash())) {

        throw new IllegalArgumentException("Current password is incorrect");
    }

    // Prevent changing to the same password
    if (passwordEncoder.matches(
            request.getNewPassword(),
            user.getPasswordHash())) {

        throw new IllegalArgumentException(
                "New password must be different from the current password");
    }

    // Encode and save the new password
    user.setPasswordHash(
            passwordEncoder.encode(request.getNewPassword()));

    userRepository.save(user);
}

}
