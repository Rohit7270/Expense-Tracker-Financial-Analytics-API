package com.expensetracker.repository;

import com.expensetracker.entity.Tag;
import com.expensetracker.entity.User;
import com.expensetracker.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = entityManager.persist(User.builder()
                .name("User One")
                .email("one@example.com")
                .password("password123")
                .role(Role.USER)
                .build());

        user2 = entityManager.persist(User.builder()
                .name("User Two")
                .email("two@example.com")
                .password("password123")
                .role(Role.USER)
                .build());

        entityManager.flush();
    }

    @Test
    @DisplayName("Unique constraint uk_tags_user_name prevents duplicate tag for the same user")
    void testDuplicateTagForSameUserThrowsDataIntegrityException() {
        Tag tag1 = Tag.builder().name("Groceries").user(user1).build();
        tagRepository.saveAndFlush(tag1);

        Tag duplicateTag = Tag.builder().name("Groceries").user(user1).build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            tagRepository.saveAndFlush(duplicateTag);
        });
    }

    @Test
    @DisplayName("Different users can have tags with the same name")
    void testSameTagDifferentUsersSucceeds() {
        Tag tag1 = Tag.builder().name("Groceries").user(user1).build();
        tagRepository.saveAndFlush(tag1);

        Tag tag2 = Tag.builder().name("Groceries").user(user2).build();
        Tag savedTag2 = tagRepository.saveAndFlush(tag2);

        assertNotNull(savedTag2.getId());
        assertEquals(tag1.getName(), savedTag2.getName());
        assertNotEquals(tag1.getUser().getId(), savedTag2.getUser().getId());
    }
}
