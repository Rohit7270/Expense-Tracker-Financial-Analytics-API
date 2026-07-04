package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.ChangePasswordRequest;
import com.enterprise.expensetracker.dto.MessageResponse;
import com.enterprise.expensetracker.dto.ProfileRequest;
import com.enterprise.expensetracker.dto.ProfileResponse;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProfileResponse profile = authService.getProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ProfileRequest request
    ) {
        ProfileResponse updatedProfile = authService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
    }
}
