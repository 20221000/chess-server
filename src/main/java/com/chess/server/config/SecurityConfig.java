package com.chess.server.config;

import com.chess.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용하므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // JWT 사용하므로 세션 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입은 누구나 접근 가능
                        .requestMatchers("/api/auth/**").permitAll()
                        // WebSocket 누구나 접근 가능
                        .requestMatchers("/ws/**").permitAll()
                        // 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터를 Security 필터 앞에 추가
                .addFilterBefore(
                        new JwtFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )
                // 기본 로그인 폼 비활성화
                .formLogin(form -> form.disable())
                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    // 비밀번호 암호화 (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}