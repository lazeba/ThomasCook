package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import io.github.tcdl.msb.api.*;
import io.github.tcdl.msb.config.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Component
public class ProcessingService {

    static final Logger LOG = LoggerFactory.getLogger(ProcessingService.class);

    @Autowired
    Config config;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MsbContext msbContext;

    @Autowired
    MessageTemplate messageTemplate;

    @Autowired
    SourceCodeResolver sourceCodeResolver;

    @Autowired
    SapBwBookingInfoService bookingInfoService;

    @Autowired
    SapBwCustomerInfoService customerInfoService;

    @Autowired
    EnrichmentService enrichmentService;

    @PostConstruct
    public void start() {

        final String routingKey = ConfigurationUtil.getString(config, "namespace.routingKey");
        final String fromNamespace = ConfigurationUtil.getString(config, "namespace.from");
        final String toNamespace = ConfigurationUtil.getString(config, "namespace.to");

        final RequestOptions requestOptions = new RequestOptions.Builder()
                .withMessageTemplate(messageTemplate)
                .withWaitForResponses(0)
                .build();

        msbContext.getObjectFactory()
                .createResponderServer(fromNamespace, new AmqpResponderOptions.Builder()
                                .withExchangeType(ExchangeType.TOPIC)
                                .withMessageTemplate(messageTemplate)
                                .withBindingKeys(Sets.newHashSet(routingKey))

                                .build(),
                        (request, responder) -> {
                            JsonNode booking = request.path("booking");

                            String bookingNumber = booking.path("identifier").path("bookingNumber").asText();
                            String tourOperatorCode = booking.path("general").path("toCode").asText();
                            Optional<String> sourceSystemCode = sourceCodeResolver.resolve(tourOperatorCode);
                            LocalDate bookingDate = LocalDate.parse(booking.path("general").path("bookingDate").asText(), ISO_LOCAL_DATE);

                            if (!sourceSystemCode.isPresent()) {
                                LOG.error("Couldn't resolve source system code by tour operator code {} for booking {}", tourOperatorCode, bookingNumber);
                                return;
                            }

                            LOG.info("Booking processing started: {}", bookingNumber);
                            LOG.debug("Message: {}", request);

                            Map bookingInfo = bookingInfoService.getBookingByKey(bookingNumber, bookingDate, sourceSystemCode.get());

//                            Map<String, Object> customerByKey = customerInfoService.getCustomerByKey("070184007", "0"); //todo - delete while sapbw customer happy flow implemented
                            JsonNode payload = objectMapper.createObjectNode().set("booking", enrichmentService.enrichBooking(booking, bookingInfo));

                            msbContext.getObjectFactory()
                                    .createRequesterForFireAndForget(toNamespace, requestOptions)
                                    .publish(payload);

                            LOG.info("Booking processed: {}", bookingNumber);
                        },
                        (exception, message) -> {
                            LOG.error("Error processing message {} {}", message, exception.getMessage());
                        },
                        ObjectNode.class)
                .listen();
        LOG.info("sapbw-enrich-booking microservice is listening namespace '{}' with routing key '{}'", fromNamespace, routingKey);
    }
}
