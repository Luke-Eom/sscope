package com.sscope.sscope.login.config;


import com.sscope.sscope.login.oauth.KakaoOAuth2;
import com.sscope.sscope.login.repository.AccountRepository;
import com.sscope.sscope.login.security.CustomAuthenticationFilter;
import com.sscope.sscope.login.security.CustomAuthorizationFilter;
import com.sscope.sscope.login.security.CustomKakaoAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final AccessDeniedHandler accessDeniedHandler;

    private final KakaoOAuth2 kakaoOAuth2;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 일반 로그인 인증 필터
        CustomAuthenticationFilter customAuthenticationFilter =
                new CustomAuthenticationFilter(authenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");
        customAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        customAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        // Kakao 로그인 인증 필터
        CustomKakaoAuthenticationFilter customKakaoAuthenticationFilter =
                new CustomKakaoAuthenticationFilter(kakaoOAuth2, accountRepo, passwordEncoder, authenticationManagerBean());
        customKakaoAuthenticationFilter.setFilterProcessesUrl("/api/oauth/kakao");
        customKakaoAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        customKakaoAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 사용 X
        http.authorizeRequests().antMatchers("/api/my/**").hasAnyAuthority("ROLE_USER");
        http.authorizeRequests().antMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN");
        http.authorizeRequests().anyRequest().permitAll();
        http.addFilter(customKakaoAuthenticationFilter);
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);

        http.oauth2Login();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
