package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.employee.EmployeeService;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.hub.HubService;
import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentService;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.request.UpdateTransportRequest;
import br.com.senior.transport_logistics.domain.transport.dto.response.HubSummaryProjection;
import br.com.senior.transport_logistics.domain.transport.dto.response.TransportCreatedResponseDTO;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.domain.truck.TruckService;
import br.com.senior.transport_logistics.domain.truck.dto.response.AverageDimensionsTrucks;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.TransportRecommendation;
import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ORSRoute;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response.StepRecord;
import br.com.senior.transport_logistics.infrastructure.email.SpringMailSenderService;
import br.com.senior.transport_logistics.infrastructure.exception.common.ResourceNotFoundException;
import br.com.senior.transport_logistics.infrastructure.external.GeminiApiClientService;
import br.com.senior.transport_logistics.infrastructure.external.OpenRouteApiClientService;
import br.com.senior.transport_logistics.infrastructure.pdf.PdfGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.com.senior.transport_logistics.domain.transport.enums.TransportStatus.*;
import static br.com.senior.transport_logistics.domain.truck.enums.AxleSetup.AXLE_4x2;
import static br.com.senior.transport_logistics.domain.truck.enums.TruckBody.DRY_VAN;
import static br.com.senior.transport_logistics.domain.truck.enums.TruckType.LIGHT_DUTY_TRUCK;
import static br.com.senior.transport_logistics.infrastructure.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock
    private TransportRepository repository;

    @Mock
    private TruckService truckService;

    @Mock
    private HubService hubService;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private OpenRouteApiClientService openRouteApiClientService;

    @Mock
    private GeminiApiClientService geminiApiClientService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SpringMailSenderService emailService;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @InjectMocks
    private TransportService service;

    private static Long defaultID;
    private static TruckEntity truck;
    private static ShipmentEntity sh1;
    private static EmployeeEntity emp1;
    private static HubEntity hub1;
    private static TransportEntity transport1;
    private static ProductEntity product;
    private static HubEntity hubBlumenau;
    private static HubEntity hubNavegantes;
    private static ShipmentEntity optShipment;
    private static ShipmentEntity pendingShipment;

    private static List<ShipmentEntity> allPendingShipments;

    private static HubSummaryProjection hubSummaryProjection;

    // necessario para realizar a auto-injecao e evitar null pointer
    @BeforeEach
    void setUpSelfInjection() {
        ReflectionTestUtils.setField(service, "self", service);
    }

    @BeforeAll
    static void setUp() {
        defaultID = 1L;

        hub1 = new HubEntity();
        hub1.setId(defaultID);

        truck = new TruckEntity();
        truck.setId(defaultID);
        truck.setModel("Mercedes-Benz Actros 2651");
        truck.setHub(hubBlumenau);
        truck.setType(LIGHT_DUTY_TRUCK);
        truck.setBody(DRY_VAN);
        truck.setAxleSetup(AXLE_4x2);
        truck.setLoadCapacity(20000.0);
        truck.setWeight(8000.0);
        truck.setLength(12.0);
        truck.setWidth(2.5);
        truck.setHeight(3.8);
        truck.setAverageFuelConsumption(2.5);
        truck.setFeatures("Ar-condicionado, GPS, Freio ABS");

        sh1 = new ShipmentEntity();
        sh1.setId(defaultID);

        emp1 = new EmployeeEntity();
        emp1.setId(defaultID);

        hub1 = new HubEntity();
        hub1.setId(defaultID);

        hubBlumenau = new HubEntity();
        hubBlumenau.setId(2L);
        hubBlumenau.setName("Blumenau");
        hubBlumenau.setLatitude(-26.9126133);
        hubBlumenau.setLongitude(-49.0878262);

        hubNavegantes = new HubEntity();
        hubNavegantes.setId(3L);
        hubNavegantes.setName("Navegantes");
        hubNavegantes.setLatitude(-26.8757926);
        hubNavegantes.setLongitude(-48.6817321);

        product = new ProductEntity();
        product.setId(defaultID);
        product.setName("Produto Exemplo");
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setWeight(10.5);
        product.setActive(true);

        optShipment = new ShipmentEntity();
        optShipment.setId(2L);
        optShipment.setWeight(100.0);
        optShipment.setQuantity(10);
        optShipment.setNotes("Exemplo");
        optShipment.setHazardous(false);
        optShipment.setProduct(product);
        optShipment.setStatus(TransportStatus.PENDING);
        optShipment.setOriginHub(hubBlumenau);
        optShipment.setDestinationHub(hubNavegantes);

        pendingShipment = new ShipmentEntity();
        pendingShipment.setId(3L);
        pendingShipment.setWeight(100.0);
        pendingShipment.setQuantity(5);
        pendingShipment.setNotes("Pending shipment notes");
        pendingShipment.setHazardous(false);
        pendingShipment.setProduct(product);
        pendingShipment.setOriginHub(hubNavegantes);
        pendingShipment.setDestinationHub(hubBlumenau);
        pendingShipment.setStatus(TransportStatus.PENDING);

        allPendingShipments = List.of(pendingShipment);

        transport1 = new TransportEntity();
        transport1.setId(defaultID);
        transport1.setFuelConsumption(25.0);
        transport1.setDistance(500.0);
        transport1.setExitDay(LocalDate.now());
        transport1.setExpectedArrivalDay(LocalDate.now().plusDays(3));
        transport1.setStatus(PENDING);
        transport1.setTruck(truck);
        transport1.setShipment(optShipment);
        transport1.setDriver(emp1);
        transport1.setOriginHub(hub1);
        transport1.setDestinationHub(hubBlumenau);

        hubSummaryProjection = new HubSummaryProjection(
                defaultID,
                "HubName",
                "HubAddress",
                new String[]{"Truck 1", "Truck 2"},
                new String[]{"Empl 1", "Empl 2"},
                BigDecimal.valueOf(250.75)
        );
    }

    @Test
    @DisplayName("Deve listar transportes com paginação")
    void testFindAll_shouldFindAllPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TransportEntity> transportsPage = new PageImpl<>(List.of(transport1), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(transportsPage);

        var response = service.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.data().size());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());

        assertEquals(transport1.getId(), response.data().get(0).id());
        assertEquals(transport1.getExitDay(), response.data().get(0).exitDay());
        assertEquals(transport1.getExpectedArrivalDay(), response.data().get(0).expectedArrivalDay());
        assertEquals(transport1.getStatus().getDescription(), response.data().get(0).status());
        assertEquals(transport1.getShipment().getId(), response.data().get(0).shipmentTrackingCode());
    }

    @Test
    @DisplayName("Deve listar transportes com paginação sem nenhum conteudo")
    void testFindAll_shouldFindAllPaginatedWithoutContent() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TransportEntity> transportsPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(transportsPage);

        var response = service.findAll(pageable);

        assertNotNull(response);
        assertEquals(0, response.data().size());
        assertEquals(10, response.size());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName("Deve criar transporte com rota, caminhão e motorista otimizados. Carga no retorno foi possível")
    void testOptimizeAllocation_shouldCreateSuccessfully() throws JsonProcessingException {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                optShipment.getId(),
                hubBlumenau.getId(),
                hubNavegantes.getId(),
                false
        );

        var averageDimensions = new AverageDimensionsTrucks(8000.0, 12.0, 3.8);

        var route = new ORSRoute(
                200.0,
                18000,
                List.of(
                        new StepRecord(
                                200,
                                18000,
                                1,
                                "Exemplo",
                                "Exemplo",
                                List.of(1, 2, 3),
                                1
                        )
                )
        );

        var choosedTruck = new TransportRecommendation(
                truck.getId(),
                pendingShipment.getId(),
                "Exemplo de justificativa",
                "Exemplo de justificativa 2",
                10.0,
                10.0
        );

        var durationInDays = transportRequest.exitDay().plusDays((long) Math.ceil(route.duration() / 86400.0));

        var routeStepsJson = "exemplo";

        when(hubService.findById(hubBlumenau.getId())).thenReturn(hubBlumenau);
        when(hubService.findById(hubNavegantes.getId())).thenReturn(hubNavegantes);
        when(shipmentService.findById(transportRequest.idShipment())).thenReturn(optShipment);

        when(shipmentService.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, hubNavegantes.getId(), hubBlumenau.getId())).thenReturn(allPendingShipments);

        when(truckService.findAverageDimensionsTrucks()).thenReturn(averageDimensions);

        when(openRouteApiClientService.obterDistancia(
                any(CoordinatesDTO.class),
                any(CoordinatesDTO.class),
                any(RestrictionsRecord.class)
        )).thenReturn(route);

        when(truckService.findByLoadCapacityGreaterThan(
                optShipment.getWeight(),
                hubBlumenau.getId(),
                transportRequest.exitDay(),
                durationInDays
        )).thenReturn(List.of(truck));

        when(objectMapper.writeValueAsString(route.steps())).thenReturn(routeStepsJson);

        when(geminiApiClientService.chooseBestTruck(
                routeStepsJson,
                route.distance(),
                optShipment,
                List.of(truck),
                allPendingShipments
        )).thenReturn(choosedTruck);

        when(truckService.findById(choosedTruck.suggestedTruckId())).thenReturn(truck);

        when(employeeService.findDriversOrderedByHistoryScore(
                choosedTruck.suggestedTruckId(),
                hubNavegantes.getId(),
                hubBlumenau.getId()
        )).thenReturn(emp1);

        var exitTransport = new TransportEntity(
                emp1, hubBlumenau, hubNavegantes, optShipment,
                truck, route.distance(), choosedTruck.litersSpentOutbound(), transportRequest.exitDay(),
                durationInDays
        );

        when(shipmentService.findById(pendingShipment.getId())).thenReturn(pendingShipment);

        var returnTransport = new TransportEntity(
                emp1, hubNavegantes, hubBlumenau, pendingShipment,
                truck, route.distance() / 2, choosedTruck.litersSpentReturn(), null,
                durationInDays
        );

        var response = service.optimizeAllocation(transportRequest);

        var expected = TransportCreatedResponseDTO.buildCreatedResponse(
                exitTransport, choosedTruck.truckJustification(), choosedTruck.returnShipmentJustification(),
                choosedTruck.litersSpentOutbound(), route.distance()
        );

        assertNotNull(response);
        assertEquals(expected.idTransport(), response.idTransport());

        verify(repository).save(exitTransport);
        verify(repository).save(returnTransport);
    }

    @Test
    @DisplayName("Deve criar transporte com rota, caminhão e motorista otimizados. Sem carga no retorno")
    void testOptimizeAllocation_shouldCreateSuccessfullyWithoutReturnShipment() throws JsonProcessingException {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                optShipment.getId(),
                hubBlumenau.getId(),
                hubNavegantes.getId(),
                false
        );

        var averageDimensions = new AverageDimensionsTrucks(8000.0, 12.0, 3.8);

        var route = new ORSRoute(
                200.0,
                18000,
                List.of(
                        new StepRecord(
                                200,
                                18000,
                                1,
                                "Exemplo",
                                "Exemplo",
                                List.of(1, 2, 3),
                                1
                        )
                )
        );

        var choosedTruck = new TransportRecommendation(
                truck.getId(),
                null,
                "Exemplo de justificativa",
                null,
                10.0,
                10.0
        );

        var durationInDays = transportRequest.exitDay().plusDays((long) Math.ceil(route.duration() / 86400.0));

        var routeStepsJson = "exemplo";

        when(hubService.findById(hubBlumenau.getId())).thenReturn(hubBlumenau);
        when(hubService.findById(hubNavegantes.getId())).thenReturn(hubNavegantes);
        when(shipmentService.findById(transportRequest.idShipment())).thenReturn(optShipment);

        when(shipmentService.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, hubNavegantes.getId(), hubBlumenau.getId())).thenReturn(allPendingShipments);

        when(truckService.findAverageDimensionsTrucks()).thenReturn(averageDimensions);

        when(openRouteApiClientService.obterDistancia(
                any(CoordinatesDTO.class),
                any(CoordinatesDTO.class),
                any(RestrictionsRecord.class)
        )).thenReturn(route);

        when(truckService.findByLoadCapacityGreaterThan(
                optShipment.getWeight(),
                hubBlumenau.getId(),
                transportRequest.exitDay(),
                durationInDays
        )).thenReturn(List.of(truck));

        when(objectMapper.writeValueAsString(route.steps())).thenReturn(routeStepsJson);

        when(geminiApiClientService.chooseBestTruck(
                routeStepsJson,
                route.distance(),
                optShipment,
                List.of(truck),
                allPendingShipments
        )).thenReturn(choosedTruck);

        when(truckService.findById(choosedTruck.suggestedTruckId())).thenReturn(truck);

        when(employeeService.findDriversOrderedByHistoryScore(
                choosedTruck.suggestedTruckId(),
                hubNavegantes.getId(),
                hubBlumenau.getId()
        )).thenReturn(emp1);

        var exitTransport = new TransportEntity(
                emp1, hubBlumenau, hubNavegantes, optShipment,
                truck, route.distance(), choosedTruck.litersSpentOutbound(), transportRequest.exitDay(),
                durationInDays
        );

        var response = service.optimizeAllocation(transportRequest);

        var expected = TransportCreatedResponseDTO.buildCreatedResponse(
                exitTransport, choosedTruck.truckJustification(), choosedTruck.returnShipmentJustification(),
                choosedTruck.litersSpentOutbound(), route.distance()
        );

        assertNotNull(response);
        assertEquals(expected.idTransport(), response.idTransport());

        verify(repository).save(exitTransport);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não localizar Filial de origem")
    void testOptimizeAllocation_shouldThrowResourceNotFoundExceptionWhenNotFoundOriginHub() {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                optShipment.getId(),
                150L,
                hubNavegantes.getId(),
                false
        );

        when(hubService.findById(transportRequest.idOriginHub()))
                .thenThrow(new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(transportRequest.idOriginHub())));

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.optimizeAllocation(transportRequest));

        assertNotNull(exception);
        assertEquals(HUB_NOT_FOUND_BY_ID.getMessage(transportRequest.idOriginHub()), exception.getMessage());

        verifyNoMoreInteractions(hubService);
        verifyNoInteractions(shipmentService);
        verifyNoInteractions(truckService);
        verifyNoInteractions(employeeService);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(pdfGenerationService);
        verifyNoInteractions(repository);
        verifyNoInteractions(openRouteApiClientService);
        verifyNoInteractions(geminiApiClientService);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não localizar Filial de destino")
    void testOptimizeAllocation_shouldThrowResourceNotFoundExceptionWhenNotFoundDestinationHub() {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                optShipment.getId(),
                hubBlumenau.getId(),
                150L,
                false
        );

        when(hubService.findById(transportRequest.idOriginHub()))
                .thenThrow(new ResourceNotFoundException(HUB_NOT_FOUND_BY_ID.getMessage(transportRequest.idDestinationHub())));

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.optimizeAllocation(transportRequest));

        assertNotNull(exception);
        assertEquals(HUB_NOT_FOUND_BY_ID.getMessage(transportRequest.idDestinationHub()), exception.getMessage());

        verifyNoMoreInteractions(hubService);
        verifyNoInteractions(shipmentService);
        verifyNoInteractions(truckService);
        verifyNoInteractions(employeeService);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(pdfGenerationService);
        verifyNoInteractions(repository);
        verifyNoInteractions(openRouteApiClientService);
        verifyNoInteractions(geminiApiClientService);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando não localizar carga")
    void testOptimizeAllocation_shouldThrowResourceNotFoundExceptionWhenNotFoundShipment() {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                150L,
                hubBlumenau.getId(),
                hubNavegantes.getId(),
                false
        );

        when(hubService.findById(transportRequest.idOriginHub()))
                .thenThrow(new ResourceNotFoundException(SHIPMENT_NOT_FOUND_BY_ID.getMessage(transportRequest.idShipment())));

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.optimizeAllocation(transportRequest));

        assertNotNull(exception);
        assertEquals(SHIPMENT_NOT_FOUND_BY_ID.getMessage(transportRequest.idShipment()), exception.getMessage());

        verifyNoMoreInteractions(hubService);
        verifyNoMoreInteractions(shipmentService);
        verifyNoInteractions(truckService);
        verifyNoInteractions(employeeService);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(pdfGenerationService);
        verifyNoInteractions(repository);
        verifyNoInteractions(openRouteApiClientService);
        verifyNoInteractions(geminiApiClientService);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao não localizar caminhão recomendado")
    void testOptimizeAllocation_shouldThrowResourceNotFoundExceptionWhenNotFoundTruck() throws JsonProcessingException {
        var transportRequest = new CreateTransportRequest(
                LocalDate.now().plusDays(5),
                optShipment.getId(),
                hubBlumenau.getId(),
                hubNavegantes.getId(),
                false
        );

        var averageDimensions = new AverageDimensionsTrucks(8000.0, 12.0, 3.8);

        var route = new ORSRoute(
                200.0,
                18000,
                List.of(
                        new StepRecord(
                                200,
                                18000,
                                1,
                                "Exemplo",
                                "Exemplo",
                                List.of(1, 2, 3),
                                1
                        )
                )
        );

        var choosedTruck = new TransportRecommendation(
                truck.getId(),
                pendingShipment.getId(),
                "Exemplo de justificativa",
                "Exemplo de justificativa 2",
                10.0,
                10.0
        );

        var durationInDays = transportRequest.exitDay().plusDays((long) Math.ceil(route.duration() / 86400.0));

        var routeStepsJson = "exemplo";

        when(hubService.findById(hubBlumenau.getId())).thenReturn(hubBlumenau);
        when(hubService.findById(hubNavegantes.getId())).thenReturn(hubNavegantes);
        when(shipmentService.findById(transportRequest.idShipment())).thenReturn(optShipment);

        when(shipmentService.findAllByIdHubAndDestinationHubAndStatus(TransportStatus.PENDING, hubNavegantes.getId(), hubBlumenau.getId())).thenReturn(allPendingShipments);

        when(truckService.findAverageDimensionsTrucks()).thenReturn(averageDimensions);

        when(openRouteApiClientService.obterDistancia(
                any(CoordinatesDTO.class),
                any(CoordinatesDTO.class),
                any(RestrictionsRecord.class)
        )).thenReturn(route);

        when(truckService.findByLoadCapacityGreaterThan(
                optShipment.getWeight(),
                hubBlumenau.getId(),
                transportRequest.exitDay(),
                durationInDays
        )).thenReturn(List.of(truck));

        when(objectMapper.writeValueAsString(route.steps())).thenReturn(routeStepsJson);

        when(geminiApiClientService.chooseBestTruck(
                routeStepsJson,
                route.distance(),
                optShipment,
                List.of(truck),
                allPendingShipments
        )).thenReturn(choosedTruck);

        when(truckService.findById(choosedTruck.suggestedTruckId())).thenThrow(new ResourceNotFoundException(
                TRUCK_NOT_FOUND_BY_ID.getMessage(choosedTruck.suggestedTruckId())
        ));

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.optimizeAllocation(transportRequest));

        assertNotNull(exception);
        assertEquals(TRUCK_NOT_FOUND_BY_ID.getMessage(choosedTruck.suggestedTruckId()), exception.getMessage());

        verifyNoMoreInteractions(hubService);
        verifyNoMoreInteractions(shipmentService);
        verifyNoMoreInteractions(truckService);
        verifyNoInteractions(employeeService);
        verifyNoInteractions(pdfGenerationService);
        verifyNoInteractions(repository);
        verifyNoMoreInteractions(objectMapper);
        verifyNoMoreInteractions(openRouteApiClientService);
        verifyNoMoreInteractions(geminiApiClientService);
    }

    @Test
    @DisplayName("Deve obter resumo da filial")
    void testHubSummary_shouldGetSuccessfully() {
        when(repository.findHubSummaryById(defaultID)).thenReturn(Optional.of(hubSummaryProjection));

        var response = service.hubSummary(defaultID);

        assertNotNull(response);
        assertEquals(hubSummaryProjection, response);

        verify(repository, times(1)).findHubSummaryById(defaultID);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar obter resumo da filial")
    void testHubSummary_shouldThrowResourceNotFoundException() {
        when(repository.findHubSummaryById(defaultID)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.hubSummary(defaultID));

        assertNotNull(exception);
        assertEquals(HUB_NOT_FOUND_BY_ID.getMessage(defaultID), exception.getMessage());

        verify(repository, times(1)).findHubSummaryById(defaultID);
    }

    @Test
    @DisplayName("Deve confirmar transporte com sucesso")
    void testConfirmTransport_shouldConfirmSuccessfully() {
        var pdf = new byte[]{1, 2, 3};

        var confirmedTransport = transport1;
        confirmedTransport.setStatus(ASSIGNED);

        when(repository.findById(defaultID)).thenReturn(Optional.of(transport1));
        when(repository.save(any(TransportEntity.class))).thenReturn(confirmedTransport);
        when(pdfGenerationService.generateTransportManifestPdf(confirmedTransport)).thenReturn(pdf);
        doNothing().when(emailService).sendConfirmTransportEmail(any(TransportEntity.class), any(byte[].class));

        var response = service.confirmTransport(defaultID);

        assertNotNull(response);
        assertEquals(confirmedTransport.getStatus().getDescription(), response.status());
    }

    @Test
    @DisplayName("Deve atualizar status do transporte com sucesso")
    void testStatusUpdate_shouldUpdateStatusSuccessfully() {
        when(repository.findById(defaultID)).thenReturn(Optional.of(transport1));
        when(repository.save(any(TransportEntity.class))).thenReturn(transport1);

        var response = service.updateStatus(defaultID, DELIVERED);

        assertNotNull(response);
        assertEquals(DELIVERED.getDescription(), response.status());

        verify(repository, times(1)).findById(defaultID);
        verify(repository, times(1)).save(any(TransportEntity.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar status de transporte inexistente")
    void testStatusUpdate_shouldThrowResourceNotFoundException() {
        when(repository.findById(defaultID)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.updateStatus(defaultID, DELIVERED));

        assertNotNull(exception);
        assertEquals(TRANSPORT_NOT_FOUND_BY_ID.getMessage(defaultID), exception.getMessage());

        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Deve atualizar transporte com sucesso")
    void testUpdate_shouldUpdateSuccessfully() {
        var nowPlusFiveDays = LocalDate.now().plusDays(5);

        var updateRequest = new UpdateTransportRequest(nowPlusFiveDays, emp1.getId(), PENDING);

        when(repository.findById(defaultID)).thenReturn(Optional.of(transport1));
        when(employeeService.findById(defaultID)).thenReturn(emp1);
        when(repository.save(any(TransportEntity.class))).thenReturn(transport1);

        var response = service.update(updateRequest, defaultID);

        assertNotNull(response);
        assertEquals(nowPlusFiveDays, response.exitDay());
        assertEquals(emp1.getName(), response.driverName());
        assertEquals(PENDING.getDescription(), response.status());

        verify(repository, times(1)).findById(defaultID);
        verify(employeeService, times(1)).findById(defaultID);
        verify(repository, times(1)).save(any(TransportEntity.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar por ID de Transporte inexistente")
    void testUpdate_shouldThrowResourceNotFoundExceptionWhenTransportIDNotFound() {
        when(repository.findById(defaultID)).thenReturn(Optional.empty());

        var response = assertThrows(ResourceNotFoundException.class, () -> service.update(any(UpdateTransportRequest.class), defaultID));

        assertEquals(TRANSPORT_NOT_FOUND_BY_ID.getMessage(defaultID), response.getMessage());

        verifyNoMoreInteractions(repository);
        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar por ID de Funcionário inexistente")
    void testUpdate_shouldThrowResourceNotFoundExceptionWhenEmployeeIDNotFound() {
        var nowPlusFiveDays = LocalDate.now().plusDays(5);
        var updateRequest = new UpdateTransportRequest(nowPlusFiveDays, emp1.getId(), PENDING);

        when(repository.findById(defaultID)).thenReturn(Optional.of(transport1));
        when(employeeService.findById(defaultID)).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> service.update(updateRequest, defaultID));

        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(employeeService);
    }

    @Test
    @DisplayName("Deve deletar transporte com sucesso")
    void testDelete_shouldDeleteSuccessfully() {
        when(repository.existsById(defaultID)).thenReturn(true);

        service.delete(defaultID);

        verify(repository).deleteById(defaultID);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar com ID inexistente")
    void testDelete_shouldThrowResourceNotFoundException() {
        when(repository.existsById(defaultID)).thenReturn(false);

        var exception = assertThrows(ResourceNotFoundException.class, () -> service.delete(defaultID));

        assertEquals(TRANSPORT_NOT_FOUND_BY_ID.getMessage(defaultID), exception.getMessage());

        verifyNoMoreInteractions(repository);
    }
}
