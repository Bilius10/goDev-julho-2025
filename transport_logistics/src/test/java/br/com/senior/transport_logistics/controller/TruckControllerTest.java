package br.com.senior.transport_logistics.controller;

import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.AxleSetup;
import br.com.senior.transport_logistics.domain.truck.enums.TruckBody;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TruckControllerTest {

    @InjectMocks
    private TruckController controller;

    @Mock
    private TruckService service;

    @Test
    void create() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        TruckRequestDTO requestDTO = new TruckRequestDTO("Volvo FH 540", 1L, TruckType.HEAVY_DUTY_TRUCK,
                TruckBody.DUMP_BODY, AxleSetup.AXLE_4x2, 25000.0, 18000.0, 14.5, 2.6,
                4.0, 2.5, "Air conditioning, GPS"
        );

        TruckResponseDTO responseDTO = new TruckResponseDTO("T-123", "Volvo FH 540", null,
                "HEAVY_DUTY_TRUCK", "DUMP_BODY", "AXLE_4x2 Axle", 25000.0, 18000.0,
                14.5, 2.6, 4.0, 2.5, "AVAILABLE", "Air conditioning, GPS"
        );

        when(service.create(any(TruckRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<TruckResponseDTO> response = controller.create(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("T-123", response.getBody().code());
    }

    @Test
    void getAlls() {

        int page = 0;
        int size = 10;
        String sortBy = "model";
        boolean ascending = true;
        TruckStatus status = TruckStatus.AVAILABLE;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));

        TruckResponseDTO responseDTO = new TruckResponseDTO("T-123", "Volvo FH 540", null,
                "HEAVY_DUTY_TRUCK", "DUMP_BODY", "AXLE_4x2 Axle", 25000.0, 18000.0,
                14.5, 2.6, 4.0, 2.5, "AVAILABLE", "Air conditioning, GPS"
        );

        PageDTO<TruckResponseDTO> mockedPageDTO
                = new PageDTO<>(Collections.singletonList(responseDTO), 0, 10, 1, 1);

        when(service.findAll(status, pageable)).thenReturn(mockedPageDTO);

        ResponseEntity<PageDTO<TruckResponseDTO>> response = controller.getAll(status, page, size, sortBy, ascending);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals("T-123", response.getBody().data().get(0).code());
    }

    @Test
    void getByCode() {

        String code = "T-123";

        TruckResponseDTO responseDTO = new TruckResponseDTO("T-123", "Volvo FH 540", null,
                "HEAVY_DUTY_TRUCK", "DUMP_BODY", "AXLE_4x2 Axle", 25000.0, 18000.0,
                14.5, 2.6, 4.0, 2.5, "AVAILABLE", "Air conditioning, GPS"
        );

        when(service.findByCode(code)).thenReturn(responseDTO);

        ResponseEntity<TruckResponseDTO> response = controller.getByCode(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(code, response.getBody().code());
    }

    @Test
    void updateStatus_ShouldReturnNoContent() {

        String code = "T-123";
        TruckStatus newStatus = TruckStatus.IN_TRANSIT;
        doNothing().when(service).updateStatus(code, newStatus);

        ResponseEntity<Void> response = controller.updateStatus(code, newStatus);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
}