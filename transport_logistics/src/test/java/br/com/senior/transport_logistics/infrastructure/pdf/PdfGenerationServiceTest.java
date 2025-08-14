package br.com.senior.transport_logistics.infrastructure.pdf;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @InjectMocks
    private PdfGenerationService pdfService;

    @Test
    @DisplayName("Deve gerar o PDF do manifesto com sucesso quando os dados estão completos")
    void generateTransportManifestPdf_Success() {

        TransportEntity transport = createCompleteTransportEntity();

        byte[] pdfBytes = pdfService.generateTransportManifestPdf(transport);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "O array de bytes do PDF não deveria estar vazio.");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao tentar gerar PDF com dados essenciais nulos")
    void generateTransportManifestPdf_NullData_ShouldThrowException() {

        TransportEntity transportWithNullData = new TransportEntity();

        assertThrows(RuntimeException.class, () -> {
            pdfService.generateTransportManifestPdf(transportWithNullData);
        });
    }

    private TransportEntity createCompleteTransportEntity() {
        HubEntity originHub = new HubEntity();
        originHub.setName("Centro de Distribuição Navegantes");
        originHub.setStreet("Av. Portuária");
        originHub.setNumber("123");
        originHub.setCity("Navegantes");
        originHub.setState("SC");
        originHub.setCep("88370-000");
        originHub.setCnpj("11.222.333/0001-01");

        HubEntity destinationHub = new HubEntity();
        destinationHub.setName("Filial Blumenau");
        destinationHub.setStreet("Rua XV de Novembro");
        destinationHub.setNumber("456");
        destinationHub.setCity("Blumenau");
        destinationHub.setState("SC");
        destinationHub.setCep("89010-000");
        destinationHub.setCnpj("11.222.333/0002-02");

        ProductEntity product = new ProductEntity();
        product.setName("Smart TV 55 Polegadas");
        product.setCategory(ProductCategory.AUTOMOTIVE);

        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setProduct(product);
        shipment.setWeight(25.5);
        shipment.setQuantity(50);
        shipment.setNotes("Manusear com cuidado, frágil.");
        shipment.setHazardous(false);

        TruckEntity truck = new TruckEntity();
        truck.setModel("Volvo FH 540");
        truck.setCode("TRK-001");

        EmployeeEntity driver = new EmployeeEntity();
        driver.setName("Carlos Alberto");
        driver.setCnh("12345678901");

        TransportEntity transport = new TransportEntity();
        transport.setId(1L);
        transport.setOriginHub(originHub);
        transport.setDestinationHub(destinationHub);
        transport.setShipment(shipment);
        transport.setTruck(truck);
        transport.setDriver(driver);

        return transport;
    }
}