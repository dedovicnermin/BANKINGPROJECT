package tech.nermindedovic.routerstreams.utils;

import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import tech.nermindedovic.routerstreams.business.domain.Account;
import tech.nermindedovic.routerstreams.business.domain.PaymentParty;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;


@Slf4j
public class TransferMessageParser {

    private static final String DEBTOR           = "debtor";
    private static final String CREDITOR         = "creditor";
    private static final String ACCOUNT_NUMBER   = "accountNumber";
    private static final String ROUTING_NUMBER   = "routingNumber";
    private static final String MESSAGE_ID       = "messageId";
    private static final String AMOUNT           = "amount";

    static final SAXBuilder builder = new SAXBuilder();
    private PaymentParty paymentParty;



    public TransferMessageParser(String xml) throws JDOMException, IOException {
        Document messageDocument = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        Element root = messageDocument.getRootElement();
        paymentParty = createPaymentParty(root);

    }

    public PaymentParty getPaymentParty() {
        return paymentParty;
    }


    private PaymentParty createPaymentParty(Element root) {
        paymentParty = new PaymentParty();
        Element debtor = root.getChild(DEBTOR);
        Element creditor = root.getChild(CREDITOR);
        Element messageId = root.getChild(MESSAGE_ID);
        Element amount = root.getChild(AMOUNT);
        paymentParty.setMessageId(Long.parseLong(messageId.getValue()));
        paymentParty.setDebtorAccount(retrieveAccount(debtor));
        paymentParty.setCreditorAccount(retrieveAccount(creditor));
        paymentParty.setAmount(new BigDecimal(amount.getValue()));
        return this.paymentParty;
    }



    public Long retrieveMessageId() {
        return paymentParty.getMessageId();
    }

    public int countRoutingNumbersPresent() {
        return getPaymentParty().getRoutingSet().size();
    }

    public Long getMatchingRoute() {
        return this.getPaymentParty().getDebtorAccount().getRoutingNumber();
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
