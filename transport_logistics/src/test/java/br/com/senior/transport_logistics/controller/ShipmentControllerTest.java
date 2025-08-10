package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.shipment.ShipmentService;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentCreateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.request.ShipmentUpdateDTO;
import br.com.senior.transport_logistics.domain.shipment.dto.response.ShipmentResponseDTO;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @InjectMocks
    private ShipmentController controller;

    @Mock
    private ShipmentService service;

    @Test
    void findAll() {

        int page = 0;
        int size = 10;
        String sortBy = "name";
        boolean ascending = true;

        ShipmentResponseDTO shipmentDTO
                = new ShipmentResponseDTO(1L, 10.0, 5, "Notas", "Produto A", false);

        Page<ShipmentResponseDTO> mockedPage = new PageImpl<>(Collections.singletonList(shipmentDTO));

        PageDTO<ShipmentResponseDTO> mockedPageDTO = new PageDTO<>(
                mockedPage.getContent(),
                mockedPage.getNumber(),
                mockedPage.getSize(),
                mockedPage.getTotalElements(),
                mockedPage.getTotalPages()
        );

        when(service.findAll(
                any(Pageable.class)
        )).thenReturn(mockedPageDTO);

        ResponseEntity<PageDTO<ShipmentResponseDTO>> response = controller.findAll(
                page,
                size,
                sortBy,
                ascending
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockedPageDTO, response.getBody());
        assertEquals(1, response.getBody().totalElements());
    }

    @Test
    void create() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ShipmentCreateDTO requestDTO = new ShipmentCreateDTO(5, "Notas do novo shipment", false, 1L);
        ShipmentResponseDTO responseDTO = new ShipmentResponseDTO(1L, 10.0, 5, "Notas do novo shipment", "Produto A", false);

        when(service.create(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> response = controller.create(requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());

        verify(service).create(requestDTO);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void update() {
        Long shipmentId = 1L;
        ShipmentUpdateDTO requestDTO = new ShipmentUpdateDTO(10, "Notas atualizadas", true);
        ShipmentResponseDTO responseDTO = new ShipmentResponseDTO(shipmentId, 10.0, 10, "Notas atualizadas", "Produto A", true);

        when(service.update(shipmentId, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<ShipmentResponseDTO> response = controller.update(shipmentId, requestDTO);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());

        verify(service).update(shipmentId, requestDTO);
    }

    @Test
    void delete() {
        Long shipmentId = 1L;

        ResponseEntity<Void> response = controller.delete(shipmentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(service).delete(shipmentId);
    }
}