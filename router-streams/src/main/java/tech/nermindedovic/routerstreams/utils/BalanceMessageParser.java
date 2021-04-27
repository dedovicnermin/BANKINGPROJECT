package tech.nermindedovic.routerstreams.utils;


import lombok.extern.slf4j.Slf4j;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import tech.nermindedovic.routerstreams.exception.InvalidRoutingNumberException;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class BalanceMessageParser {
    private static final String ROUTING_NUMBER      = "routingNumber";
    static final Set<Long> validRoutes = Stream.of(111L, 222L).collect(Collectors.toSet());
    static final SAXBuilder builder = new SAXBuilder();

    private final Long route;
    public BalanceMessageParser(String xml) throws JDOMException, IOException, InvalidRoutingNumberException {
        Document messageDocument = builder.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            final Element root = messageDocument.getRootElement();
            route = Long.parseLong(root.getChild(ROUTING_NUMBER).getValue());
            if (!validRoutes.contains(route)) throw new InvalidRoutingNumberException(route + " is not a valid route.");
    }

    public Long getRoute() {return this.route;}



}
