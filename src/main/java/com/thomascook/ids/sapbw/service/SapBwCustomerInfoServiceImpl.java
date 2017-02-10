package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Sergii Meleshko on 2/6/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */

@Component
public class SapBwCustomerInfoServiceImpl implements SapBwCustomerInfoService {
    static final Logger LOG = LoggerFactory.getLogger(SapBwCustomerInfoServiceImpl.class);
    private final ODataCommunicationClient oDataCommunicationClient;

    @Autowired
    public SapBwCustomerInfoServiceImpl(ODataCommunicationClient oDataCommunicationClient) {
        this.oDataCommunicationClient = oDataCommunicationClient;
    }

    @Override
    public Map<String, Object> getCustomerByKey(String customerNo, String businessArea) {
        try {
            ODataEntry customerEntry = oDataCommunicationClient.getCustomerEntry(customerNo, businessArea);
            return customerEntry.getProperties();
        } catch (Exception e) {
            LOG.warn("Error while finding customer with id {} failed with ", customerNo, e);
            return null;
        }
    }
}
