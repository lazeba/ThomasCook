package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.tcdl.msb.support.Utils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnrichmentService {

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

    private void mapBookingInfo(ObjectNode booking, ObjectNode bookingInfo) {
        // map from Key node
        JsonNode keyNode = bookingInfo.path("Key");
        booking.set("sourceSystem", keyNode.path("Sourcesystem"));
        booking.set("bookingNumber", keyNode.path("Bookingno"));

        // map from SalesBk node
        JsonNode salesBkNode = bookingInfo.path("SalesBk");
        booking.set("businessArea", salesBkNode.path("BusinessareaTxt"));
        booking.set("agent", objectMapper.createObjectNode().set("shopCode", salesBkNode.path("AgencyTxt")));
        booking.set("hasComplaint", salesBkNode.path("Complaintexists"));

        // map from MeasuresBk node
        JsonNode measuresBkNode = bookingInfo.path("MeasuresBk");
        booking.set("travelAmount", measuresBkNode.path("Ra"));
        booking.set("numberOfParticipants", measuresBkNode.path("Pax"));
        booking.set("numberOfAdults", measuresBkNode.path("Adults"));
        booking.set("numberOfChildren", measuresBkNode.path("Children"));
        booking.set("numberOfInfants", measuresBkNode.path("Infants"));
        booking.set("currency", measuresBkNode.path("Currency"));
        booking.set("duration", measuresBkNode.path("Duration"));

        // map from CustomerBk node
        JsonNode customerBkNode = bookingInfo.path("CustomerBk");
        booking.set("customerId", customerBkNode.path("Customerno"));

        ObjectNode bookerNode = objectMapper.createObjectNode();
        bookerNode.set("mobile", customerBkNode.path("Mobile"));
        bookerNode.set("bookerEmail", customerBkNode.path("Email"));
        bookerNode.set("phone", customerBkNode.path("Phone"));
        bookerNode.set("emergencyNumber", customerBkNode.path("Emergencyno"));
        booking.set("booker", bookerNode);
    }
}
