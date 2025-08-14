package br.com.senior.transport_logistics.domain.transport;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.transport.dto.request.CreateTransportRequest;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import br.com.senior.transport_logistics.infrastructure.dto.GeminiDTO.TransportRecommendation;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ORSRoute;

import java.time.LocalDate;

public record TransportAllocationData(
        HubEntity originHub,
        HubEntity destinationHub,
        ShipmentEntity shipment,
        ShipmentEntity returnShipment,
        EmployeeEntity chosenDriver,
        TruckEntity chosenTruck,
        ORSRoute route,
        TransportRecommendation truckSuggestion,
        LocalDate availabilityDeadline,
        double totalFuel,
        CreateTransportRequest request
) {
}
