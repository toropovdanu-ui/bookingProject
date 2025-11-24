package com.skillbox.security;

import com.skillbox.entity.RoleType;
import com.skillbox.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {
    private final UserEntity userEntity;


    public Long getUserId(){
        return userEntity.getId();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = userEntity.getRoles().size() == 1 ? RoleType.ROLE_USER.name() :
                RoleType.ROLE_ADMIN.name();

        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }
}
