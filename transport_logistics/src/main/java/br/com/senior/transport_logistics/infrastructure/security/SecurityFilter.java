package br.com.senior.transport_logistics.infrastructure.security;

import br.com.senior.transport_logistics.domain.employee.EmployeeRepository;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final EmployeeRepository repository;
    private final TokenService tokenService;

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            String email = tokenService.getSubject(token);
            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", email));

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null) {
            return null;
        }

        return authHeader.replace(BEARER, "");
    }
}