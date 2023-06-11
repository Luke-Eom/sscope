package com.sscope.sscope.login.entity;

import com.sscope.sscope.login.enums.Role;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    private Long oid;
    private String email;

    @Setter
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.MEMBER;

    private String refreshToken;

    public void updateRefreshToken(String newToken) {
        this.refreshToken = newToken;
    }

}
