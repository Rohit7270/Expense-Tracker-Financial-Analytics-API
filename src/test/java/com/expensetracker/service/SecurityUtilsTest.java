package com.expensetracker.service;

import com.expensetracker.entity.User;
import com.expensetracker.enums.Role;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUser - When security context is null, throws AccessDeniedException")
    void testGetCurrentUserNullAuth() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> securityUtils.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser - When principal is anonymousUser, throws AccessDeniedException")
    void testGetCurrentUserAnonymous() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertThrows(AccessDeniedException.class, () -> securityUtils.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser - When authentication is not authenticated, throws AccessDeniedException")
    void testGetCurrentUserNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        assertThrows(AccessDeniedException.class, () -> securityUtils.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser - When authenticated and user in database, returns User")
    void testGetCurrentUserSuccess() {
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "pass", Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        User expectedUser = User.builder().id(1L).email("john@example.com").role(Role.USER).build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(expectedUser));

        User actualUser = securityUtils.getCurrentUser();

        assertNotNull(actualUser);
        assertEquals("john@example.com", actualUser.getEmail());
    }

    @Test
    @DisplayName("getCurrentUser - When authenticated but user not in database, throws ResourceNotFoundException")
    void testGetCurrentUserNotFoundInDb() {
        Authentication auth = new UsernamePasswordAuthenticationToken("ghost@example.com", "pass", Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> securityUtils.getCurrentUser());
    }
}
