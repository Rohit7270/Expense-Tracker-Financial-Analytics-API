package com.expensetracker.config;

import com.expensetracker.entity.Category;
import com.expensetracker.enums.TransactionType;
import com.expensetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        categoryRepository.saveAll(java.util.List.of(
                Category.builder().name("Food & Dining").type(TransactionType.EXPENSE).icon("utensils").build(),
                Category.builder().name("Transport").type(TransactionType.EXPENSE).icon("car").build(),
                Category.builder().name("Shopping").type(TransactionType.EXPENSE).icon("shopping-bag").build(),
                Category.builder().name("Entertainment").type(TransactionType.EXPENSE).icon("film").build(),
                Category.builder().name("Bills & Utilities").type(TransactionType.EXPENSE).icon("file-invoice").build(),
                Category.builder().name("Healthcare").type(TransactionType.EXPENSE).icon("heart-pulse").build(),
                Category.builder().name("Other Expense").type(TransactionType.EXPENSE).icon("ellipsis").build(),
                Category.builder().name("Salary").type(TransactionType.INCOME).icon("money-bill").build(),
                Category.builder().name("Investment").type(TransactionType.INCOME).icon("chart-line").build(),
                Category.builder().name("Other Income").type(TransactionType.INCOME).icon("ellipsis").build()
        ));
    }
}
