package com.sscope.sscope.login.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sscope.sscope.login.dto.LoginRequest;
import com.sscope.sscope.login.entity.Account;
import com.sscope.sscope.login.entity.KakaoUserInfo;
import com.sscope.sscope.login.enums.Role;
import com.sscope.sscope.login.oauth.KakaoOAuth2;
import com.sscope.sscope.login.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.sscope.sscope.login.security.JwtConstants.*;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final KakaoOAuth2 kakaoOAuth2;
    private final AuthenticationManager authenticationManager;

    @Override
    public Long saveAccount(LoginRequest dto) {
        validateDuplicateUsername(dto);
        dto.encodePassword(passwordEncoder.encode(dto.getPassword()));
        return accountRepo.save(dto.toEntity()).getId();
    }

    private void validateDuplicateUsername(LoginRequest dto) {
        if (accountRepo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("이미 존재하는 ID입니다.");
        }
    }

    // =============== TOKEN ============ //

    @Override
    public void updateRefreshToken(String username, String refreshToken) {
        Account account = accountRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        account.updateRefreshToken(refreshToken);
    }

    @Override
    public Map<String, String> refresh(String refreshToken) {

        // === Refresh Token 유효성 검사 === //
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(JWT_SECRET)).build();
        DecodedJWT decodedJWT = verifier.verify(refreshToken);

        // === Access Token 재발급 === //
        long now = System.currentTimeMillis();
        String username = decodedJWT.getSubject();
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        if (!account.getRefreshToken().equals(refreshToken)) {
            throw new JWTVerificationException("유효하지 않은 Refresh Token 입니다.");
        }
        String accessToken = JWT.create()
                .withSubject(account.getUsername())
                .withExpiresAt(new Date(now + AT_EXP_TIME))
                .withClaim("role", account.getRole().toString())
                .sign(Algorithm.HMAC256(JWT_SECRET));
        Map<String, String> accessTokenResponseMap = new HashMap<>();

        // === 현재시간과 Refresh Token 만료날짜를 통해 남은 만료기간 계산 === //
        // === Refresh Token 만료시간 계산해 1개월 미만일 시 refresh token도 발급 === //
        long refreshExpireTime = decodedJWT.getClaim("exp").asLong() * 1000;
        long diffDays = (refreshExpireTime - now) / 1000 / (24 * 3600);
        long diffMin = (refreshExpireTime - now) / 1000 / 60;
        if (diffMin < 5) {
            String newRefreshToken = JWT.create()
                    .withSubject(account.getUsername())
                    .withExpiresAt(new Date(now + RT_EXP_TIME))
                    .sign(Algorithm.HMAC256(JWT_SECRET));
            accessTokenResponseMap.put(RT_HEADER, newRefreshToken);
            account.updateRefreshToken(newRefreshToken);
        }

        accessTokenResponseMap.put(AT_HEADER, accessToken);
        return accessTokenResponseMap;
    }

    @Override
    public void kakaoLogin(String authorizedCode) throws JsonProcessingException {

        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(authorizedCode);
        Long oAuthId = userInfo.getOid();
        String email = userInfo.getEmail();

        String password = oAuthId.toString();

        Account kakaoAccount = accountRepo.findByOid(oAuthId).orElse(null);

        if (kakaoAccount == null) {
            Account account = Account.builder()
                    .oid(oAuthId)
                    .email(email)
                    .username(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.MEMBER)
                    .build();
            accountRepo.save(account);
        }

        Authentication authToken = new UsernamePasswordAuthenticationToken(email, password);
        authenticationManager.authenticate(authToken);
    }
}
