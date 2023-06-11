package com.sscope.sscope.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sscope.sscope.login.dto.LoginRequest;


import java.util.Map;
public interface AccountService {
    Long saveAccount(LoginRequest dto);
    void updateRefreshToken(String username, String refreshToken);
    Map<String, String> refresh(String refreshToken);
    void kakaoLogin(String authorizedCode) throws JsonProcessingException;
}
