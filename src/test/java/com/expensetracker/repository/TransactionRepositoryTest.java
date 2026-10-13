package com.expensetracker.repository;

import com.expensetracker.entity.Account;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.enums.AccountType;
import com.expensetracker.enums.Role;
import com.expensetracker.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private User otherUser;
    private Account checkingAccount;
    private Account savingsAccount;
    private Category foodCategory;
    private Category transportCategory;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        testUser = entityManager.persist(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("encoded_pass")
                .role(Role.USER)
                .build());

        otherUser = entityManager.persist(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .password("encoded_pass")
                .role(Role.USER)
                .build());

        checkingAccount = entityManager.persist(Account.builder()
                .name("Checking")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .user(testUser)
                .build());

        savingsAccount = entityManager.persist(Account.builder()
                .name("Savings")
                .type(AccountType.BANK)
                .balance(new BigDecimal("5000.00"))
                .user(testUser)
                .build());

        foodCategory = entityManager.persist(Category.builder()
                .name("Food & Dining")
                .type(TransactionType.EXPENSE)
                .icon("utensils")
                .user(null)
                .build());

        transportCategory = entityManager.persist(Category.builder()
                .name("Transport")
                .type(TransactionType.EXPENSE)
                .icon("car")
                .user(null)
                .build());

        salaryCategory = entityManager.persist(Category.builder()
                .name("Salary")
                .type(TransactionType.INCOME)
                .icon("money-bill")
                .user(null)
                .build());

        // Persist transactions for testUser
        entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.EXPENSE)
                .description("Lunch")
                .transactionDate(LocalDate.of(2026, 10, 5))
                .category(foodCategory)
                .account(checkingAccount)
                .user(testUser)
                .build());

        entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .description("Groceries")
                .transactionDate(LocalDate.of(2026, 10, 10))
                .category(foodCategory)
                .account(checkingAccount)
                .user(testUser)
                .build());

        entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("30.00"))
                .type(TransactionType.EXPENSE)
                .description("Bus fare")
                .transactionDate(LocalDate.of(2026, 10, 12))
                .category(transportCategory)
                .account(checkingAccount)
                .user(testUser)
                .build());

        entityManager.persist(Transaction.builder()
                .amount(new BigDecimal("3000.00"))
                .type(TransactionType.INCOME)
                .description("Monthly salary")
                .transactionDate(LocalDate.of(2026, 10, 1))
                .category(salaryCategory)
                .account(checkingAccount)
                .user(testUser)
                .build());

        entityManager.flush();
    }

    @Test
    @DisplayName("search() - Filters by type, category, and date range accurately")
    void testSearchFilters() {
        List<Transaction> foodExpenses = transactionRepository.search(
                testUser,
                TransactionType.EXPENSE,
                foodCategory.getId(),
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(2, foodExpenses.size());
        assertTrue(foodExpenses.stream().allMatch(t -> t.getCategory().getName().equals("Food & Dining")));

        // Search with no filters returns all 4 transactions for testUser
        List<Transaction> all = transactionRepository.search(testUser, null, null, null, null, null);
        assertEquals(4, all.size());

        // Search for otherUser returns empty
        List<Transaction> otherTx = transactionRepository.search(otherUser, null, null, null, null, null);
        assertTrue(otherTx.isEmpty());
    }

    @Test
    @DisplayName("sumByUserAndTypeAndDateRange() - Aggregates total amount by type")
    void testSumByUserAndTypeAndDateRange() {
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateRange(
                testUser,
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("180.00"), totalExpenses);

        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateRange(
                testUser,
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("3000.00"), totalIncome);

        // Date range with no transactions returns 0
        BigDecimal emptySum = transactionRepository.sumByUserAndTypeAndDateRange(
                testUser,
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        assertEquals(BigDecimal.ZERO, emptySum);
    }

    @Test
    @DisplayName("sumExpenseByCategoryAndDateRange() - Aggregates expenses for specific category")
    void testSumExpenseByCategoryAndDateRange() {
        BigDecimal foodTotal = transactionRepository.sumExpenseByCategoryAndDateRange(
                testUser,
                foodCategory.getId(),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("150.00"), foodTotal);

        BigDecimal transportTotal = transactionRepository.sumExpenseByCategoryAndDateRange(
                testUser,
                transportCategory.getId(),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("30.00"), transportTotal);
    }

    @Test
    @DisplayName("sumExpensesGroupedByCategory() - Returns grouped category rows ordered by total desc")
    void testSumExpensesGroupedByCategory() {
        List<Object[]> rows = transactionRepository.sumExpensesGroupedByCategory(
                testUser,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(2, rows.size());

        // First row should be highest expense (Food & Dining = 150.00)
        assertEquals("Food & Dining", rows.get(0)[0]);
        assertEquals(new BigDecimal("150.00"), rows.get(0)[1]);

        // Second row should be Transport = 30.00
        assertEquals("Transport", rows.get(1)[0]);
        assertEquals(new BigDecimal("30.00"), rows.get(1)[1]);
    }

    @Test
    @DisplayName("existsByAccount() and existsByCategory() - Detects referenced entities")
    void testExistsByAccountAndCategory() {
        assertTrue(transactionRepository.existsByAccount(checkingAccount));
        assertFalse(transactionRepository.existsByAccount(savingsAccount));

        assertTrue(transactionRepository.existsByCategory(foodCategory));
        assertTrue(transactionRepository.existsByCategory(transportCategory));

        Category unusedCategory = entityManager.persist(Category.builder()
                .name("Entertainment")
                .type(TransactionType.EXPENSE)
                .icon("film")
                .build());
        entityManager.flush();

        assertFalse(transactionRepository.existsByCategory(unusedCategory));
    }
}
