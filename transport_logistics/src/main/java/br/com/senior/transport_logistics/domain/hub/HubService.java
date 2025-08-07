package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.dto.response.EmployeeResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.dto.AddresDTO;
import br.com.senior.transport_logistics.dto.CoordinatesDTO;
import br.com.senior.transport_logistics.dto.PageDTO;
import br.com.senior.transport_logistics.service.NominatimApiClientService;
import br.com.senior.transport_logistics.service.ViaCepApiCilentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HubService {

    private final HubRepository repository;
    private final ViaCepApiCilentService viaCepApiCilentService;
    private final NominatimApiClientService nominatimApiClientService;

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

    public HubSummaryProjection hubSummary(Long id){
        return repository.findHubSummaryById(id)
                .orElseThrow(() -> new RuntimeException("Sede não encontrada"));
    }

    @Transactional
    public HubResponseDTO create(HubCreateRequestDTO request){
        createValidation(request);

        AddresDTO address = viaCepApiCilentService.getAddress(request.cep());

        checkIfThereIsAHubInTheCity(address.localidade());

        HubEntity hubEntity = new HubEntity(request, address);

        CoordinatesDTO coordinates = nominatimApiClientService.getCoordinates(hubEntity.formatAddress());

        hubEntity.updateCoordinates(coordinates);

        HubEntity saveHub = repository.save(hubEntity);

        return HubResponseDTO.basic(saveHub);
    }

    @Transactional
    public HubResponseDTO update(HubUpdateRequestDTO request, Long id){
        HubEntity hubFound = this.findById(id);

        if(!Objects.equals(hubFound.getName(), request.name())){
            verifyIfNameIsUsed(request.name());
            hubFound.setName(request.name());
        }

        if(!Objects.equals(hubFound.getCep(), request.cep())){
            AddresDTO address = viaCepApiCilentService.getAddress(request.cep());

            checkIfThereIsAHubInTheCity(address.localidade());

            hubFound.updateAddres(address, request.number());

            CoordinatesDTO coordinates = nominatimApiClientService.getCoordinates(hubFound.formatAddress());
            hubFound.updateCoordinates(coordinates);
        }

        HubEntity saveHub = repository.save(hubFound);

        return HubResponseDTO.basic(saveHub);
    }

    @Transactional
    public void delete(Long id){
        if(repository.existsById(id)){
            throw new RuntimeException("Sede não encontrada");
        }

        repository.deleteById(id);
    }

    private void createValidation(HubCreateRequestDTO request){
        verifyIfCnpjIsUsed(request.cnpj());
        verifyIfNameIsUsed(request.name());
    }

    private void verifyIfNameIsUsed(String name){
        if(repository.existsByName(name)){
            throw new RuntimeException("Nome ja em uso");
        }
    }

    private void verifyIfCnpjIsUsed(String cnpj){
        if(repository.existsByCnpj(cnpj)){
            throw new RuntimeException("Cnpj ja em uso");
        }
    }

    private void checkIfThereIsAHubInTheCity(String cidade){
        if(repository.existsByCity(cidade)){
            throw new RuntimeException("Já existe uma sede nessa cidade");
        }
    }

    public HubEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nenhuma filial encontrada"));
    }
}
