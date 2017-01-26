package com.thomascook.ids.sapbw.client;

import com.typesafe.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public class SapBwUriFactory {
    private static final String METADATA = "$metadata";
    private static final String BOOKING_SERVICE = "ZBOOKING_SRV";
    private static final String CUSTOMER_SERVICE = "ZCUSTOMER_SRV";

    private final String baseUri;

    @Autowired
    public SapBwUriFactory(Config config) {
        this.baseUri = config.getString("sap-bw-de.url");
    }

    public URI getBookingMetadataUri() {
        return UriComponentsBuilder.fromHttpUrl(baseUri)
                .pathSegment(BOOKING_SERVICE, METADATA)
                .build()
                .toUri();
    }

    public URI getBookingUri(String bookingKey) {
        return UriComponentsBuilder.fromHttpUrl(baseUri)
                .pathSegment(BOOKING_SERVICE, String.format("BOOKING('%s')", bookingKey))
                .build()
                .toUri();
    }

    public URI getCustomerMetadataUri() {
        return UriComponentsBuilder.fromHttpUrl(baseUri)
                .pathSegment(CUSTOMER_SERVICE, METADATA)
                .build()
                .toUri();
    }

    public URI getCustomerUri(String customerId) {
        return UriComponentsBuilder.fromHttpUrl(baseUri)
                .pathSegment(CUSTOMER_SERVICE, String.format("CU_MAINDATA('%s')", customerId))
                .queryParam("$format", "xml")
                .queryParam("sap-client", "010")
                .queryParam("sap-language", "EN")
                .build()
                .toUri();
    }

}
