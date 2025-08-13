package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.ViaCepDTO.AddresDTO;
import br.com.senior.transport_logistics.infrastructure.exception.external.ErrorForRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCepApiCilentServiceTest {

    @InjectMocks
    private ViaCepApiCilentService viaCepService;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void getAddress_Success() {

        String cep = "89010-000";
        String expectedUrl = "https://viacep.com.br/ws/89010-000/json/";
        AddresDTO expectedAddress = new AddresDTO("Rua XV de Novembro", "Centro", "Blumenau", "SC");

        when(restTemplate.getForObject(eq(expectedUrl), eq(AddresDTO.class))).thenReturn(expectedAddress);

        AddresDTO actualAddress = viaCepService.getAddress(cep);

        assertNotNull(actualAddress);
        assertEquals(expectedAddress.logradouro(), actualAddress.logradouro());
        assertEquals(expectedAddress.bairro(), actualAddress.bairro());
        assertEquals(expectedAddress.localidade(), actualAddress.localidade());
        assertEquals(expectedAddress.uf(), actualAddress.uf());
    }

    @Test
    void getAddress_Error() {

        String cep = "99999-999";
        String expectedUrl = "https://viacep.com.br/ws/99999-999/json/";


        when(restTemplate.getForObject(eq(expectedUrl), eq(AddresDTO.class)))
                .thenThrow(new RestClientException("Erro ao acessar API"));

        ErrorForRequest exception = assertThrows(ErrorForRequest.class, () -> {
            viaCepService.getAddress(cep);
        });

        assertEquals("Não foi possível consultar o CEP: " + cep, exception.getMessage());
    }
}