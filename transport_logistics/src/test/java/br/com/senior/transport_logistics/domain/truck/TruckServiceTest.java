package br.com.senior.transport_logistics.domain.truck;

import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.truck.dto.request.TruckRequestDTO;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.domain.truck.dto.response.TruckResponseDTO;
import br.com.senior.transport_logistics.domain.truck.enums.AxleSetup;
import br.com.senior.transport_logistics.domain.truck.enums.TruckBody;
import br.com.senior.transport_logistics.domain.truck.enums.TruckStatus;
import br.com.senior.transport_logistics.domain.truck.enums.TruckType;
import br.com.senior.transport_logistics.infrastructure.dto.PageDTO;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TruckServiceTest {
    @Mock
    private TruckRepository repository;

    @Mock
    private HubService hubService;

    @InjectMocks
    private TruckService service;

    private HubEntity hub;

    @BeforeEach
    void setUp() {
        hub = new HubEntity();
    }

    @Test
    @DisplayName("Deve criar o caminhão corretamente")
    void create_shouldCreate() {
        TruckRequestDTO request = createTruckRequestDTO();
        TruckEntity truck = new TruckEntity(request, generateTruckCode(request.type()), hub);

        when(hubService.findById(1L)).thenReturn(hub);
        when(repository.save(any(TruckEntity.class))).thenReturn(truck);

        TruckResponseDTO respostaEsperada = TruckResponseDTO.detailed(truck);
        TruckResponseDTO truckResponseDTO = service.create(request);

        assertEquals(truckResponseDTO, respostaEsperada);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o hub não existe")
    void create_shouldThrowException() {
        TruckRequestDTO request = createTruckRequestDTO();
        TruckEntity truck = new TruckEntity(request, generateTruckCode(request.type()), hub);

        when(hubService.findById(1L)).thenThrow(new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(1L)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.create(request);
        });

        assertEquals(HUB_NOT_FOUND_BY_ID.getMessage(request.hubId()), exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todos corretamente usando filtros")
    void findAll_shouldList() {
        TruckStatus truckStatus = TruckStatus.AVAILABLE;
        Pageable pageable = PageRequest.of(0, 10);

        List<TruckEntity> truckEntities = getTruckEntityList();

        Page<TruckEntity> trucksPage = new PageImpl<>(truckEntities, pageable, 2);

        when(repository.findAll(truckStatus, pageable))
                .thenReturn(trucksPage);

        PageDTO<TruckResponseDTO> result = service.findAll(truckStatus, pageable);

        assertEquals(result.data().get(0).code(), truckEntities.get(0).getCode());
        assertEquals(result.data().get(1).status(), truckEntities.get(1).getStatus().getDescription());
        assertEquals(result.data().get(1).loadCapacity(), truckEntities.get(1).getLoadCapacity());
        assertEquals(2, result.totalElements());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver nenhum")
    void findAll_shouldReturnEmptyList() {
        TruckStatus truckStatus = TruckStatus.AVAILABLE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<TruckEntity> trucksPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(repository.findAll(truckStatus, pageable))
                .thenReturn(trucksPage);

        PageDTO<TruckResponseDTO> result = service.findAll(truckStatus, pageable);

        assertEquals(0, result.totalElements());
    }

    @Test
    @DisplayName("Deve retornar truck corretamente")
    void findByCode_shouldReturn() {
        String code = generateTruckCode(TruckType.LIGHT_DUTY_TRUCK);

        TruckEntity truck = createTruckEntity(code);

        when(repository.findByCode(code)).thenReturn(Optional.of(truck));

        TruckResponseDTO expected = TruckResponseDTO.detailed(truck);
        TruckResponseDTO result = service.findByCode(code);

        assertEquals(result, expected);
    }

    @Test
    @DisplayName("Deve lançar exceção quando truck não for encontrado")
    void findByCode_shouldThrowException() {
        String code = generateTruckCode(TruckType.LIGHT_DUTY_TRUCK);
        when(repository.findByCode(code)).thenThrow(new ResourceNotFoundException(TRUCK_NOT_FOUND_BY_CODE.getMessage(code)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.findEntityByCode(code);
        });

        assertEquals(TRUCK_NOT_FOUND_BY_CODE.getMessage(code), exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar o status corretamente")
    void updateStatus_shouldUpdate() {
        String code = generateTruckCode(TruckType.PICKUP_TRUCK);
        TruckEntity truck = createTruckEntity(code);
        TruckStatus truckStatus = TruckStatus.IN_TRANSIT;

        when(repository.findByCode(code)).thenReturn(Optional.of(truck));

        service.updateStatus(code, truckStatus);

        assertEquals(truckStatus, truck.getStatus());
        verify(repository).save(truck);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o truck não for encontrado")
    void updateStatus_shouldThrowException() {
        String code = generateTruckCode(TruckType.PICKUP_TRUCK);

        when(repository.findByCode(code)).thenThrow(new ResourceNotFoundException(TRUCK_NOT_FOUND_BY_CODE.getMessage(code)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.updateStatus(code, TruckStatus.IN_TRANSIT);
        });

        assertEquals(TRUCK_NOT_FOUND_BY_CODE.getMessage(code), exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar truck corretamente")
    void findEntityByCode_shouldReturn() {
        String code = generateTruckCode(TruckType.LIGHT_DUTY_TRUCK);

        TruckEntity truck = createTruckEntity(code);

        when(repository.findByCode(code)).thenReturn(Optional.of(truck));

        TruckEntity result = service.findEntityByCode(code);

        assertEquals(result, truck);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o truck não for encontrado")
    void findEntityByCode_shouldThrowException() {
        String code = generateTruckCode(TruckType.PICKUP_TRUCK);

        when(repository.findByCode(code)).thenThrow(new ResourceNotFoundException(TRUCK_NOT_FOUND_BY_CODE.getMessage(code)));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.findEntityByCode(code);
        });

        assertEquals(TRUCK_NOT_FOUND_BY_CODE.getMessage(code), exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar lista de trucks corretamente")
    void findAllByHub_shouldReturn() {
        List<TruckEntity> truckEntities = getTruckEntityList();

        when(repository.findAllByHub(hub)).thenReturn(truckEntities);
        List<TruckEntity> result = service.findAllByHub(hub);

        assertEquals(result.get(0).getCode(), truckEntities.get(0).getCode());
        assertEquals(result.get(1).getStatus(), truckEntities.get(1).getStatus());
        assertEquals(result.get(1).getLoadCapacity(), truckEntities.get(1).getLoadCapacity());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve retornar lista vazia")
    void findAllByHub_shouldReturnEmptyList() {
        when(repository.findAllByHub(hub)).thenReturn(new ArrayList<>());

        List<TruckEntity> result = service.findAllByHub(hub);

        assertEquals(0, result.size());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Deve retornar truck corretamente")
    void findById_shouldReturnTruck() {
        String code = generateTruckCode(TruckType.LIGHT_DUTY_TRUCK);
        TruckEntity truck = createTruckEntity(code);

        when(repository.findById(1L)).thenReturn(Optional.of(truck));

        TruckEntity result = service.findById(1L);

        assertEquals(truck, result);
    }

    @Test
    @DisplayName("Deve retornar médias de dimensões dos caminhões quando houver dados")
    void findAverageDimensionsTrucks_shouldReturnAverageDimensions() {
        AverageDimensionsTrucks averageDimensionsTrucks = new AverageDimensionsTrucks(8500.0, 12.5, 3.8);

        when(repository.findAverageDimensionsTrucks()).thenReturn(Optional.of(averageDimensionsTrucks));

        AverageDimensionsTrucks result = service.findAverageDimensionsTrucks();

        assertEquals(averageDimensionsTrucks.weightAvarege(), result.weightAvarege());
        assertEquals(averageDimensionsTrucks.lengthAvarege(), result.lengthAvarege());
        assertEquals(averageDimensionsTrucks.heightAvarege(), result.heightAvarege());
    }

    @Test
    @DisplayName("Deve lançar exceção quando não houver caminhões")
    void findAverageDimensionsTrucks_shouldThrowExceptionWhenNoTrucksExist() {
        when(repository.findAverageDimensionsTrucks()).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.findAverageDimensionsTrucks();
        });

        assertEquals(NO_TRUCK_IN_THE_SYSTEM.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar caminhões disponíveis com capacidade maior que a informada e não em rota")
    void findByLoadCapacityGreaterThan_shouldReturnList() {
        Double loadCapacity = 10000.0;
        Long hubId = 1L;
        LocalDate exitDay = LocalDate.of(2025, 8, 20);
        LocalDate expectArrivalDay = LocalDate.of(2025, 8, 25);

        List<TruckEntity> expectedTrucks = List.of(
                createTruckEntity(generateTruckCode(TruckType.LIGHT_DUTY_TRUCK))
        );

        when(repository.findAvailableTrucksByCapacityAndHubNotInRouteBetween(loadCapacity, hubId, exitDay, expectArrivalDay))
                .thenReturn(expectedTrucks);

        List<TruckEntity> result = service.findByLoadCapacityGreaterThan(loadCapacity, hubId, exitDay, expectArrivalDay);

        assertEquals(1, result.size());
        assertEquals(expectedTrucks.get(0).getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhum caminhão disponível suporta a carga")
    void findByLoadCapacityGreaterThan_shouldThrowException() {
        Double loadCapacity = 50000.0;
        Long hubId = 1L;
        LocalDate exitDay = LocalDate.of(2025, 8, 20);
        LocalDate expectArrivalDay = LocalDate.of(2025, 8, 25);

        when(repository.findAvailableTrucksByCapacityAndHubNotInRouteBetween(loadCapacity, hubId, exitDay, expectArrivalDay))
                .thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.findByLoadCapacityGreaterThan(loadCapacity, hubId, exitDay, expectArrivalDay);
        });

        assertEquals(TRUCK_NOT_SUPPORT_LOAD.getMessage(loadCapacity), exception.getMessage());
    }

    private static TruckRequestDTO createTruckRequestDTO() {
        return new TruckRequestDTO("Volvo FH 540",
                1L, TruckType.LIGHT_DUTY_TRUCK, TruckBody.DUMP_BODY, AxleSetup.AXLE_4x2, 32000.0, 8500.0, 12.5, 2.5, 3.8, 2.2, "Ar-condicionado, GPS"
        );
    }

    private List<TruckEntity> getTruckEntityList() {
        return List.of(
                new TruckEntity(1L, generateTruckCode(TruckType.LIGHT_DUTY_TRUCK),"Volvo FH 540", hub, TruckType.LIGHT_DUTY_TRUCK, TruckBody.DUMP_BODY, AxleSetup.AXLE_4x2, 32000.0, 8500.0, 12.5, 2.5, 3.8, 2.2, TruckStatus.AVAILABLE,"Ar-condicionado, GPS"),
                new TruckEntity(2L, generateTruckCode(TruckType.SEMI_TRUCK),"Volvo FH 540", hub, TruckType.SEMI_TRUCK, TruckBody.DUMP_BODY, AxleSetup.AXLE_6x2, 32000.0, 9000.0, 13.5, 2.5, 3.8, 3.5, TruckStatus.AVAILABLE,"Ar-condicionado, GPS")
        );
    }

    private static String generateTruckCode(TruckType type) {
        long timeMillisSuffix = System.currentTimeMillis() % 10000;
        String dateTimeCode = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TR-%s-%s-%04d", type, dateTimeCode, timeMillisSuffix);
    }

    private TruckEntity createTruckEntity(String code) {
        return new TruckEntity(1L, code,"Volvo FH 540", hub, TruckType.LIGHT_DUTY_TRUCK, TruckBody.DUMP_BODY, AxleSetup.AXLE_4x2, 32000.0, 8500.0, 12.5, 2.5, 3.8, 2.2, TruckStatus.AVAILABLE,"Ar-condicionado, GPS");
    }
}