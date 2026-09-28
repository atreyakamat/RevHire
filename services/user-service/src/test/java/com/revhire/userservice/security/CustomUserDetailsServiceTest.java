package com.revhire.userservice.security;

import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void testLoadUserByUsername_Success() {
        User user = new User("test@revhire.com", "secret", Role.JOB_SEEKER);
        user.setId(42L);

        when(userRepository.findByEmail("test@revhire.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@revhire.com");

        assertNotNull(userDetails);
        assertEquals("test@revhire.com", userDetails.getUsername());
        assertEquals("secret", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_JOB_SEEKER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testLoadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("missing@revhire.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@revhire.com"));
    }
}
