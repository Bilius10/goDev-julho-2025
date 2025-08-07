package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO.AddresDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CNPJ;

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
    @CNPJ(message = "{hub.cnpj.pattern}")
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
    private String neighborhood;

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
    @Column(nullable = false)
    private String cep;

    public HubEntity(HubCreateRequestDTO request, AddresDTO addresDTO){
        this.name = request.name();
        this.cnpj = request.cnpj();
        this.cep = request.cep();
        this.street = addresDTO.logradouro();
        this.number = request.number();
        this.neighborhood = addresDTO.bairro();
        this.city = addresDTO.localidade();
        this.state = addresDTO.uf();
        this.country = "Brasil";
    }

    public void updateAddres(AddresDTO addresDTO, String number){
        this.street = addresDTO.logradouro();
        this.number = number;
        this.neighborhood = addresDTO.bairro();
        this.city = addresDTO.localidade();
        this.state = addresDTO.uf();
    }

    public void updateCoordinates(CoordinatesDTO coordinatesDTO){
        this.latitude = coordinatesDTO.latitude();
        this.longitude = coordinatesDTO.longitude();
    }

    public String formatAddress() {
        return this.street + ", " + this.number + ", " + this.neighborhood + ", "
                + this.city + ", " + this.state + ", " + this.country + ", " + this.cep;
    }
}
