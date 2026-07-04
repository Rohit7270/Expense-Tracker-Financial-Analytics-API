package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.entity.Category;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.exception.BadRequestException;
import com.enterprise.expensetracker.exception.ResourceNotFoundException;
import com.enterprise.expensetracker.repository.CategoryRepository;
import com.enterprise.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogService activityLogService;

    public List<CategoryDto> getCategoriesForUser(Long userId) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIsNull(userId);
        return categories.stream().map(c -> CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(Long userId, CategoryDto categoryDto) {
        if (categoryRepository.existsByNameIgnoreCaseAndUserIsNull(categoryDto.getName()) ||
                categoryRepository.existsByNameIgnoreCaseAndUserId(categoryDto.getName(), userId)) {
            throw new BadRequestException("Category with this name already exists!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = Category.builder()
                .name(categoryDto.getName())
                .type(categoryDto.getType())
                .user(user)
                .build();

        categoryRepository.save(category);
        
        activityLogService.log("CATEGORY_CREATED", "Created custom category: " + category.getName(), user);

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .build();
    }
}
