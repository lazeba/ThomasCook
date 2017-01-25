package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class ProcessingService {

    static final Logger LOG = LoggerFactory.getLogger(ProcessingService.class);

    static final DateFormat BOOKING_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static final DateFormat SAPBW_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    Config config;

    @Autowired
    ObjectMapper objectMapper;

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
                        (request, responder) -> {
                            JsonNode booking = request.path("booking");
                            String bookingNumber = booking.path("identifier").path("bookingNumber").asText();

                            LOG.info("Booking processing started: {}", bookingNumber);
                            LOG.debug("Message: {}", request);

                            Map bookingInfo = bookingInfoService.getBookingByKey(buildBookingKey(booking));
                            JsonNode payload = objectMapper.createObjectNode().set("booking", enrichmentService.enrichBooking(booking, bookingInfo));

                            msbContext.getObjectFactory()
                                    .createRequesterForFireAndForget(toNamespace, requestOptions)
                                    .publish(payload);

                            LOG.info("Booking processed: {}", bookingNumber);
                        },
                        (exception, message) -> {
                            LOG.error("Error processing message {}", message);
                        },
                        ObjectNode.class)
                .listen();
        LOG.info("customer-sapbw-enrich-canonical-booking-microservice is listening namespace '{}' with routing key '{}'", fromNamespace, routingKey);
    }

    private String buildBookingKey(JsonNode bookingNode) throws ParseException {
        String bookingNumber = bookingNode.path("identifier").path("bookingNumber").asText();
        Date bookingDate = BOOKING_DATE_FORMAT.parse(bookingNode.path("general").path("bookingDate").asText());
        String sourceSystemCode = bookingNode.path("general").path("toCode").asText();

        return bookingNumber + SAPBW_DATE_FORMAT.format(bookingDate) + normalizeSourceSystemCode(sourceSystemCode);
    }

    private String normalizeSourceSystemCode(String sourceSystemCode) {
        return sourceSystemCode.startsWith("0") ? sourceSystemCode.substring(1) : sourceSystemCode;
    }
}
