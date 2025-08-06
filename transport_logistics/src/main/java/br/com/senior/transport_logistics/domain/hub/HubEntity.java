package br.com.senior.transport_logistics.domain.hub;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Hub")
@Table(name = "hubs")
public class HubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "{hub.name.notBlank}")
    @Size(max = 100, message = "{hub.name.size}")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "{hub.cnpj.notBlank}")
    @Pattern(message = "{hub.cnpj.pattern}", regexp = "^\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}$\n")
    @Column(nullable = false, unique = true)
    private String cnpj;

    @NotBlank(message = "{hub.street.notBlank}")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "{hub.number.notBlank}")
    @Size(max = 6, message = "{hub.number.size}")
    @Column(nullable = false)
    private String number;

    @NotBlank(message = "{hub.district.notBlank}")
    @Column(nullable = false)
    private String district;

    @NotBlank(message = "{hub.city.notBlank}")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "{hub.state.notBlank}")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "{hub.country.notBlank}")
    @Column(nullable = false)
    private String country;

    @NotNull(message = "{hub.latitude.notNull}")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "{hub.longitude.notNull}")
    @Column(nullable = false)
    private Double longitude;

    @NotBlank(message = "{hub.cep.notBlank}")
    @Pattern(regexp = "^(0[1-9]\\d{3}|1\\d{4})-?\\d{3}$")
    @Column(nullable = false)
    private String cep;

    public String formatAddress() {
        return this.street + ", " + this.number + ", " + this.district + ", "
                + this.city + ", " + this.state + ", " + this.country + ", " + this.cep;
    }
}
