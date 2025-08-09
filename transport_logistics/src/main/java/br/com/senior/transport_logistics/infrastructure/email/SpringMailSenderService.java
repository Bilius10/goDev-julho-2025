package br.com.senior.transport_logistics.infrastructure.email;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import br.com.senior.transport_logistics.domain.transport.enums.TransportStatus;
import br.com.senior.transport_logistics.domain.truck.TruckEntity;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;


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
                Map.of(
                        "driver", transport.getDriver(),
                        "transport", transport
                )
        );
    }

    public void sendUpdatePasswordEmail(EmployeeEntity employee){
        sendEmailWithTemplate(
                employee.getEmail(),
                "Redefinir senha padrão",
                "update-password.html",
                 Map.of("nome", employee.getName())
        );
    }

    public void sendMonthReportEmail(EmployeeEntity manager, List<TransportEntity> transports, List<EmployeeEntity> drivers,
                                     List<TruckEntity> trucks, Map<String, Double> fuelByTruck, double totalDistance) {

        HubEntity hub = transports.get(0).getOriginHub();

        LocalDate reportDate = transports.get(0).getExitDay();
        String reportMonth = reportDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
                + " de " + reportDate.getYear();

        double totalFuelConsumed = transports.stream()
                .filter(transport -> transport.getStatus() == TransportStatus.DELIVERED)
                .mapToDouble(TransportEntity::getFuelConsumption)
                .sum();

        Map<String, Object> variables = new HashMap<>();
        variables.put("hubName", hub.getName());
        variables.put("reportMonth", reportMonth);
        variables.put("totalTransports", transports.size());
        variables.put("totalDistance", totalDistance/1000);
        variables.put("totalFuelConsumed", totalFuelConsumed);
        variables.put("transports", transports);
        variables.put("drivers", drivers);
        variables.put("trucks", trucks);
        variables.put("fuelConsumptionByTruck", fuelByTruck);

        String subject = String.format("Relatório Mensal de Operações - %s - %s", hub.getName(), reportMonth);
        String templateName = "month-report.html";

        sendEmailWithTemplate(
                manager.getEmail(),
                subject,
                templateName,
                variables
        );
    }

    public void sendWeeklyScheduleEmail(List<TransportEntity> transportEntities) {

        if (transportEntities == null || transportEntities.isEmpty()) {
            log.info("A lista de transportes está vazia. Nenhum e-mail de resumo semanal será enviado.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        LocalDate nextSevenDays = today.plusDays(7);
        String dateRange = String.format("de %s a %s", today.format(formatter), nextSevenDays.format(formatter));

        sendEmailWithTemplate(
                transportEntities.get(0).getDriver().getEmail(),
                "Seu Resumo Semanal de Entregas - LogiTrack",
                "weekly-schedule.html",
                Map.of(
                        "driverName", transportEntities.get(0).getDriver().getName(),
                        "transports", transportEntities,
                        "dateRange", dateRange
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
