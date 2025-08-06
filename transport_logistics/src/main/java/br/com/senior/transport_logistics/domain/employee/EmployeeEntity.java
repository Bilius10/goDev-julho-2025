package br.com.senior.transport_logistics.domain.employee;

import br.com.senior.transport_logistics.domain.employee.dto.request.EmployeeRequestDTO;
import br.com.senior.transport_logistics.domain.employee.enums.Role;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employees")
@Entity(name = "Employee")
public class EmployeeEntity {

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
    private String cnh;

    @Column(name = "cpf")
    @Size(max = 11, message = "{employee.cpf.size}")
    private String cpf;

    @Column(name = "email")
    @Size(max = 100, message = "{employee.email.size}")
    @NotBlank(message = "{employee.email.notBlank}")
    private String email;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "hub_id")
    private HubEntity hub;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{employee.role.notNull}")
    @Size(max = 50, message = "{employee.role.size}")
    private Role role;

    public EmployeeEntity(EmployeeRequestDTO request) {
        this.name = request.name();
        this.cnh = request.cnh();
        this.cpf = request.cpf();
        this.email = request.email();
        this.active = true;
        this.role = request.role();
    }
}
