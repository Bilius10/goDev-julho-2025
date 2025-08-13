package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.ResponseForGemini;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.request.RestrictionsRecord;
import br.com.senior.transport_logistics.infrastructure.dto.OpenRouteDTO.response.*;
import br.com.senior.transport_logistics.infrastructure.exception.external.ErrorForRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenRouteApiClientServiceTest {

    @InjectMocks
    private OpenRouteApiClientService openRouteService;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void obterDistancia_Success() {

        CoordinatesDTO start = new CoordinatesDTO(-26.91, -49.06);
        CoordinatesDTO finish = new CoordinatesDTO(-27.59, -48.54);
        RestrictionsRecord restrictions = new RestrictionsRecord(1.0, 1.0, 1.0, false);

        SummaryRecord summary = new SummaryRecord(50000, 3600);
        StepRecord step = new StepRecord(summary.distance(), summary.duration(),
                1, "teste", "teste", List.of(1), 1);
        SegmentRecord segment = new SegmentRecord(step.distance(), step.duration(),List.of(step));
        RouteRecord route = new RouteRecord(summary, List.of(segment));
        OrsResponse mockResponse = new OrsResponse(List.of(route));

        ResponseEntity<OrsResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(OrsResponse.class)
        )).thenReturn(responseEntity);

        // Ação
        ResponseForGemini result = openRouteService.obterDistancia(start, finish, restrictions);

        // Verificação
        assertNotNull(result);
        assertEquals(50.0, result.distance());
        assertEquals(3600, result.duration());
        assertFalse(result.steps().isEmpty());
    }

    @Test
    void obterDistancia_NoRouteFound() {

        CoordinatesDTO start = new CoordinatesDTO(0, 0);
        CoordinatesDTO finish = new CoordinatesDTO(1, 1);
        RestrictionsRecord restrictions = new RestrictionsRecord(1.0, 1.0, 1.0, false);

        OrsResponse mockResponse = new OrsResponse(Collections.emptyList());
        ResponseEntity<OrsResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(OrsResponse.class)
        )).thenReturn(responseEntity);

        ErrorForRequest exception = assertThrows(ErrorForRequest.class, () -> {
            openRouteService.obterDistancia(start, finish, restrictions);
        });

        assertEquals("Nenhuma rota encontrada entre os pontos informados.", exception.getMessage());
    }

    @Test
    void obterDistancia_ApiError() {

        CoordinatesDTO start = new CoordinatesDTO(0, 0);
        CoordinatesDTO finish = new CoordinatesDTO(1, 1);
        RestrictionsRecord restrictions = new RestrictionsRecord(1.0, 1.0, 1.0, false);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(OrsResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ErrorForRequest exception = assertThrows(ErrorForRequest.class, () -> {
            openRouteService.obterDistancia(start, finish, restrictions);
        });

        assertTrue(exception.getMessage().startsWith("Problema com a requisição para a API de rotas:"));
    }
}