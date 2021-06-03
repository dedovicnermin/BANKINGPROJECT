package tech.nermindedovic.routerstreams.utils;

import lombok.extern.slf4j.Slf4j;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Component;
import tech.nermindedovic.library.pojos.Creditor;
import tech.nermindedovic.library.pojos.Debtor;
import tech.nermindedovic.routerstreams.business.domain.PaymentData;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;


@Slf4j
@Component
public class TransferMessageParser {

    private static final String DEBTOR           = "debtor";
    private static final String CREDITOR         = "creditor";
    private static final String ACCOUNT_NUMBER   = "accountNumber";
    private static final String ROUTING_NUMBER   = "routingNumber";
    private static final String MESSAGE_ID       = "messageId";
    private static final String AMOUNT           = "amount";

    final SAXBuilder builder;

    public TransferMessageParser(final SAXBuilder saxBuilder)  {
        this.builder = saxBuilder;
    }

    public PaymentData build(String xml)  {
        Document messageDocument;
        try {
            messageDocument = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            PaymentData paymentData = new PaymentData();
            paymentData.setTransferMessageXml(xml);
            return paymentData;
        }
        Element root = messageDocument.getRootElement();
        return createPaymentData(root);
    }




    private PaymentData createPaymentData(Element root) {
        PaymentData paymentData = new PaymentData();
        Element debtor = root.getChild(DEBTOR);
        Element creditor = root.getChild(CREDITOR);
        Element messageId = root.getChild(MESSAGE_ID);
        Element amount = root.getChild(AMOUNT);

        retrieveAccounts(paymentData, debtor, creditor);
        paymentData.setMessageId(Long.parseLong(messageId.getValue()));
        paymentData.setAmount(new BigDecimal(amount.getValue()));

        return paymentData;
    }



    private void retrieveAccounts(PaymentData paymentData, Element debtor, Element creditor) {
        paymentData.setDebtorAccount(retrieveDebtorAccount(debtor));
        paymentData.setCreditorAccount(retrieveCreditorAccount(creditor));
    }

    private Debtor retrieveDebtorAccount(Element debtor) {
        Element debtorAN = debtor.getChild(ACCOUNT_NUMBER);
        Element debtorRN = debtor.getChild(ROUTING_NUMBER);
        return new Debtor(Long.parseLong(debtorAN.getValue()), Long.parseLong(debtorRN.getValue()));
    }

    private Creditor retrieveCreditorAccount(Element creditor) {
        Element creditorAN = creditor.getChild(ACCOUNT_NUMBER);
        Element creditorRN = creditor.getChild(ROUTING_NUMBER);
        return new Creditor(Long.parseLong(creditorAN.getValue()), Long.parseLong(creditorRN.getValue()));
    }








}
