package com.thomascook.ids.sapbw.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import com.thomascook.ids.sapbw.client.SapBwUriFactory;
import io.github.tcdl.msb.support.Utils;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnrichmentServiceTest {

    static final String PATH_TO_REQUEST_PAYLOAD = "/json/in-payload-example.json";
    static final String PATH_TO_BOOKING_RESPONSE = "/json/sapbw-booking-response-example.json";
    static final String PATH_TO_EXPECTED_BOOKING_PAYLOAD = "/json/out-payload-example.json";


    static final String PATH_TO_CUSTOMER_RESPONSE = "/odata-output/sapbw-customer-content.xml";
    static final String PATH_TO_CUSTOMER_METADATA = "/odata-output/sapbw-customer-metadata.xml";


    private EnrichmentService enrichmentService;
    private ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

    @Before
    public void setUp() {
        enrichmentService = new EnrichmentService(objectMapper);
    }

    @Test
    public void bookingInfoMappingTest() throws Exception {
        JsonNode requestPayload = readJsonNode(PATH_TO_REQUEST_PAYLOAD);
        Map bookingInfo = Utils.convert(readJsonNode(PATH_TO_BOOKING_RESPONSE), Map.class, objectMapper);
        ObjectNode enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), bookingInfo, null);

        assertEquals("34", enrichedPayload.path("sourceSystem").asText());
        assertEquals("1223374", enrichedPayload.path("bookingNumber").asText());
        assertEquals("<NO TEXT>", enrichedPayload.path("businessArea").asText());
        assertEquals("Tour Vital Touristik GmbH", enrichedPayload.path("agent").path("shopCode").asText());

        assertFalse(enrichedPayload.path("general").path("hasComplaint").asBoolean());
        assertEquals(1, enrichedPayload.path("general").path("travelAmount").asInt());
        assertEquals(2, enrichedPayload.path("general").path("numberOfParticipants").asInt());
        assertEquals(2, enrichedPayload.path("general").path("numberOfAdults").asInt());
        assertEquals(3, enrichedPayload.path("general").path("numberOfChildren").asInt());
        assertEquals(1, enrichedPayload.path("general").path("numberOfInfants").asInt());
        assertEquals("EUR", enrichedPayload.path("general").path("currency").asText());
        assertEquals(9, enrichedPayload.path("general").path("duration").asInt());

        assertEquals("115005070", enrichedPayload.path("general").path("customerId").asText());
        assertEquals("+4412345678", enrichedPayload.path("booker").path("mobile").asText());
        assertEquals("customer@thomascookonline.com", enrichedPayload.path("booker").path("bookerEmail").asText());
        assertEquals("+440000000", enrichedPayload.path("booker").path("phone").asText());
        assertEquals("12345678", enrichedPayload.path("booker").path("emergencyNumber").asText());
    }

    @Test
    public void bookingInfoMapping_HasComplaintTest() throws Exception {
        JsonNode requestPayload = readJsonNode(PATH_TO_REQUEST_PAYLOAD);
        JsonNode response = readJsonNode(PATH_TO_BOOKING_RESPONSE);

        ((ObjectNode)response.path("SalesBk")).put("Complaintexists", "Y");
        ObjectNode enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), Utils.convert(response, Map.class, objectMapper), null);
        assertTrue(enrichedPayload.path("general").path("hasComplaint").isBoolean());
        assertTrue(enrichedPayload.path("general").path("hasComplaint").asBoolean());

        ((ObjectNode)response.path("SalesBk")).put("Complaintexists", "N");
        enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), Utils.convert(response, Map.class, objectMapper), null);
        assertTrue(enrichedPayload.path("general").path("hasComplaint").isBoolean());
        assertFalse(enrichedPayload.path("general").path("hasComplaint").asBoolean());

        ((ObjectNode)response.path("SalesBk")).put("Complaintexists", MissingNode.getInstance());
        enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), Utils.convert(response, Map.class, objectMapper), null);
        assertTrue(enrichedPayload.path("general").path("hasComplaint").isNull());
    }

    @Test
    public void enrichBookingTest() throws Exception {
        JsonNode requestPayload = readJsonNode(PATH_TO_REQUEST_PAYLOAD);
        Map bookingInfo = Utils.convert(readJsonNode(PATH_TO_BOOKING_RESPONSE), Map.class, objectMapper);
        JsonNode expectedJsonNode = readJsonNode(PATH_TO_EXPECTED_BOOKING_PAYLOAD).path("booking");

        ObjectNode enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), bookingInfo, null);
        JSONAssert.assertEquals(expectedJsonNode.toString(), enrichedPayload.toString(), true);
    }

    @Test
    public void enrichCustomerTest() throws Exception {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        SapBwUriFactory uriFactoryMock = mock(SapBwUriFactory.class);
        ODataCommunicationClient oDataCommunicationClient = new ODataCommunicationClient(restTemplateMock, uriFactoryMock);

        try (
                InputStream metadataInputStream = Files.newInputStream(Paths.get(this.getClass().getResource(PATH_TO_CUSTOMER_METADATA).toURI()));
                InputStream contentInputStream = Files.newInputStream(Paths.get(this.getClass().getResource(PATH_TO_CUSTOMER_RESPONSE).toURI()))
        ) {
            URI metadataUri = new URI("metadataUri");
            when(uriFactoryMock.getCustomerMetadataUri()).thenReturn(metadataUri);
            URI contentUri = new URI("contentUri");
            when(uriFactoryMock.getCustomerUri("123", "456")).thenReturn(contentUri);

            ResponseEntity<Resource> metadataResponse = new ResponseEntity<>(new InputStreamResource(metadataInputStream, "odata meta"), HttpStatus.OK);
            when(restTemplateMock.exchange(new RequestEntity(HttpMethod.GET, metadataUri), Resource.class))
                    .thenReturn(metadataResponse);

            ResponseEntity<Resource> customerContentResponse = new ResponseEntity<>(new InputStreamResource(contentInputStream, "odata contentUri"), HttpStatus.OK);
            when(restTemplateMock.exchange(new RequestEntity(HttpMethod.GET, contentUri), Resource.class))
                    .thenReturn(customerContentResponse);

            ODataEntry customerEntry = oDataCommunicationClient.getCustomerEntry("123", "456");


            JsonNode requestPayload = readJsonNode(PATH_TO_REQUEST_PAYLOAD);
            Map bookingInfo = Utils.convert(readJsonNode(PATH_TO_BOOKING_RESPONSE), Map.class, objectMapper);
            Map customerInfo = customerEntry.getProperties();
            ObjectNode enrichedPayload = enrichmentService.enrichBooking(requestPayload.path("booking"), bookingInfo, customerInfo);


        }
    }

    private JsonNode readJsonNode(String fileName) throws Exception {
        URL resource = com.thomascook.ids.sapbw.service.EnrichmentServiceTest.class.getResource(fileName);
        return objectMapper.readTree(resource);
    }
}