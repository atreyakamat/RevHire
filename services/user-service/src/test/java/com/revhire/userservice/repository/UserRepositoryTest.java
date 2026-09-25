package com.revhire.userservice.repository;

import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User("test@email.com", "password", Role.JOB_SEEKER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = new User("exists@email.com", "password", Role.EMPLOYER);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@email.com");

        assertThat(exists).isTrue();
    }
}
