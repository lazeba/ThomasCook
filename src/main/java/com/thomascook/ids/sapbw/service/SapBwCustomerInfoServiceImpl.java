package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Sergii Meleshko on 2/6/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */

@Component
public class SapBwCustomerInfoServiceImpl implements SapBwCustomerInfoService {
    private final ODataCommunicationClient oDataCommunicationClient;

    @Autowired
    public SapBwCustomerInfoServiceImpl(ODataCommunicationClient oDataCommunicationClient) {
        this.oDataCommunicationClient = oDataCommunicationClient;
    }

    @Override
    public Map<String, Object> getCustomerByKey(String customerNo, String businessArea) throws IOException, EntityProviderException, EdmException {
        ODataEntry customerEntry = oDataCommunicationClient.getCustomerEntry(customerNo, businessArea);
        return customerEntry.getProperties();
    }
}
