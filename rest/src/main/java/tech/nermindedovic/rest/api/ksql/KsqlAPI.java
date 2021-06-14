package tech.nermindedovic.rest.api.ksql;



import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("ksql")
@Slf4j
public class KsqlAPI {


    private final KsqlService ksqlService;
    public KsqlAPI(final KsqlService service) {
        this.ksqlService = service;
    }


    /**
     * Will print all defined topics with partition count
     * @return formatted table of kafka topics
     */
    @GetMapping(value = "/topics")
    public String getKafkaTopics() {
        return ksqlService.retrieveTopics();
    }


    /**
     * Will return count of errors consumed by transferError consumer
     * @return response string
     */
    @GetMapping(value = "/error-count")
    public String getErrorCount() {
        return ksqlService.retrieveErrorCount();
    }


    /**
     * Will return string representing the amount of transfers processed by bank1 (111)
     * @return response
     */
    @GetMapping(value = "/bank1/persisted-count")
    public String getBank1PCount()  {
        return ksqlService.retrieveBank1PersistedCount();
    }


    /**
     * Will return string representing the amount of transfers processed by bank2 (222)
     * @return response
     */
    @GetMapping(value = "/bank2/persisted-count")
    public String getBank2PCount()  {
        return ksqlService.retrieveBank2PersistedCount();
    }






}
