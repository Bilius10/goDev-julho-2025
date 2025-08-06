package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubService {

    private final HubRepository repository;

    public PageDTO<HubResponseDTO> findAll(Pageable pageable) {
        Page<HubEntity> hubs = repository.findAll(pageable);

        Page<HubResponseDTO> dtosPage = hubs.map(HubResponseDTO::basic);

        return new PageDTO<>(
                dtosPage.getContent(),
                hubs.getNumber(),
                hubs.getSize(),
                hubs.getTotalElements(),
                hubs.getTotalPages());
    }

    public HubEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhuma filial encontrada"));
    }
}
