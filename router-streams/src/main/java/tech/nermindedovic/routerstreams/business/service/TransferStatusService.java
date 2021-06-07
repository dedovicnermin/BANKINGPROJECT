package tech.nermindedovic.routerstreams.business.service;

import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tech.nermindedovic.routerstreams.utils.RouterTopicNames;

@RestController
public class TransferStatusService {

    private final InteractiveQueryService iqService;

    public TransferStatusService(final InteractiveQueryService iqService) {
        this.iqService = iqService;
    }

    @GetMapping("transfer/{key}")
    public String getStatus(@PathVariable final String key) {
        final ReadOnlyKeyValueStore<String, String> store = iqService.getQueryableStore(RouterTopicNames.TRANSFER_STATUS_STORE, QueryableStoreTypes.keyValueStore());
        String s = store.get(key);
        if (s == null || s.isEmpty()) {
            return String.format("No records with ID (%s)", key);
        }
        return s;
    }
}
