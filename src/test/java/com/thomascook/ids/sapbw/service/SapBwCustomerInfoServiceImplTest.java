package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by Sergii Meleshko on 2/8/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */

@RunWith(MockitoJUnitRunner.class)
public class SapBwCustomerInfoServiceImplTest {

    private SapBwCustomerInfoService customerInfoService;
    @Mock
    private ODataCommunicationClient communicationClientMock;
    @Mock
    private ODataEntry customerODataMock;

    @Test
    public void testGetCustomerByKey() throws EntityProviderException, EdmException, IOException {
        customerInfoService = new SapBwCustomerInfoServiceImpl(communicationClientMock);
        when(communicationClientMock.getCustomerEntry("1", "2")).thenReturn(customerODataMock);
        when(customerODataMock.getProperties()).thenReturn(new HashMap<>());
        Map<String, Object> customerByKey = customerInfoService.getCustomerByKey("1", "2");
        assertNotNull(customerByKey);
        verify(communicationClientMock, times(1)).getCustomerEntry("1", "2");
        verify(customerODataMock, times(1)).getProperties();
    }
}