package com.expensetracker.service;

import com.expensetracker.dto.request.UpdateUserRequest;
import com.expensetracker.dto.response.UserResponse;
import com.expensetracker.entity.User;
import com.expensetracker.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

    public UserResponse getCurrentUserProfile() {
        return userMapper.toResponse(securityUtils.getCurrentUser());
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = securityUtils.getCurrentUser();
        user.setName(request.getName());
        return userMapper.toResponse(user);
    }
}
