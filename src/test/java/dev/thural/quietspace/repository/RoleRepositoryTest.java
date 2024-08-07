package dev.thural.quietspace.repository;

import dev.thural.quietspace.entity.Role;
import dev.thural.quietspace.entity.User;
import dev.thural.quietspace.utils.enums.RoleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    private final User user = User.builder()
            .email("user@email.com")
            .username("user")
            .firstname("firstname")
            .lastname("lastname")
            .password("78921731")
            .accountLocked(false)
            .username("test user")
            .createDate(OffsetDateTime.now())
            .updateDate(OffsetDateTime.now())
            .build();

    private final Role adminRole = Role.builder()
            .name(RoleType.ADMIN.toString())
            .user(List.of(user))
            .build();

    private User savedUser;
    private Role savedRole;

    @BeforeEach
    void setUp() {
        this.savedUser = userRepository.save(user);
        this.savedRole = roleRepository.save(adminRole);
        // USER role is bootstrapped on spring context load
    }

    @AfterEach
    void tearDown() {
        this.userRepository.delete(savedUser);
        this.roleRepository.delete(savedRole);
    }

    @Test
    void findByName() {
        Optional<Role> role = roleRepository.findByName(RoleType.ADMIN.toString());
        assertTrue(role.isPresent());
        assertEquals(RoleType.ADMIN.name(), role.get().getName());
    }

    @Test
    void findUserRole() {
        Optional<Role> role = roleRepository.findByName(RoleType.USER.toString());
        assertTrue(role.isPresent());
        assertEquals(RoleType.USER.name(), role.get().getName());
    }
}