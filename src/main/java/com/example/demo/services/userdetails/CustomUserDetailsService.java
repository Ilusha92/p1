package com.example.demo.services.userdetails;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Value("${spring.security.user.name}")
    private String adminUser;
    @Value("${spring.security.user.roles}")
    private String adminRole;
    @Value("${spring.security.user.password}")
    private String adminPassword;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("USER CAME: " + username);
        if (adminUser.equals(username)) {
            return new CustomUserDetails(null, adminUser, adminPassword,
                    List.of(new SimpleGrantedAuthority("ROLE_" + adminRole)));
        } else {
            User user = userRepository.findByEmail(username);
            return new CustomUserDetails(user);
        }
    }
}
