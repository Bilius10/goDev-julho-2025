package br.com.senior.transport_logistics.infrastructure.email;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;


@Service
@Async
@RequiredArgsConstructor
public class SpringMailSenderService {

    private static final Logger log = LoggerFactory.getLogger(SpringMailSenderService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailFrom;

    public void sendWelcomeEmail(EmployeeEntity employee) {
        sendEmailWithTemplate(
                employee.getEmail(),
                String.format(" Seja muito bem-vindo(a), %s ,à equipe LogiTrack", employee.getName()),
                "welcome-email.html",
                Map.of("nome", employee.getName())
        );
    }

    public void sendConfirmTransportEmail(TransportEntity transport){
        sendEmailWithTemplate(
                transport.getDriver().getEmail(),
                String.format("Olá, %s voce possui uma nova entrega", transport.getDriver().getName()),
                "confirm-transport.html",
                Map.ofEntries(
                        Map.entry("driver.name", transport.getDriver().getName()),
                        Map.entry("transport.id", transport.getId()),
                        Map.entry("transport.originHub.name", transport.getOriginHub().getName()),
                        Map.entry("transport.destinationHub.name", transport.getDestinationHub().getName()),
                        Map.entry("transport.shipment.product.name", transport.getShipment().getProduct().getName()),
                        Map.entry("transport.shipment.quantity", transport.getShipment().getQuantity()),
                        Map.entry("transport.shipment.weight", transport.getShipment().getWeight()),
                        Map.entry("transport.shipment.is_hazardous", transport.getShipment().isHazardous()),
                        Map.entry("transport.shipment.notes", transport.getShipment().getNotes()),
                        Map.entry("transport.truck.model", transport.getTruck().getModel()),
                        Map.entry("transport.truck.code", transport.getTruck().getCode()),
                        Map.entry("transport.exit_day", transport.getExitDay()),
                        Map.entry("transport.expected_arrival_day", transport.getExpectedArrivalDay())
                )
        );
    }

    private void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("E-mail com template '{}' enviado com sucesso para {}", templateName, to);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail com template '{}' para {}: {}", templateName, to, e.getMessage());
        }
    }

}
