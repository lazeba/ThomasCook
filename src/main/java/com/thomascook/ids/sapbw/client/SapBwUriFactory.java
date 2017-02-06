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
    private static final String MSG_HEADER = "MSG_HEADER(Requester='TST')";
    private static final String CUSTOMER_SERVICE_MAINDATA_TEMPLATE = "CU_MAINDATA(Customerno='%s',Businessarea='%s')";
    private static final String BOOKING_SERVICE_MAINDATA_TEMPLATE = "BOOKING('%s')";

    private final String bookingUrl;
    private final String customerUrl; //todo clarify - define - format of link - is same for booking service

    @Autowired
    public SapBwUriFactory(Config config) {
        this.bookingUrl = config.getString("sap-bw-de.booking-url");
        this.customerUrl = config.getString("sap-bw-de.customer-url");//todo clarify-for-difference-with-booking-service base uri
    }

    public URI getBookingMetadataUri() {
        return UriComponentsBuilder.fromHttpUrl(bookingUrl)
                .path(METADATA)
                .build()
                .toUri();
    }

    public URI getBookingUri(String bookingKey) {
        return UriComponentsBuilder.fromHttpUrl(bookingUrl)
                .path(String.format(BOOKING_SERVICE_MAINDATA_TEMPLATE, bookingKey))
                .build()
                .toUri();
    }

    public URI getCustomerMetadataUri() {
        return UriComponentsBuilder.fromHttpUrl(customerUrl)
                .path(METADATA)
                .build()
                .toUri();
    }

    public URI getCustomerUri(String customerNo, String businessArea) {
        return UriComponentsBuilder.fromHttpUrl(customerUrl)
                .pathSegment(MSG_HEADER, String.format(CUSTOMER_SERVICE_MAINDATA_TEMPLATE, customerNo, businessArea))
                .build()
                .toUri();
    }

}
