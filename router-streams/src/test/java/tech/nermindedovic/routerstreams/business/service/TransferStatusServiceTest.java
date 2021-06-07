package tech.nermindedovic.routerstreams.business.service;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferStatusServiceTest {

    @Mock
    InteractiveQueryService service = mock(InteractiveQueryService.class);
    @Mock
    ReadOnlyKeyValueStore store;

    @Test
    @SuppressWarnings("unchecked")
    void serviceWillReturnStatusString() {
        String inputKey = "12344";
        when(service.getQueryableStore(anyString(), any())).thenReturn(store);
        when(store.get(inputKey)).thenReturn("PERSISTED");

        TransferStatusService statusService = new TransferStatusService(service);
        assertThat(statusService.getStatus(inputKey)).isEqualTo("PERSISTED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void serviceWillReturnErrorMessageWhenCantFind() {
        String inputKey = "0";
        when(service.getQueryableStore(anyString(), any())).thenReturn(store);
        when(store.get(inputKey)).thenReturn(null);
        TransferStatusService statusService = new TransferStatusService(service);
        assertThat(statusService.getStatus(inputKey)).isEqualTo("No records with ID (0)");
    }

}