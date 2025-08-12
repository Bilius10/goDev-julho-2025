package br.com.senior.transport_logistics.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Autenticação
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-in").permitAll()

                        // Produtos
                        .requestMatchers(HttpMethod.GET, "/api/v1/products").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/{id}").hasRole("MANAGER")

                        // Cargas
                        .requestMatchers(HttpMethod.GET, "/api/v1/shipments").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/shipments").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/shipments/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/shipments/{id}").hasRole("MANAGER")

                        // Caminhões
                        .requestMatchers(HttpMethod.GET, "/api/v1/trucks", "/api/v1/trucks/{code}").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/trucks").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/trucks/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/trucks/{code}/status").hasRole("MANAGER")

                        // Hubs
                        .requestMatchers(HttpMethod.GET, "/api/v1/hubs").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/hubs").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/hubs/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/hubs/{id}").hasRole("ADMIN")

                        // Funcionários
                        .requestMatchers(HttpMethod.GET, "/api/v1/employees").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/employees/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/employees/password").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/employees/{id}/role").hasRole("ADMIN")

                        // Transportes
                        .requestMatchers(HttpMethod.GET, "/api/v1/transports").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/transports/optimize-allocation").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/transports/confirm-transport/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/transports/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/transports/{id}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/transports/update-status/{id}").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/transports/send-weekly-schedule").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/transports/send-month-report").hasRole("MANAGER")

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/docs/**").permitAll()

                        // Qualquer outra requisição
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
