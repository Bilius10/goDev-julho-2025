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

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Size(max = 11, message = "{employee.cpf.size}")
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

    public EmployeeEntity(EmployeeCreateRequestDTO request, HubEntity hub) {
        EmployeeEntity.builder()
                .name(request.name())
                .cnh(request.cnh())
                .cpf(request.cpf())
                .email(request.email())
                .active(true)
                .role(Role.DRIVER)
                .hub(hub)
                .build();
    }

    public void updateEmployee(EmployeeUpdateRequestDTO request) {
        this.name = request.name();
        this.email = request.email();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
