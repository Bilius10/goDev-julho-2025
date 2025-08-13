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
                .sessionManagement(      session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Endpoints publicos
                    auth.requestMatchers(
                        "/v3/api-docs/**", 
                        "/swagger-ui.html", 
                        "/swagger-ui/**", 
                        "/docs/**"
                    ).permitAll();
                    
                    // Endpoints de autenticacao
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-in").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/create").hasRole("ADMIN");

                    // Endpoints de produto (ADMIN > MANAGER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/products").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/products").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/products/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/products/{id}").hasAnyRole("ADMIN", "MANAGER");

                    // Endpoints de carga (ADMIN > MANAGER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/shipments").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/shipments").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/shipments/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/shipments/{id}").hasAnyRole("ADMIN", "MANAGER");

                    // Endpoints de caminhao (ADMIN > MANAGER)
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/trucks").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/trucks/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/v1/trucks/{code}/status").hasAnyRole("ADMIN", "MANAGER");
                    
                    // Leitura - Caminhao (ADMIN > MANAGER > DRIVER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/trucks").hasAnyRole("ADMIN", "MANAGER", "DRIVER");
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/trucks/{code}").hasAnyRole("ADMIN", "MANAGER", "DRIVER");

                    // Gestao de filiais (ADMIN)
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/hubs").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/hubs/{id}").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/hubs/{id}").hasRole("ADMIN");
                    
                    // Leitura - Filial (ADMIN > MANAGER > DRIVER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/hubs").hasAnyRole("ADMIN", "MANAGER", "DRIVER");

                    // Gestão de funcionarios (ADMIN > MANAGER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/employees").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/employees/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/employees/{id}").hasAnyRole("ADMIN", "MANAGER");
                    
                    // Permissoes especiais de funcionarios
                    auth.requestMatchers(HttpMethod.PATCH, "/api/v1/employees/password").hasAnyRole("ADMIN", "MANAGER", "DRIVER");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/v1/employees/{id}/role").hasRole("ADMIN");

                    // Gestao de transportes (ADMIN > MANAGER)
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/transports").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/transports/optimize-allocation").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/v1/transports/confirm-transport/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/transports/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/transports/{id}").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/transports/send-weekly-schedule").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/transports/send-month-report").hasAnyRole("ADMIN", "MANAGER");
                    
                    // Permissoes especificas de motoristas para transportes (ADMIN > MANAGER > DRIVER)
                    auth.requestMatchers(HttpMethod.PATCH, "/api/v1/transports/update-status/{id}").hasAnyRole("ADMIN", "MANAGER", "DRIVER");

                    // Restante dos endpoints precisam de autenticacao
                    auth.anyRequest().authenticated();
                })
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
