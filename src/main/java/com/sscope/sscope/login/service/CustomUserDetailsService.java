package com.sscope.sscope.login.service;


import com.sscope.sscope.login.entity.Account;
import com.sscope.sscope.login.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("UserDetailsService - loadUserByUsername : 사용자를 찾을 수 없습니다."));

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(account.getUsername(), account.getPassword(), authorities);
    }
}
