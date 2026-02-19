// backend-spring/src/main/java/org/backend/config/security/SecurityConfig.java
package org.backend.config.security;

import org.backend.config.security.filter.JwtAuthenticationFilter;
import org.backend.config.security.handler.SecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(JwtProvider jwtProvider, SecurityExceptionHandler securityExceptionHandler) {
        this.jwtProvider = jwtProvider;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProvider);

        http
                // REST API 기준: CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 기본 폼 로그인/Basic 로그인 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 사용 안함(JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //  401/403 예외를 ApiError JSON으로 내려주기
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityExceptionHandler) //401
                        .accessDeniedHandler(securityExceptionHandler) //403
                )

                // CORS
                .cors(Customizer.withDefaults())

                // 인가 정책
                .authorizeHttpRequests(auth -> auth
                        // 로그인/구글 콜백은 열어두기
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger 사용 중이면 열어둠 (필요 없으면 삭제)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // OPTIONS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터는 UsernamePasswordAuthenticationFilter 앞에
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
