package ru.alenavir.mini_ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.alenavir.mini_ecommerce.security.jwt.JwtFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public static final String API_V1 = "/api/v1";

    public static final String REGISTRATION_ENTRY_POINT = API_V1 + "/users/registration";
    public static final String AUTH_ENTRY_POINT = API_V1 + "/auth/**";
    public static final String PRODUCTS_VIEW_ENTRY_POINT = API_V1 + "/products/**";
    public static final String ADMIN_ENTRY_POINT = API_V1 + "/admin/**";

    public static final String ROLE_ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // регистрация и логин доступны всем
                        .requestMatchers(REGISTRATION_ENTRY_POINT).permitAll()
                        .requestMatchers(AUTH_ENTRY_POINT).permitAll()

                        // просмотр продуктов доступен всем
                        .requestMatchers(HttpMethod.GET, PRODUCTS_VIEW_ENTRY_POINT).permitAll()

                        // админка доступна только users с ролью ADMIN
                        .requestMatchers(PRODUCTS_VIEW_ENTRY_POINT).hasRole(ROLE_ADMIN)
                        .requestMatchers(ADMIN_ENTRY_POINT).hasRole(ROLE_ADMIN)

                        // все остальные запросы требуют авторизации
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 401
                        .accessDeniedHandler((request, response, accessDeniedException) -> { // 403
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }
}
