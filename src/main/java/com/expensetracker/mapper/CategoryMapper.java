package com.expensetracker.mapper;

import com.expensetracker.dto.request.CategoryRequest;
import com.expensetracker.dto.response.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for Category Entity <-> DTO conversions.
 */
@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .global(category.getUser() == null)
                .build();
    }

    public Category toEntity(CategoryRequest request, User user) {
        if (request == null) {
            return null;
        }
        return Category.builder()
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .user(user)
                .build();
    }
}
