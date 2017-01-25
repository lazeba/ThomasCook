package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by Sergii Meleshko on 1/4/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@RunWith(MockitoJUnitRunner.class)
public class SapBwBookingInfoServiceImplTest {

    private static final String BOOKING_KEY = "100020160501123";
    private SapBwBookingInfoService sapBwBookingInfoService;

    @Mock
    private ODataCommunicationClient oDataCommunicationClientMock;

    @Mock
    private ODataEntry oDataEntryMock;

    @Before
    public void setUp() {
        sapBwBookingInfoService = new SapBwBookingInfoServiceImpl(oDataCommunicationClientMock);
    }
    @Test
    public void getBookingByKey() throws Exception {
        when(oDataCommunicationClientMock.getBookingEntry(BOOKING_KEY)).thenReturn(oDataEntryMock);
        when(oDataEntryMock.getProperties()).thenReturn(getBookingInfoProperties());
        Map<String, Object> bookingInfo = sapBwBookingInfoService.getBookingByKey("1000", new SimpleDateFormat("yyyyMMdd").parse("20160501"), "123");
        assertNotNull(bookingInfo);
        verify(oDataCommunicationClientMock, times(1)).getBookingEntry(BOOKING_KEY);
        verify(oDataEntryMock, times(1)).getProperties();
    }

    @Test
    public void getBookingByKey1() throws Exception {
        String bookingKey = "123";
        when(oDataCommunicationClientMock.getBookingEntry(bookingKey)).thenReturn(oDataEntryMock);
        when(oDataEntryMock.getProperties()).thenReturn(getBookingInfoProperties());
        Map<String, Object> bookingInfo = sapBwBookingInfoService.getBookingByKey(bookingKey);
        assertNotNull(bookingInfo);
        verify(oDataCommunicationClientMock, times(1)).getBookingEntry(bookingKey);
        verify(oDataEntryMock, times(1)).getProperties();
    }

    private HashMap<String, Object> getBookingInfoProperties() {
        HashMap<String, Object> bookingInfoProperties = new HashMap<>();
        bookingInfoProperties.put("CustomerBk", "CustomerId");
        return bookingInfoProperties;
    }
}