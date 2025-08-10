package br.com.senior.transport_logistics.domain.hub;

import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.domain.product.dto.response.ProductResponseDTO;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO.AddresDTO;
import br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages;
import br.com.senior.transport_logistics.infrastructure.exception.common.FieldAlreadyExistsException;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.external.NominatimApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.ViaCepApiCilentService;
import org.hibernate.mapping.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HubServiceTest {

    @InjectMocks
    private HubService service;

    @Mock
    private HubRepository repository;

    @Mock
    private ViaCepApiCilentService viaCepApiCilentService;

    @Mock
    private NominatimApiClientService nominatimApiClientService;


    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        HubEntity updatedHub = new HubEntity(1L, "Hub Novo", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "001");
        Page<HubEntity> hubsPage = new PageImpl<>(Collections.singletonList(updatedHub), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(hubsPage);

        PageDTO<HubResponseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(updatedHub.getName(), result.data().get(0).name());;

    }

    @Test
    @DisplayName("Deve retornar o resumo de um hub com sucesso")
    void hubSummary_context1() {
        Long hubId = 1L;

        HubSummaryProjection hubSummary
                = new HubSummaryProjection(1L, "teste", "teste", null, null, 0.0);

        when(repository.findHubSummaryById(1L)).thenReturn(Optional.of(hubSummary));

        HubSummaryProjection hubSummaryResponse = service.hubSummary(hubId);

        assertEquals(hubSummaryResponse, hubSummaryResponse);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar resumo de hub inexistente")
    void hubSummary_context2() {
        Long hubId = 1L;

        when(repository.findHubSummaryById(hubId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.hubSummary(hubId));
        assertEquals(ExceptionMessages.HUB_NOT_FOUND_BY_ID.getMessage(hubId), exception.getMessage());
    }

    @Test
    @DisplayName("Deve criar um novo hub com sucesso")
    void create_context1() {
        HubCreateRequestDTO requestDTO = new HubCreateRequestDTO("Hub Novo", "00.000.000/0001-00", "01001000", "123");
        AddresDTO addressDTO = new AddresDTO("Rua Teste", "Bairro Teste", "Cidade Teste", "SP");
        CoordinatesDTO coordinatesDTO = new CoordinatesDTO(40.7128, -74.0060);
        HubEntity updatedHub = new HubEntity(1L, "Hub Novo", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "001");

        when(repository.existsByCnpj(anyString())).thenReturn(false);
        when(repository.existsByName(anyString())).thenReturn(false);
        when(repository.existsByCity(anyString())).thenReturn(false);
        when(viaCepApiCilentService.getAddress(anyString())).thenReturn(addressDTO);
        when(nominatimApiClientService.getCoordinates(anyString())).thenReturn(coordinatesDTO);
        when(repository.save(any(HubEntity.class))).thenReturn(updatedHub);

        HubResponseDTO result = service.create(requestDTO);

        assertNotNull(result);
        assertEquals(updatedHub.getName(), result.name());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar hub com nome já existente")
    void create_context2() {
        HubCreateRequestDTO requestDTO = new HubCreateRequestDTO("Hub Novo", "00.000.000/0001-00", "01001000", "123");
        when(repository.existsByName(anyString())).thenReturn(true);

        Exception exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(requestDTO));
        assertEquals(ExceptionMessages.HUB_NAME_IN_USE.getMessage(requestDTO.name()), exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar hub com CNPJ já existente")
    void create_context3() {
        HubCreateRequestDTO requestDTO = new HubCreateRequestDTO("Hub Novo", "00.000.000/0001-00", "01001000", "123");
        when(repository.existsByCnpj(anyString())).thenReturn(true);

        Exception exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(requestDTO));
        assertEquals(ExceptionMessages.HUB_CNPJ_IN_USE.getMessage(requestDTO.cnpj()), exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar hub em uma cidade que ja possui um")
    void create_context4() {
        HubCreateRequestDTO requestDTO = new HubCreateRequestDTO("Hub Novo", "00.000.000/0001-00", "01001000", "123");
        AddresDTO addressDTO = new AddresDTO("Rua Teste", "Bairro Teste", "Cidade Teste", "SP");

        when(repository.existsByCnpj(anyString())).thenReturn(false);
        when(repository.existsByName(anyString())).thenReturn(false);
        when(repository.existsByCity(anyString())).thenReturn(true);
        when(viaCepApiCilentService.getAddress(anyString())).thenReturn(addressDTO);

        Exception exception = assertThrows(FieldAlreadyExistsException.class, () -> service.create(requestDTO));
        assertEquals(ExceptionMessages.HUB_ALREADY_EXISTS_IN_CITY.getMessage(addressDTO.localidade()), exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar um hub com sucesso, caso que atualiza nome e endereço")
    void update() {
        Long hubId = 1L;
        HubUpdateRequestDTO requestDTO = new HubUpdateRequestDTO("Hub Atualizado", "001", "456");

        HubEntity existingHub = new HubEntity(1L, "Hub antigo", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "000");

        HubEntity updatedHub = new HubEntity(1L, "Hub Atualizado", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "001");

        AddresDTO addressDTO = new AddresDTO("Rua Teste", "Bairro Teste", "Cidade Teste", "SP");
        CoordinatesDTO coordinatesDTO = new CoordinatesDTO(40.7128, -74.0060);

        when(repository.findById(hubId)).thenReturn(Optional.of(existingHub));
        when(repository.existsByName(anyString())).thenReturn(false);
        when(viaCepApiCilentService.getAddress(anyString())).thenReturn(addressDTO);
        when(repository.existsByCity(anyString())).thenReturn(false);
        when(nominatimApiClientService.getCoordinates(anyString())).thenReturn(coordinatesDTO);
        when(repository.save(any(HubEntity.class))).thenReturn(updatedHub);

        HubResponseDTO result = service.update(requestDTO, hubId);

        assertNotNull(result);
        assertEquals(updatedHub.getName(), result.name());
        verify(repository).findById(hubId);
        verify(repository).save(any(HubEntity.class));
    }

    @Test
    @DisplayName("Deve deletar um hub com sucesso")
    void delete_context1() {
        Long hubId = 1L;

        when(repository.existsById(hubId)).thenReturn(true);

        service.delete(hubId);

        verify(repository).deleteById(hubId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar um hub inexistente")
    void delete_context2() {
        Long hubId = 99L;

        when(repository.existsById(hubId)).thenReturn(false);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.delete(hubId));
        assertEquals(ExceptionMessages.HUB_NOT_FOUND_BY_ID.getMessage(hubId), exception.getMessage());
    }

    @Test
    @DisplayName("Deve encontrar um hub por ID com sucesso")
    void findById_context1() {
        Long hubId = 1L;
        HubEntity hubEntity = new HubEntity(1L, "Hub Atualizado", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "000");

        when(repository.findById(hubId)).thenReturn(Optional.of(hubEntity));

        HubEntity result = service.findById(hubId);

        assertNotNull(result);
        assertEquals(hubEntity.getId(), result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar hub por ID inexistente")
    void findById_context2() {
        Long hubId = 99L;

        when(repository.findById(hubId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.findById(hubId));
        assertEquals(ExceptionMessages.HUB_NOT_FOUND_BY_ID.getMessage(hubId), exception.getMessage());
    }
}