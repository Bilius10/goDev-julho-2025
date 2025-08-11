package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeCreateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employees")
@Entity(name = "Employee")
public class EmployeeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    @Size(max = 100, message = "{employee.name.size}")
    @NotBlank(message = "{employee.name.notBlank}")
    private String name;

    @Column(name = "cnh")
    @Size(max = 11, message = "{employee.cnh.size}")
    @NotBlank(message = "{employee.cnh.notBlank}")
    @Pattern(regexp = "^\\d{11}$", message = "{employee.cnh.format}")
    private String cnh;

    @Column(name = "cpf")
    @CPF(message = "{employee.cpf.format}")
    private String cpf;

    @Column(name = "email")
    @Size(max = 100, message = "{employee.email.size}")
    @NotBlank(message = "{employee.email.notBlank}")
    @Email(message = "{employee.email.format}")
    private String email;

    @Column(name = "password")
    @Size(min = 8, max = 100, message = "{employee.password.size}")
    @NotBlank(message = "{employee.password.notBlank}")
    private String password;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "hub_id")
    private HubEntity hub;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{employee.role.notNull}")
    private Role role;

    public EmployeeEntity(EmployeeCreateRequestDTO request, HubEntity hub, String password) {
        this.name = request.name();
        this.cnh = request.cnh();
        this.cpf = request.cpf();
        this.email = request.email();
        this.password = password;
        this.active = true;
        this.role = Role.DRIVER;
        this.hub = hub;
    }

    public void updateEmployee(EmployeeUpdateRequestDTO request) {
        this.name = request.name();
        this.email = request.email();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (this.role == Role.MANAGER) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));
        } else if (this.role == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

}
