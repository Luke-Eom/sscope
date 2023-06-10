package com.sscope.sscope.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;


import java.util.Map;
public interface AccountService {
    Long saveAccount(AccountRequestDto dto);
    Long addRoleToUser(RoleToUserRequestDto dto);

    void updateRefreshToken(String username, String refreshToken);

    Map<String, String> refresh(String refreshToken);

    void kakaoLogin(String authorizedCode) throws JsonProcessingException;
}
