package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubCreateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.request.HubUpdateRequestDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubResponseDTO;
import br.com.senior.transport_logistics.domain.hub.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubControllerTest {

    @InjectMocks
    private HubController controller;

    @Mock
    private HubService service;

    @Test
    void findAll() {
        int page = 0;
        int size = 10;
        String sortBy = "name";
        boolean ascending = true;

        HubEntity hubEntity = new HubEntity(1L, "Hub Atualizado", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "000");

        Page<HubEntity> mockedPage = new PageImpl<>(Collections.singletonList(hubEntity));

        Page<HubResponseDTO> dtosPage = mockedPage.map(HubResponseDTO::basic);

        PageDTO<HubResponseDTO> mockedPageDTO = new PageDTO<>(
                dtosPage.getContent(),
                mockedPage.getNumber(),
                mockedPage.getSize(),
                mockedPage.getTotalElements(),
                mockedPage.getTotalPages()
        );

        when(service.findAll(any(Pageable.class))).thenReturn(mockedPageDTO);

        ResponseEntity<PageDTO<HubResponseDTO>> response = controller.findAll(page, size, sortBy, ascending);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    void hubSummary() {
        Long hubId = 1L;
        HubSummaryProjection hubSummaryProjection
                = new HubSummaryProjection(1l, "teste", "teste", null, null, 0.0);

        when(service.hubSummary(hubId)).thenReturn(hubSummaryProjection);

        ResponseEntity<HubSummaryProjection> response = controller.hubSummary(hubId);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(hubSummaryProjection, response.getBody());

        verify(service).hubSummary(hubId);
    }

    @Test
    void create() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        HubCreateRequestDTO requestDTO = new HubCreateRequestDTO("Hub Atualizado", "0", "0", "0");
        HubResponseDTO responseDTO
                = new HubResponseDTO(1L, "Hub Atualizado", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "000");

        when(service.create(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<HubResponseDTO> response = controller.create(requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());

        RequestContextHolder.resetRequestAttributes();

        verify(service).create(requestDTO);
    }

    @Test
    void update() {
        Long hubId = 1L;
        HubUpdateRequestDTO requestDTO = new HubUpdateRequestDTO("Hub Atualizado", "0", "0");
        HubResponseDTO responseDTO
                = new HubResponseDTO(hubId, "Hub Atualizado", "0", "rua teste",
                "0", "bairro teste", "cidade testes", "estado teste",
                "pais teste", 0.0, 0.0, "000");

        when(service.update(requestDTO, hubId)).thenReturn(responseDTO);

        ResponseEntity<HubResponseDTO> response = controller.update(hubId, requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());

        verify(service).update(requestDTO, hubId);
    }

    @Test
    void delete() {
        Long hubId = 1L;

        ResponseEntity<Void> response = controller.delete(hubId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(service).delete(hubId);
    }
}