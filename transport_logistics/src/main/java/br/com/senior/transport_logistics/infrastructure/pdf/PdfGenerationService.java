package br.com.senior.transport_logistics.infrastructure.pdf;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.product.ProductEntity;
import br.com.senior.transport_logistics.domain.shipment.ShipmentEntity;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGenerationService {

    public byte[] generateTransportManifestPdf(TransportEntity transport) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            PdfFont bold = PdfFontFactory.createFont("Helvetica-Bold");

            document.add(new Paragraph("Confirmação de transporte")
                    .setFont(bold).setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("ID do Transporte: " + transport.getId())
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth();

            mainTable.addCell(createAddressCell("ORIGEM", transport.getOriginHub(), bold));
            mainTable.addCell(createAddressCell("DESTINO", transport.getDestinationHub(), bold));

            mainTable.addCell(createShipmentDetailsCell(transport.getShipment(), bold).setPaddingTop(15));

            mainTable.addCell(createTruckAndDriverCell(transport.getTruck(), transport.getDriver(), bold).setPaddingTop(15));

            document.add(mainTable);

            document.add(new Paragraph("\n\n\n"));
            document.add(new Paragraph("_________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Assinatura do Motorista")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n\n\n"));
            document.add(new Paragraph("_________________________")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Assinatura do Fiscal")
                    .setTextAlignment(TextAlignment.CENTER));


            document.close();
            return baos.toByteArray();

        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
    }


    private Cell createAddressCell(String title, HubEntity hub, PdfFont boldFont) {
        Cell cell = new Cell().setPadding(5).setBorder(null);
        cell.add(new Paragraph(title).setFont(boldFont).setFontSize(12));
        cell.add(new Paragraph(hub.getName()));
        cell.add(new Paragraph(hub.getStreet() + ", " + hub.getNumber()));
        cell.add(new Paragraph(hub.getCity() + " - " + hub.getState() + ", " + hub.getCep()));
        cell.add(new Paragraph("CNPJ: " + hub.getCnpj()));
        return cell;
    }

    private Cell createShipmentDetailsCell(ShipmentEntity shipment, PdfFont boldFont) {
        Cell cell = new Cell(1, 2).setPadding(5).setBorder(null);
        cell.add(new Paragraph("DETALHES DA CARGA").setFont(boldFont).setFontSize(12));

        ProductEntity product = shipment.getProduct();
        cell.add(new Paragraph("Produto: " + product.getName()));
        cell.add(new Paragraph("Categoria: " + product.getCategory()));
        cell.add(new Paragraph("Peso Total: " + shipment.getWeight() + " kg"));
        cell.add(new Paragraph("Quantidade: " + shipment.getQuantity()));
        cell.add(new Paragraph("Notas: " + (shipment.getNotes() != null ? shipment.getNotes() : "N/A")));
        cell.add(new Paragraph("Perigoso: " + (shipment.isHazardous() ? "Sim" : "Não")));
        return cell;
    }

    private Cell createTruckAndDriverCell(TruckEntity truck, EmployeeEntity driver, PdfFont boldFont) {
        Cell cell = new Cell(1, 2).setPadding(5).setBorder(null);
        cell.add(new Paragraph("VEÍCULO E MOTORISTA").setFont(boldFont).setFontSize(12));

        cell.add(new Paragraph("Caminhão: " + truck.getModel() + " (" + truck.getCode() + ")"));
        cell.add(new Paragraph("Motorista: " + driver.getName()));
        cell.add(new Paragraph("CNH: " + driver.getCnh()));
        return cell;
    }
}
