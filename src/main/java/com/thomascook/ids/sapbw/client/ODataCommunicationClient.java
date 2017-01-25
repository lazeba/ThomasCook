package com.thomascook.ids.sapbw.client;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@Component
public class ODataCommunicationClient {

    private RestTemplate restTemplate;
    private SapBwUriFactory uriFactory;

    public static final String BOOKING_ENTITY_NAME = "BOOKING";
    public static final String CUSTOMER_PROPERTY_NAME = "CustomerBk";

    @Autowired
    public ODataCommunicationClient(RestTemplate restTemplate, SapBwUriFactory sapBwUriFactory) {
        this.restTemplate = restTemplate;
        this.uriFactory = sapBwUriFactory;
    }

    public void ping() throws IOException {
        restTemplate.exchange(new RequestEntity(HttpMethod.GET, uriFactory.getBookingMetadataUri()), Resource.class);
    }

    public Edm getBookingMetadata() throws EntityProviderException, IOException {
        try (InputStream metadataInputStream = restTemplate.exchange(new RequestEntity(HttpMethod.GET, uriFactory.getBookingMetadataUri()), Resource.class)
                .getBody()
                .getInputStream()) {
            return EntityProvider.readMetadata(metadataInputStream, true);
        }
    }

    public ODataEntry getBookingEntry(String key) throws IOException, EntityProviderException, EdmException {
        Edm metadata = getBookingMetadata();
        EdmEntityContainer entityContainer = metadata.getDefaultEntityContainer();
        try (InputStream contentInputStream = restTemplate.exchange(new RequestEntity<>(HttpMethod.GET, uriFactory.getBookingUri(key)), Resource.class)
                .getBody()
                .getInputStream()) {
            return EntityProvider.readEntry(MediaType.APPLICATION_XML_VALUE,
                    entityContainer.getEntitySet(BOOKING_ENTITY_NAME),
                    contentInputStream,
                    EntityProviderReadProperties.init().build());
        }
    }
}
