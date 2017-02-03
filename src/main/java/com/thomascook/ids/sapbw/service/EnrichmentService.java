package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.tcdl.msb.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnrichmentService {

    private static final Logger LOG = LoggerFactory.getLogger(EnrichmentService.class);

    public static final TextNode EMPTY_VALUE = TextNode.valueOf("");

    private final ObjectMapper objectMapper;

    public EnrichmentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getKey(JsonNode bookingNode) {
        JsonNode booking = bookingNode.path("booking");
        String bookingNumber = booking.path("identifier").path("bookingNumber").asText();
        String bookingDate = booking.path("general").path("bookingDate").asText();
        String sourceSystemCode = booking.path("general").path("toCode").asText();

        return bookingNumber + bookingDate + normalizeSourceSystemCode(sourceSystemCode);
    }

    private String normalizeSourceSystemCode(String sourceSystemCode) {
        return sourceSystemCode.startsWith("0") ? sourceSystemCode.substring(1) : sourceSystemCode;
    }

    public ObjectNode enrichBooking(JsonNode booking, Map bookingInfo) {
        ObjectNode node = booking.deepCopy();
        mapBookingInfo(node, Utils.convert(bookingInfo, ObjectNode.class, objectMapper));
        return node;
    }

    private void mapBookingInfo(ObjectNode bookingNode, ObjectNode bookingInfo) {
        ObjectNode generalNode = (ObjectNode)bookingNode.path("general");

        // map from Key node
        JsonNode keyNode = bookingInfo.path("Key");
        bookingNode.set("sourceSystem", keyNode.path("SourcesystemTxt"));
        bookingNode.set("bookingNumber", keyNode.path("Bookingno"));

        // map from SalesBk node
        JsonNode salesBkNode = bookingInfo.path("SalesBk");
        bookingNode.set("businessArea", salesBkNode.path("BusinessareaTxt"));
        bookingNode.set("agent", objectMapper.createObjectNode().set("shopCode", salesBkNode.path("AgencyTxt")));
        generalNode.set("hasComplaint", complaintExists(salesBkNode.path("Complaintexists")));

        // map from MeasuresBk node
        JsonNode measuresBkNode = bookingInfo.path("MeasuresBk");
        generalNode.set("travelAmount", measuresBkNode.path("Ra"));
        generalNode.set("numberOfParticipants", measuresBkNode.path("Pax"));
        generalNode.set("numberOfAdults", measuresBkNode.path("Adults"));
        generalNode.set("numberOfChildren", measuresBkNode.path("Children"));
        generalNode.set("numberOfInfants", measuresBkNode.path("Infants"));
        generalNode.set("currency", measuresBkNode.path("Currency"));
        generalNode.set("duration", measuresBkNode.path("Duration"));

        // map from CustomerBk node
        JsonNode customerBkNode = bookingInfo.path("CustomerBk");
        generalNode.set("customerId", customerBkNode.path("Customerno"));

        ObjectNode bookerNode = objectMapper.createObjectNode();
        bookerNode.set("mobile", customerBkNode.path("Mobile"));
        bookerNode.set("bookerEmail", customerBkNode.path("Email"));
        bookerNode.set("phone", customerBkNode.path("Phone"));
        bookerNode.set("emergencyNumber", customerBkNode.path("Emergencyno"));
        bookingNode.set("booker", bookerNode);
    }

    private JsonNode complaintExists(JsonNode node) {
        if(node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        switch (node.asText()) {
            case "Y": {
                return BooleanNode.TRUE;
            }
            case "N": {
                return BooleanNode.FALSE;
            }
            default: {
                LOG.warn("Can't convert Complaintexists value {}", node);
                return EMPTY_VALUE;
            }
        }
    }
}
