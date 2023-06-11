package com.sscope.sscope.login.security;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.sscope.sscope.login.entity.KakaoUserInfo;
import com.sscope.sscope.login.entity.Account;
import com.sscope.sscope.login.enums.Role;
import com.sscope.sscope.login.oauth.KakaoOAuth2;
import com.sscope.sscope.login.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Slf4j
public class CustomKakaoAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final KakaoOAuth2 kakaoOAuth2;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String authorizedCode = request.getParameter("code");
        KakaoUserInfo userInfo = null;
        try {
            userInfo = kakaoOAuth2.getUserInfo(authorizedCode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Long oid = userInfo.getOid();
        String email = userInfo.getEmail();

        String password = oid.toString();

        Account kakaoAccount = accountRepo.findByOid(oid).orElse(null);

        // 가입되어 있는 회원이 아닐 시 User DB에 생성 (회원가입 처리)
        if (kakaoAccount == null) {
            Account account = Account.builder()
                    .oid(oid)
                    .email(email)
                    .username(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.MEMBER)
                    .build();
            accountRepo.save(account);
        }

        // 생성된 id pw 기반으로 Provider에게 Authentication 객체를 넘겨주며 인증 요청
        Authentication authToken = new UsernamePasswordAuthenticationToken(email, password);
        return authenticationManager.authenticate(authToken);
    }
}
