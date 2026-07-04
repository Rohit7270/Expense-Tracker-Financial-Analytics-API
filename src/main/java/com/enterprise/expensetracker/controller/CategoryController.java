package com.enterprise.expensetracker.controller;

import com.enterprise.expensetracker.dto.CategoryDto;
import com.enterprise.expensetracker.security.UserDetailsImpl;
import com.enterprise.expensetracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CategoryDto> categories = categoryService.getCategoriesForUser(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CategoryDto categoryDto
    ) {
        CategoryDto created = categoryService.createCategory(userDetails.getId(), categoryDto);
        return ResponseEntity.ok(created);
    }
}
