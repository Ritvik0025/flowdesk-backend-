package com.flowdesk.flowdesk_backend.service;

import com.flowdesk.flowdesk_backend.config.JwtUtil;
import com.flowdesk.flowdesk_backend.dto.AuthResponse;
import com.flowdesk.flowdesk_backend.dto.LoginRequest;
import com.flowdesk.flowdesk_backend.dto.RegisterRequest;
import com.flowdesk.flowdesk_backend.model.User;
import com.flowdesk.flowdesk_backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisService redisService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RedisService redisService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.redisService = redisService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        // Save token in Redis for 24 hours
        redisService.save("token:" + user.getEmail(), token, 1440);

        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        // Save token in Redis for 24 hours
        redisService.save("token:" + user.getEmail(), token, 1440);

        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    public void logout(String email) {
        // Delete token from Redis on logout
        redisService.delete("token:" + email);
    }
}