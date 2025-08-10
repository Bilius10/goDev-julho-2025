package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO.AddresDTO;
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.external.NominatimApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.ViaCepApiCilentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.*;

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
                .orElseThrow(() -> new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(id)));
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

        return HubResponseDTO.detailed(saveHub);
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

        return HubResponseDTO.detailed(saveHub);
    }

    @Transactional
    public void delete(Long id){
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(id));
        }

        repository.deleteById(id);
    }

    public HubEntity findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(id)));
    }

    private void createValidation(HubCreateRequestDTO request){
        verifyIfCnpjIsUsed(request.cnpj());
        verifyIfNameIsUsed(request.name());
    }

    private void verifyIfNameIsUsed(String name){
        if(repository.existsByName(name)){
            throw new FieldAlreadyExistsException(HUB_NAME_IN_USE.getMessage(name));
        }
    }

    private void verifyIfCnpjIsUsed(String cnpj){
        if(repository.existsByCnpj(cnpj)){
            throw new FieldAlreadyExistsException(HUB_CNPJ_IN_USE.getMessage(cnpj));
        }
    }

    private void checkIfThereIsAHubInTheCity(String cidade){
        if(repository.existsByCity(cidade)){
            throw new FieldAlreadyExistsException(HUB_ALREADY_EXISTS_IN_CITY.getMessage(cidade));
        }
    }
}
