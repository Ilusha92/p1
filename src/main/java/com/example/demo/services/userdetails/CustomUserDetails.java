package com.example.demo.services.userdetails;

import com.example.demo.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String password;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter
    private final Long id;
    private final boolean isEnabled;
    private final boolean credentialsNotExpired;
    private final boolean accountNotExpired;
    private final boolean accountNotLocked;

    public CustomUserDetails(final User user) {
        this.id = user.getId();
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = List.of(user.getRoles());
        this.isEnabled = true;
        this.credentialsNotExpired = true;
        this.accountNotLocked = true;
        this.accountNotExpired = true;
    }

    public CustomUserDetails(final Long userId,
                             final String username,
                             final String password,
                             final Collection<? extends GrantedAuthority> authorities) {
        this.id = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.isEnabled = true;
        this.credentialsNotExpired = true;
        this.accountNotLocked = true;
        this.accountNotExpired = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNotExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNotLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNotExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public String toString() {
        return "{\"password=\":\"" + password + "\"," +
                "\"username=\":\"" + username + "\"," +
                "\"user_roles=\":\"" + authorities + "\"," +
                "\"user_id=\":\"" + id + "\"}";
    }
}
