package br.com.senior.transport_logistics.infrastructure.email;

import br.com.senior.transport_logistics.domain.employee.EmployeeEntity;
import br.com.senior.transport_logistics.domain.hub.HubEntity;
import br.com.senior.transport_logistics.domain.transport.TransportEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringMailSenderServiceTest {

    @InjectMocks
    private SpringMailSenderService mailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(mailService, "emailFrom", "nao-responda@logitrack.com");
    }

    @Test
    @DisplayName("Deve enviar e-mail de boas-vindas com os dados corretos")
    void sendWelcomeEmail_ShouldSendCorrectEmail() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome-email.html"), any(Context.class))).thenReturn("<html></html>");
        EmployeeEntity employee = new EmployeeEntity();
        employee.setName("João da Silva");
        employee.setEmail("joao.silva@example.com");

        mailService.sendWelcomeEmail(employee);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("welcome-email.html"), contextCaptor.capture());
        assertEquals("João da Silva", contextCaptor.getValue().getVariable("nome"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar e-mail de confirmação de transporte com os dados corretos")
    void sendConfirmTransportEmail_ShouldSendEmailWithPdf() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("confirm-transport.html"), any(Context.class))).thenReturn("<html></html>");
        EmployeeEntity driver = new EmployeeEntity();
        driver.setName("Maria Oliveira");
        driver.setEmail("maria.oliveira@example.com");
        TransportEntity transport = new TransportEntity();
        transport.setDriver(driver);
        byte[] pdf = "test-pdf-content".getBytes();

        mailService.sendConfirmTransportEmail(transport, pdf);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("confirm-transport.html"), contextCaptor.capture());
        assertEquals(driver, contextCaptor.getValue().getVariable("driver"));
        assertEquals(transport, contextCaptor.getValue().getVariable("transport"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar e-mail de relatório mensal com os dados corretos")
    void sendMonthReportEmail_WhenTransportsExist_ShouldSendEmail() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("month-report.html"), any(Context.class))).thenReturn("<html></html>");
        EmployeeEntity manager = new EmployeeEntity();
        manager.setEmail("manager@example.com");
        HubEntity hub = new HubEntity();
        hub.setName("Hub Central");
        TransportEntity transport = new TransportEntity();
        transport.setOriginHub(hub);
        transport.setExitDay(java.time.LocalDate.of(2025, 8, 13));

        mailService.sendMonthReportEmail(manager, List.of(transport), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), 150.5);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("month-report.html"), contextCaptor.capture());
        assertEquals("Hub Central", contextCaptor.getValue().getVariable("hubName"));
        assertEquals("agosto de 2025", contextCaptor.getValue().getVariable("reportMonth"));
        assertEquals(1, contextCaptor.getValue().getVariable("totalTransports"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar gerar relatório mensal sem transportes")
    void sendMonthReportEmail_WhenNoTransports_ShouldThrowException() {

        EmployeeEntity manager = new EmployeeEntity();
        manager.setEmail("manager@example.com");
        List<TransportEntity> emptyList = Collections.emptyList();

        assertThrows(IndexOutOfBoundsException.class, () -> {
            mailService.sendMonthReportEmail(manager, emptyList, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), 0.0);
        });
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve enviar e-mail de agenda semanal se não houver transportes")
    void sendWeeklyScheduleEmail_WhenNoTransports_ShouldNotSendEmail() {

        List<TransportEntity> emptyList = Collections.emptyList();

        mailService.sendWeeklyScheduleEmail(emptyList);

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(templateEngine, never()).process(anyString(), any(Context.class));
    }
}