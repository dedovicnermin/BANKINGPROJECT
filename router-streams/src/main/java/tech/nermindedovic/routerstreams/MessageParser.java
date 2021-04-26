package tech.nermindedovic.routerstreams;

import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.PaymentParty;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;



@Slf4j
public class MessageParser {

    private static final String DEBTOR           = "debtor";
    private static final String CREDITOR         = "creditor";
    private static final String ACCOUNT_NUMBER   = "accountNumber";
    private static final String ROUTING_NUMBER   = "routingNumber";
    private static final String MESSAGE_ID       = "messageId";
    private static final String AMOUNT           = "amount";

    private Optional<Document> messageDocument;
    private PaymentParty paymentParty;


    public MessageParser(String xml) {
        try {
            SAXBuilder builder = new SAXBuilder();
            this.messageDocument = Optional.of(builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (JDOMException | IOException e) {
            log.error(e.getMessage());
            this.messageDocument = Optional.empty();
        }
    }



    public PaymentParty getPaymentParty() {
        if (paymentParty == null) {
            paymentParty = new PaymentParty();
            messageDocument.ifPresent(document -> {
                Element root = document.getRootElement();
                Element debtor = root.getChild(DEBTOR);
                Element creditor = root.getChild(CREDITOR);
                Element messageId = root.getChild(MESSAGE_ID);
                Element amount = root.getChild(AMOUNT);
                paymentParty.setMessageId(Long.parseLong(messageId.getValue()));
                paymentParty.setDebtorAccount(retrieveAccount(debtor));
                paymentParty.setCreditorAccount(retrieveAccount(creditor));
                paymentParty.setAmount(new BigDecimal(amount.getValue()));
            });
        }
        return this.paymentParty;

    }

    public Long retrieveMessageId() {
        return Long.parseLong(messageDocument.get().getRootElement().getChild(MESSAGE_ID).getValue());
    }



    public int countRoutingNumbersPresent() {
        if (!messageDocument.isPresent()) return 0;
        return getPaymentParty().getRoutingSet().size();
    }


    public Long getMatchingRoute() {
        return this.getPaymentParty().getDebtorAccount().getRoutingNumber();
    }

    public BigDecimal getAmount() {
        return this.getPaymentParty().getAmount();
    }


    private Account retrieveAccount(Element element) {
        Account account = new Account();
        Element accountNumber = element.getChild(ACCOUNT_NUMBER);
        Element routingNumber = element.getChild(ROUTING_NUMBER);
        account.setAccountNumber(Long.parseLong(accountNumber.getValue()));
        account.setRoutingNumber(Long.parseLong(routingNumber.getValue()));
        return account;
    }






}
