package com.thomascook.ids.sapbw.client;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * Created by Sergii Meleshko on 1/3/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@RunWith(MockitoJUnitRunner.class)
public class ODataCommunicationClientTest {

    static final String CUSTOMER_BK_PROPERTY = "CustomerBk";
    static final String PATH_TO_BOOKING_METADATA = "odata-output/ZBOOKING_SRV_metadata.xml";
    static final String PATH_TO_BOOKING_CONTENT = "odata-output/BOOKING('12233742016050133').xml";
    static final String PATH_TO_CUSTOMER_RESPONSE = "/odata-output/sapbw-customer-content.xml";
    static final String PATH_TO_CUSTOMER_METADATA = "/odata-output/sapbw-customer-metadata.xml";

    private ODataCommunicationClient oDataCommunicationClient;

    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private SapBwUriFactory uriFactoryMock;

    @Before
    public void setUp() {
        oDataCommunicationClient = new ODataCommunicationClient(restTemplateMock, uriFactoryMock);
    }

    @Test
    public void testGetBookingMetadata() throws IOException, EntityProviderException, URISyntaxException, EdmException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(PATH_TO_BOOKING_METADATA).toURI()))) {
            ResponseEntity<Resource> responseEntity = new ResponseEntity<>(new InputStreamResource(inputStream), HttpStatus.OK);

            when(uriFactoryMock.getBookingMetadataUri()).thenReturn(new URI("some.uri"));
            when(restTemplateMock.exchange(new RequestEntity(HttpMethod.GET, uriFactoryMock.getBookingMetadataUri()), Resource.class))
                    .thenReturn(responseEntity);
            Edm bookingMetadata = oDataCommunicationClient.getMetadataByUri(uriFactoryMock.getBookingMetadataUri());
            assertNotNull(bookingMetadata);
            EdmEntityContainer defaultEntityContainer = bookingMetadata.getDefaultEntityContainer();
            EdmEntitySet booking = defaultEntityContainer.getEntitySet("BOOKING");
            assertNotNull(booking);
            verify(restTemplateMock, times(1)).exchange(anyObject(), (Class<Object>) anyObject());
        }
    }

    @Test
    public void testGetBookingInfo() throws EntityProviderException, EdmException, IOException, URISyntaxException {
        try (
                InputStream metadataInputStream = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(PATH_TO_BOOKING_METADATA).toURI()));
                InputStream contentInputStream = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(PATH_TO_BOOKING_CONTENT).toURI()))
        ) {
            URI metadataUri = new URI("metadataUri");
            when(uriFactoryMock.getBookingMetadataUri()).thenReturn(metadataUri);
            URI contentUri = new URI("contentUri");
            when(uriFactoryMock.getBookingUri("123")).thenReturn(contentUri);

            ResponseEntity<Resource> metadataResponse = new ResponseEntity<>(new InputStreamResource(metadataInputStream, "odata meta"), HttpStatus.OK);
            when(restTemplateMock.exchange(new RequestEntity(HttpMethod.GET, metadataUri), Resource.class))
                    .thenReturn(metadataResponse);

            ResponseEntity<Resource> bookingContentResponse = new ResponseEntity<>(new InputStreamResource(contentInputStream, "odata contentUri"), HttpStatus.OK);
            when(restTemplateMock.exchange(new RequestEntity(HttpMethod.GET, contentUri), Resource.class))
                    .thenReturn(bookingContentResponse);


            ODataEntry bookingEntry = oDataCommunicationClient.getBookingEntry("123");
            assertNotNull(bookingEntry);
            assertTrue(bookingEntry.getProperties().containsKey(CUSTOMER_BK_PROPERTY));
            verify(restTemplateMock, times(1)).exchange(new RequestEntity(HttpMethod.GET, metadataUri), Resource.class);
        }
    }

    @Test
    public void testGetCustomerInfo() throws EntityProviderException, EdmException, IOException, URISyntaxException {
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
            assertNotNull(customerEntry);
            verify(restTemplateMock, times(1)).exchange(new RequestEntity(HttpMethod.GET, metadataUri), Resource.class);
        }
    }
}