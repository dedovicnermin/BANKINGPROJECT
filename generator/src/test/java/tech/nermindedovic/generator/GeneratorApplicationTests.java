package tech.nermindedovic.generator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;



@Slf4j
class GeneratorApplicationTests {

    GeneratorApplication generatorApplication = new GeneratorApplication();
    int acb = GeneratorApplication.accountsPerBank;

    @Test
    void populateAccountsCorrectlyPopulates() {

        long[][] accounts = generatorApplication.populateAccounts();
        Stream.of(accounts).forEach(elem -> log.info(Arrays.toString(elem)));
        assertThat(accounts.length).isEqualTo(acb * 2);

        assertThat(Stream.of(accounts).filter(arr -> arr[1] == 111).count()).isEqualTo(acb);
        assertThat(Stream.of(accounts).filter(arr -> arr[1] == 222).count()).isEqualTo(acb);
        assertThat(Stream.of(accounts).filter(arr -> arr[0] <= 0 || arr[0] > acb).count()).isZero();

    }
}
