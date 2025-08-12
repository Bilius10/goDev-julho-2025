package br.com.senior.transport_logistics.domain.hub;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<HubEntity, Long> {

    boolean existsByName(String name);
    boolean existsByCity(String city);
    boolean existsByCnpj(String cnpj);

}
