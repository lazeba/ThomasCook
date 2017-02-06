package com.thomascook.ids.sapbw.service;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Sergii Meleshko on 2/6/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public interface SapBwCustomerInfoService {
    /**
     * Method for retrieving customer information
     * according to provided customer number
     * @param customerNo - customer number provided by Sap Bw Booking service
     * @param businessArea - //todo define how to map and where to get
     * @return
     */
    Map<String, Object> getCustomerByKey(String customerNo, String businessArea) throws IOException, EntityProviderException, EdmException;
}
