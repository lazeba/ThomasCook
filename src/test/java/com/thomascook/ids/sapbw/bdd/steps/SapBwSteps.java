package com.thomascook.ids.sapbw.bdd.steps;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import com.thomascook.ids.sapbw.SapBwApplication;
import com.thomascook.ids.sapbw.client.SapBwUriFactory;
import com.typesafe.config.Config;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.github.tcdl.msb.api.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * Created by rdro-tc on 30.05.16.
 */
public class SapBwSteps {

    private final static Map<String, String> SAPBW_NAMESPACES = ImmutableMap.of(
        "m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata",
        "d", "http://schemas.microsoft.com/ado/2007/08/dataservices"
    );

    private Config config = SapBwApplication.CONFIG;
    private ConfigurableApplicationContext msContext;
    private MsbContext msbContext;
    private SapBwUriFactory uriFactory;

    private ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private String routingKey;
    private String fromNamespace;
    private String toNamespace;
    private URI url;

    private ClientAndServer server;
    private Optional<JsonNode> lastOutMessage;
    private BlockingQueue<JsonNode> outMessages;

    @Before
    public void setUp() throws Exception {
        msbContext = new MsbContextBuilder().enableShutdownHook(true).build();
        msContext = SapBwApplication.APP.run();
        uriFactory = new SapBwUriFactory(config);

        routingKey = config.getString("namespace.routingKey");
        fromNamespace = config.getString("namespace.from");
        toNamespace = config.getString("namespace.to");
        url = new URI(config.getString("sap-bw-de.booking-url"));

        server = startClientAndServer(url.getPort());

        lastOutMessage = Optional.empty();
        outMessages = new LinkedBlockingQueue<>();
        startOutMessageListener();
    }

    @After
    public void tearDown() throws Exception {
        if (this.server != null && server.isRunning()) {
            this.server.stop(true);
        }

        if (msContext != null && msContext.isRunning()) {
            msContext.stop();
        }

        lastOutMessage = Optional.empty();
        outMessages.clear();
    }

    @When("^Booking message (\\S+) has been sent for booking (\\S+) with:$")
    public void overrideAndSendCanonicalMessage(String messageFile, String bookingId, DataTable valuesTable) throws Exception {
        String messageJson = IOUtils.toString(getClass().getResourceAsStream(messageFile));
        JsonNode messageNode = objectMapper.readValue(messageJson, JsonNode.class);

        overrideCanonicalMessage(messageNode, valuesTable.asMap(String.class, String.class));
        sendMessage(fromNamespace, messageNode);
    }

    @Given("^SapBw mock responds with status (\\d+) and body (\\S+) for metadata$")
    public void mockResponseForMetaData(int statusCode, String responseFile) throws Exception  {
        String responseBody = IOUtils.toString(getClass().getResourceAsStream(responseFile));
        mockSapBwResponse(statusCode, uriFactory.getBookingMetadataUri(), responseBody);
    }

    @Given("^SapBw mock responds with status (\\d+) and body (\\S+) for booking key (\\S+) and:$")
    public void mockResponseForBooking(int statusCode, String responseFile, String bookingKey, DataTable valuesTable) throws Exception  {
        String responseBody = overrideSapBwResponse(responseFile, valuesTable.asMap(String.class, String.class));
        mockSapBwResponse(statusCode, uriFactory.getBookingUri(bookingKey), responseBody);
    }

    @Given("^SapBw mock responds with status (\\d+) and body (\\S+) for customer metadata$")
    public void mockResponseForCustomerMetaData(int statusCode, String responseFile) throws Exception  {
        String responseBody = IOUtils.toString(getClass().getResourceAsStream(responseFile));
        mockSapBwResponse(statusCode, uriFactory.getCustomerMetadataUri(), responseBody);
    }

    @Given("^SapBw mock responds with status (\\d+) and body (\\S+) for customer key (\\S+) and business area (\\S+) and:$")
    public void mockResponseForCustomer(int statusCode, String responseFile, String customerNo, String businessArea, DataTable valuesTable) throws Exception  {
        String responseBody = overrideSapBwResponse(responseFile, valuesTable.asMap(String.class, String.class));
        mockSapBwResponse(statusCode, uriFactory.getCustomerUri(customerNo, businessArea), responseBody);
    }


    @Then("^Enriched message is published with:")
    public void checkOutMessage(DataTable metaDataTable) throws Throwable {
        Optional<JsonNode> outMessage = getLastOutMessage();

        Assert.assertTrue("Message is not received", outMessage.isPresent());

        metaDataTable.asMap(String.class, Object.class).entrySet().forEach(entry -> {
            String pointer = entry.getKey();
            String expectedFieldValue = getNodeByPointer(outMessage.get(), pointer).asText();

            Assert.assertEquals(expectedFieldValue, entry.getValue());
        });
    }

    private void mockSapBwResponse(int statusCode, URI uri, String response) {
        HttpRequest httpReq = HttpRequest.request()
                .withMethod("GET")
                .withPath(uri.getPath());

        HttpResponse httpResp = HttpResponse.response()
                .withStatusCode(statusCode)
                .withBody(new StringBody(response, Charset.forName("UTF-8")));

        server.when(httpReq).respond(httpResp);
    }

    private void sendMessage(String namespace, JsonNode message) {
        RequestOptions requestOptions = new AmqpRequestOptions.Builder()
                .withExchangeType(ExchangeType.TOPIC)
                .withRoutingKey(routingKey)
                .withMessageTemplate(new MessageTemplate())
                .build();

        msbContext
                .getObjectFactory()
                .createRequesterForFireAndForget(namespace, requestOptions)
                .publish(message);
    }

    private void overrideCanonicalMessage(JsonNode messageNode, Map<String, String> values) throws Exception {
        values.forEach((pointer, value) -> {
            String path = StringUtils.substringBeforeLast(pointer, "/");
            String fieldName = StringUtils.substringAfterLast(pointer, "/");
            path = !path.startsWith("/") ? "/" + path : path;

            ObjectNode nodeByPointer = (ObjectNode) getNodeByPointer(messageNode, path);

            Validate.notNull(nodeByPointer, "Node not found at '%s'", path);
            nodeByPointer.set(fieldName, new TextNode(value));
        });
    }

    private String overrideSapBwResponse(String mockResponseFile, Map<String, String> values) throws Exception {
        Document document = new SAXReader().read(getClass().getResourceAsStream(mockResponseFile));

        values.forEach((pointer, value) -> {
            XPath xpath = DocumentHelper.createXPath(pointer);
            xpath.setNamespaceURIs(SAPBW_NAMESPACES);

            Element node = (Element) xpath.selectSingleNode(document);
            Validate.notNull(node, "Not not found at '%s'", pointer);
            node.setText(value);
        });

        return document.asXML();
    }

    private void startOutMessageListener() {
        msbContext.getObjectFactory().createResponderServer(toNamespace, new ResponderOptions.Builder().build(),
                (request, responder) -> outMessages.put(request), new TypeReference<JsonNode>(){})
                .listen();

    }

    private Optional<JsonNode> getLastOutMessage() throws InterruptedException {
        JsonNode message = lastOutMessage.orElseGet(() -> {
            try {
                return outMessages.poll(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return Optional.ofNullable(message);
    }

    private JsonNode getNodeByPointer(JsonNode node, String pointer) {
        if (!pointer.startsWith("/")) {
            pointer = "/" + pointer;
        }

        JsonNode nodeByPointer = node.at(pointer);
        Validate.notNull(node, "Parent node not found: " + pointer);

        return nodeByPointer;
    }
}