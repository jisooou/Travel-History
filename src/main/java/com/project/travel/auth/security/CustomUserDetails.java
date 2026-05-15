package com.project.travel.auth.security;

import com.project.travel.user.entity.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Integer userNo;
    private final String email;
    private final String userName;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(
            Integer userNo,
            String email,
            String userName,
            Collection<? extends GrantedAuthority> authorities) {
        this.userNo = userNo;
        this.email = email;
        this.userName = userName;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user, Collection<? extends GrantedAuthority> authorities) {
        return new CustomUserDetails(
                user.getUserNo(),
                user.getEmail(),
                user.getUserName(),
                authorities
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //    JWT 사용하여 Token을 검증한다.
    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
