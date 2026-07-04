package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.*;
import com.enterprise.expensetracker.entity.ERole;
import com.enterprise.expensetracker.entity.Role;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.BadRequestException;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.RoleRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import com.enterprise.expensetracker.security.JwtService;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ActivityLogService activityLogService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role ROLE_USER is not found."));
        roles.add(userRole);

        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .currency(currency)
                .roles(roles)
                .build();

        userRepository.save(user);
        activityLogService.log("USER_REGISTERED", "Registered new user: " + user.getUsername(), user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        activityLogService.log("USER_LOGIN", "User logged in: " + user.getUsername(), user);

        return new AuthResponse(jwt, refreshToken, userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    @Transactional
    public String initiateForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + request.getEmail()));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        activityLogService.log("PASSWORD_RESET_REQUESTED", "Requested password reset for: " + user.getUsername(), user);
        
        // In real apps, send token via email. Here, we return it to the client for testing ease.
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        activityLogService.log("PASSWORD_RESET_SUCCESS", "Successfully reset password for: " + user.getUsername(), user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        activityLogService.log("PASSWORD_CHANGED", "Password changed for: " + user.getUsername(), user);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setCurrency(request.getCurrency());
        userRepository.save(user);

        activityLogService.log("PROFILE_UPDATED", "Profile updated for: " + user.getUsername(), user);

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .currency(user.getCurrency())
                .build();
    }

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .currency(user.getCurrency())
                .build();
    }
}
