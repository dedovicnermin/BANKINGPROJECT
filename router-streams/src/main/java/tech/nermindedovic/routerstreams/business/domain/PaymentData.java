package tech.nermindedovic.routerstreams.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;


import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentData {
    private Long messageId;
    private BigDecimal amount;
    private Debtor debtorAccount;
    private Creditor creditorAccount;
    private String transferMessageXml;
}
