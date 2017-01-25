package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import io.github.tcdl.msb.api.MessageTemplate;
import io.github.tcdl.msb.api.MsbContext;
import io.github.tcdl.msb.api.RequestOptions;
import io.github.tcdl.msb.api.ResponderOptions;
import io.github.tcdl.msb.config.ConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class ProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingService.class);

    @Autowired
    Config config;

    @Autowired
    MsbContext msbContext;

    @Autowired
    MessageTemplate messageTemplate;

    @Autowired
    SapBwBookingInfoService bookingInfoService;

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
                .createResponderServer(fromNamespace, new ResponderOptions.Builder()
                                .withMessageTemplate(messageTemplate)
                                .withBindingKeys(Sets.newHashSet(routingKey))
                                .build(),
                        (booking, responder) -> {

                            String bookingNumber = booking.path("booking").path("identifier").path("bookingNumber").asText();
                            LOG.info("Booking processing started: {}", bookingNumber);
                            LOG.debug("Message: {}", booking);

                            Map bookingInfo = bookingInfoService.getBookingByKey(buildBookingKey(booking));
                            JsonNode payload = enrichmentService.enrichBooking(booking, bookingInfo);

                            msbContext.getObjectFactory().createRequester(toNamespace, requestOptions)
                                    .publish(payload);

                            LOG.info("Booking processed: {}", bookingNumber);

                        }, ObjectNode.class)
                .listen();
        LOG.info("customer-sapbw-enrich-canonical-booking-microservice is listening namespace '{}' with routing key '{}'", fromNamespace, routingKey);
    }

    private String buildBookingKey(JsonNode bookingNode) {
        JsonNode booking = bookingNode.path("booking");
        String bookingNumber = booking.path("identifier").path("bookingNumber").asText();
        String bookingDate = booking.path("general").path("bookingDate").asText();
        String sourceSystemCode = booking.path("general").path("toCode").asText();

        return bookingNumber + bookingDate + normalizeSourceSystemCode(sourceSystemCode);
    }

    private String normalizeSourceSystemCode(String sourceSystemCode) {
        return sourceSystemCode.startsWith("0") ? sourceSystemCode.substring(1) : sourceSystemCode;
    }
}
