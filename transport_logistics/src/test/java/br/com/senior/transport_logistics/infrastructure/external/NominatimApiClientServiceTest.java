package br.com.senior.transport_logistics.infrastructure.external;

import br.com.senior.transport_logistics.infrastructure.dto.NominationDTO.CoordinatesDTO;
import br.com.senior.transport_logistics.infrastructure.exception.external.ErrorForRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NominatimApiClientServiceTest {

    @Mock
    private NominatimApiClientService nominatimService;

    @Test
    void getCoordinates_Success() {

        String address = "Rua XV de Novembro, Blumenau";
        CoordinatesDTO expectedCoordinates = new CoordinatesDTO(-26.9205075, -49.0645019);

        when(nominatimService.getCoordinates(address)).thenReturn(expectedCoordinates);

        CoordinatesDTO actualCoordinates = nominatimService.getCoordinates(address);

        assertNotNull(actualCoordinates);
        assertEquals(expectedCoordinates.latitude(), actualCoordinates.latitude());
        assertEquals(expectedCoordinates.longitude(), actualCoordinates.longitude());
    }

    @Test
    void getCoordinates_AddressNotFound() {

        String nonExistentAddress = "Rua Inexistente 123, Nenhures";

        when(nominatimService.getCoordinates(nonExistentAddress))
                .thenThrow(new ErrorForRequest("Endereço não encontrado."));

        ErrorForRequest exception = assertThrows(ErrorForRequest.class, () -> {
            nominatimService.getCoordinates(nonExistentAddress);
        });

        assertEquals("Endereço não encontrado.", exception.getMessage());
    }

    @Test
    void getCoordinates_ApiError() {

        String addressWithApiError = "Endereço que causa erro na API";

        when(nominatimService.getCoordinates(addressWithApiError))
                .thenThrow(new ErrorForRequest("Erro ao buscar as coordenadas: Erro de conexão"));

        ErrorForRequest exception = assertThrows(ErrorForRequest.class, () -> {
            nominatimService.getCoordinates(addressWithApiError);
        });

        assertEquals("Erro ao buscar as coordenadas: Erro de conexão", exception.getMessage());
    }
}

