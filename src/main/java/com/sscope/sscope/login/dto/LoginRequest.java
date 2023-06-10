package com.sscope.sscope.login.dto;

import com.sscope.sscope.login.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;

    public Account toEntity() {
        return Account.builder()
                .username(username)
                .password(password)
                .build();

    }

    public void encodePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}
