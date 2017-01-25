package com.thomascook.ids.sapbw.client;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Sergii Meleshko on 1/4/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */

@RunWith(MockitoJUnitRunner.class)
public class SapBwUriFactoryTest {

    private SapBwUriFactory uriFactory;

    @Before
    public void setUp() {
        uriFactory = new SapBwUriFactory(ConfigFactory.load());
    }

    @Test
    public void getBookingMetadataUri() throws Exception {
        URI bookingMetadataUri = uriFactory.getBookingMetadataUri();
        assertNotNull(bookingMetadataUri);
        assertEquals("/ZBOOKING_SRV/$metadata", bookingMetadataUri.getPath().trim());
    }

    @Test
    public void getBookingUri() throws Exception {
        String bookingKey = "booking-key";
        URI bookingUri = uriFactory.getBookingUri(bookingKey);
        assertNotNull(bookingUri);
        assertEquals("/ZBOOKING_SRV/BOOKING('" + bookingKey + "')", bookingUri.getPath().trim());
    }

    @Test
    public void getCustomerMetadataUri() throws Exception {
        URI customerMetadataUri = uriFactory.getCustomerMetadataUri();
        assertNotNull(customerMetadataUri);
        assertEquals("/ZCUSTOMER_SRV/$metadata", customerMetadataUri.getPath().trim());
    }

    @Test
    public void getCustomerUri() throws Exception {
        String customerId = "customer-id";
        URI customerUri = uriFactory.getCustomerUri(customerId);
        assertNotNull(customerUri);
        assertEquals("/ZCUSTOMER_SRV/CU_MAINDATA('" + customerId + "')", customerUri.getPath().trim());
    }

}